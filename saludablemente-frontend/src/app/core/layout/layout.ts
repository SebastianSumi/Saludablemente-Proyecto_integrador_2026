import { Component, OnDestroy, OnInit, TemplateRef, ViewChild, computed, inject, signal } from '@angular/core';
import { DatePipe, Location } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { TuiButton, TuiDataList, TuiDropdown, TuiIcon, TuiNotificationService } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { Subscription, interval } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../auth/auth-service';
import { ThemeService } from '../theme/theme-service';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { LoadingBarComponent } from './loading-bar/loading-bar';
import { AutoRevealDirective } from '../../shared/animations/auto-reveal.directive';
import { AutoParallaxDirective } from '../../shared/animations/auto-parallax.directive';
import { PerfilService } from '../../features/admin/perfil/perfil-service';
import { PersonaService } from '../../features/admin/personal/persona/persona-service';
import { NotificacionService } from '../../features/colaborador/notificaciones/notificacion-service';
import { BandejaNotificacion } from '../../features/colaborador/notificaciones/models';
import { UsuarioPerfil } from '../../features/admin/perfil/perfil.model';
import { ADMIN_MODULOS } from '../../features/admin/home/admin-modulos';

const POLLING_MS = 45000;

export interface Miga {
  titulo: string;
  ruta?: string;
  fragment?: string;
}

@Component({
  selector: 'app-layout',
  imports: [
    Avatar,
    RouterOutlet,
    RouterLink,
    TuiBadge,
    TuiButton,
    TuiDataList,
    TuiDropdown,
    TuiIcon,
    DatePipe,
    LoadingBarComponent,
    AutoRevealDirective,
    AutoParallaxDirective,
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly notifications = inject(TuiNotificationService, {
    optional: true,
  });

  private readonly perfilService = inject(PerfilService);
  private readonly personasService = inject(PersonaService);
  private readonly notificaciones = inject(NotificacionService);

  protected readonly perfil = signal<UsuarioPerfil | null>(null);
  protected readonly perfilLoading = signal(false);
  protected readonly perfilError = signal<string | null>(null);
  protected readonly menuAbierto = signal<boolean>(false);
  protected readonly fotoUrl = signal<string | undefined>(undefined);
  protected readonly noLeidas = signal(0);
  protected readonly migas = signal<Miga[]>([{ titulo: 'Inicio', ruta: '/' }]);

  protected readonly esAdmin = computed(() => this.auth.tieneAlgunRol(['Administrador']));

  /** Evaluador puro: tiene el rol pero no es Administrador. */
  protected readonly esEvaluador = computed(
    () =>
      !this.auth.tieneAlgunRol(['Administrador']) &&
      this.auth.tieneAlgunRol(['Evaluador'])
  );

  protected readonly navEvaluador = [
    { titulo: 'Social', ruta: '/social', icono: '@tui.messages-square' },
    { titulo: 'Panel', ruta: '/evaluador', icono: '@tui.layout-dashboard' },
    { titulo: 'Colaboradores', ruta: '/evaluador/colaboradores', icono: '@tui.users' },
    { titulo: 'Nutricional', ruta: '/evaluador/nutricional', icono: '@tui.apple' },
    { titulo: 'Aptitud física', ruta: '/evaluador/aptitud', icono: '@tui.activity' },
    { titulo: 'Alertas', ruta: '/evaluador/alertas', icono: '@tui.bell' },
    { titulo: 'Metas clínicas', ruta: '/evaluador/metas', icono: '@tui.target' },
    { titulo: 'Jornadas', ruta: '/nutricional/jornadas', icono: '@tui.calendar-heart' },
    { titulo: 'Mis inscripciones', ruta: '/nutricional/mis-inscripciones', icono: '@tui.ticket' },
    { titulo: 'Reportes', ruta: '/evaluador/reportes', icono: '@tui.file-text' },
  ];

  /** Ancla de la tarjeta del módulo abierto, para volver a ella al ir a /admin. */
  protected readonly anclaModulo = computed(() =>
    this.migas().find((m) => m.fragment)?.fragment
  );

  protected readonly navColaborador = [
    { titulo: 'Social', ruta: '/social', icono: '@tui.messages-square' },
    { titulo: 'Mi panel', ruta: '/colaborador', icono: '@tui.layout-dashboard' },
    { titulo: 'Mi perfil', ruta: '/colaborador/mi-perfil', icono: '@tui.user' },
    { titulo: 'Mis evaluaciones', ruta: '/colaborador/mis-evaluaciones', icono: '@tui.activity' },
    { titulo: 'Mis alertas', ruta: '/colaborador/mis-alertas', icono: '@tui.bell' },
    { titulo: 'Mis metas', ruta: '/colaborador/mis-metas', icono: '@tui.target' },
    { titulo: 'Mis asistencias', ruta: '/colaborador/mis-asistencias', icono: '@tui.clipboard-check' },
    { titulo: 'Jornadas', ruta: '/nutricional/jornadas', icono: '@tui.calendar-heart' },
    { titulo: 'Mis inscripciones', ruta: '/nutricional/mis-inscripciones', icono: '@tui.ticket' },
    { titulo: 'Noticias', ruta: '/colaborador/noticias', icono: '@tui.newspaper' },
    { titulo: 'Notificaciones', ruta: '/colaborador/mis-notificaciones', icono: '@tui.inbox' },
  ];

  protected readonly campanaAbierta = signal(false);
  protected readonly ultimas = signal<BandejaNotificacion[]>([]);
  protected readonly campanaLoading = signal(false);
  protected readonly campanaError = signal<string | null>(null);
  private polling?: Subscription;

  @ViewChild('perfilMenu') perfilMenu!: TemplateRef<unknown>;

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        const migas = construirMigas(e.urlAfterRedirects);
        this.migas.set(migas);
        this.avisarPaginaLista(migas[migas.length - 1]?.titulo ?? 'Página');
      });
    this.migas.set(construirMigas(this.router.url));
  }

  /** Aviso tipo toast al terminar de cargar cada pantalla. */
  private avisarPaginaLista(titulo: string): void {
    this.notifications
      ?.open(`Página lista: ${titulo}`, {
        label: 'Listo',
        appearance: 'info',
        size: 's',
        icon: '@tui.check',
        autoClose: 2200,
        closable: false,
      })
      .subscribe();
  }

  ngOnInit(): void {
    if (this.auth.estaAutenticado()) {
      this.cargarPerfil();
    }
  }

  ngOnDestroy(): void {
    this.polling?.unsubscribe();
  }

  protected inicialesUsuario(): string {
    const n = this.auth.obtenerNombre();
    if (!n) return '?';
    return n
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected abrirMenu(): void {
    this.cargarPerfil();
  }

  private cargarPerfil(): void {
    if (this.perfil() || this.perfilLoading()) return;
    this.perfilLoading.set(true);
    this.perfilError.set(null);

    this.perfilService.obtener().subscribe({
      next: (data) => {
        this.perfil.set(data);
        this.perfilLoading.set(false);
        this.actualizarContador(data.idPersona);
        this.iniciarPolling(data.idPersona);
        this.personasService.obtener(data.idPersona).subscribe({
          next: (p) => this.fotoUrl.set(urlAvatarFoto(p.avatarUrl)),
          error: () => undefined,
        });
      },
      error: () => {
        this.perfilError.set('No se pudo cargar el perfil.');
        this.perfilLoading.set(false);
      },
    });
  }

  protected actualizarContador(idPersona?: number): void {
    const id = idPersona ?? this.perfil()?.idPersona;
    if (!id) return;
    this.notificaciones.contadorNoLeidas(id).subscribe({
      next: (n) => this.noLeidas.set(n ?? 0),
      error: () => undefined,
    });
  }

  private iniciarPolling(idPersona: number): void {
    this.polling?.unsubscribe();
    this.polling = interval(POLLING_MS).subscribe(() => {
      this.actualizarContador(idPersona);
      if (this.campanaAbierta()) this.cargarUltimas(false);
    });
  }

  protected alternarCampana(): void {
    const abriendo = !this.campanaAbierta();
    this.campanaAbierta.set(abriendo);
    if (abriendo) this.cargarUltimas();
  }

  protected cerrarCampana(): void {
    this.campanaAbierta.set(false);
  }

  private cargarUltimas(conSpinner = true): void {
    const id = this.perfil()?.idPersona;
    if (!id) return;
    if (conSpinner) this.campanaLoading.set(true);
    this.campanaError.set(null);
    this.notificaciones.misAlertas(id).subscribe({
      next: (data) => {
        this.ultimas.set((data ?? []).slice(0, 5));
        this.noLeidas.set((data ?? []).filter((n) => !n.leido).length);
        this.campanaLoading.set(false);
      },
      error: () => {
        this.campanaError.set('No se pudieron cargar las notificaciones.');
        this.campanaLoading.set(false);
      },
    });
  }

  protected abrirNotificacion(n: BandejaNotificacion): void {
    this.cerrarCampana();
    const destino = this.destinoNotificacion(n);
    if (!n.leido) {
      this.notificaciones.marcarLeida(n.idNotificacion).subscribe({
        next: () => {
          this.actualizarContador();
          this.router.navigate(destino);
        },
        error: () => this.router.navigate(destino),
      });
    } else {
      this.router.navigate(destino);
    }
  }

  /** Destino del click: la alerta que generó la notificación, o la bandeja como fallback. */
  protected destinoNotificacion(n: BandejaNotificacion): string[] {
    if ((n.origenModulo ?? '').toLowerCase().trim() === 'social') {
      return n.idOrigenReferencia ? ['/social/posts', String(n.idOrigenReferencia)] : ['/social'];
    }
    const esAlerta = (n.origenModulo ?? '').toLowerCase().includes('alerta');
    if (esAlerta && n.idOrigenReferencia) {
      if (this.esAdmin()) {
        return ['/alertaclinica/alertas', String(n.idOrigenReferencia)];
      }
      if (this.esEvaluador()) {
        return ['/evaluador/alertas', String(n.idOrigenReferencia)];
      }
      return ['/colaborador/mis-alertas', String(n.idOrigenReferencia)];
    }
    return this.esEvaluador()
      ? ['/evaluador']
      : ['/colaborador/mis-notificaciones'];
  }

  protected esAlertaClinica(n: BandejaNotificacion): boolean {
    return (n.origenModulo ?? '').toLowerCase().includes('alerta') && !!n.idOrigenReferencia;
  }

  protected verTodasNotificaciones(): void {
    this.cerrarCampana();
    this.actualizarContador();
    this.router.navigate(['/colaborador/mis-notificaciones']);
  }

  protected haceCuanto(fecha?: string): string {
    return haceCuanto(fecha, Date.now());
  }

  protected cerrarDropdown(): void {
    this.menuAbierto.set(false);
  }

  protected cerrarSesion(): void {
    this.menuAbierto.set(false);
    this.auth.cerrarSesion();
  }

  protected volver(): void {
    if (typeof window !== 'undefined' && window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/']);
    }
  }
}

const TITULOS: Record<string, string> = {
  admin: 'Módulos',
  evaluador: 'Panel evaluador',
  colaborador: 'Mi panel',
  'mi-perfil': 'Mi perfil',
  personal: 'Personal',
  teams: 'Teams',
  catalogos: 'Catálogos',
  catalogo: 'Catálogos',
  nutricional: 'Nutricional',
  jornadas: 'Jornadas nutricionales',
  'mis-inscripciones': 'Mis inscripciones',
  aptitud: 'Aptitud física',
  alertaclinica: 'Alertas clínicas',
  asistencias: 'Asistencias',
  metas: 'Metas',
  noticias: 'Noticias',
  notificaciones: 'Notificaciones',
  seguridad: 'Seguridad',
  'no-autorizado': 'No autorizado',
};

export function haceCuanto(fechaIso: string | undefined, ahoraMs: number): string {
  if (!fechaIso) return '';
  const t = new Date(fechaIso).getTime();
  if (!Number.isFinite(t)) return '';
  const diffMin = Math.max(0, Math.floor((ahoraMs - t) / 60000));
  if (diffMin < 1) return 'ahora mismo';
  if (diffMin < 60) return `hace ${diffMin} min`;
  const diffH = Math.floor(diffMin / 60);
  if (diffH < 24) return `hace ${diffH} h`;
  const diffD = Math.floor(diffH / 24);
  if (diffD < 30) return `hace ${diffD} d`;
  return new Date(t).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' });
}

export function construirMigas(url: string): Miga[] {
  const limpio = url.split('?')[0].split('#')[0];
  const partes = limpio.split('/').filter((p) => p.length > 0);
  const migas: Miga[] = [{ titulo: 'Inicio', ruta: '/' }];
  if (partes.length === 0) return migas;

  if (partes[0] === 'admin' && partes[1] === 'modulo' && partes[2]) {
    const mod = ADMIN_MODULOS.find((m) => m.id === partes[2]);
    migas.push({ titulo: 'Módulos', ruta: '/admin', fragment: 'modulo-' + partes[2] });
    migas.push({ titulo: mod?.titulo ?? partes[2] });
    return migas;
  }

  let acumulada = '';
  partes.forEach((p, i) => {
    acumulada += `/${p}`;
    const esUltima = i === partes.length - 1;
    const titulo = TITULOS[p] ?? humanizar(p);
    migas.push(esUltima ? { titulo } : { titulo, ruta: acumulada });
  });
  return migas;
}

function humanizar(slug: string): string {
  return slug
    .split('-')
    .map((w) => (w.length > 0 ? w.charAt(0).toUpperCase() + w.slice(1) : w))
    .join(' ');
}
