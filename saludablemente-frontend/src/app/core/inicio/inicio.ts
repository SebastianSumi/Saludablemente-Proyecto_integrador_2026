import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { catchError, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../auth/auth-service';
import { homePorRol } from '../auth/roles';
import { PersonaService } from '../../features/admin/personal/persona/persona-service';
import type { PersonalAgregado } from '../../features/admin/personal/persona/persona.model';
import { AlertaClinicaService } from '../../features/admin/alertaclinica/alerta/alerta-clinica-service';
import type { AlertaAgregado } from '../../features/admin/alertaclinica/alerta/alerta-clinica.model';
import { ActividadService } from '../../features/admin/asistencias/reporte-qr-actividad-service';
import type { Actividad } from '../../features/admin/asistencias/models';
import { CuarentenaService } from '../../features/admin/asistencias/cuarentena-service';
import { ExoneracionService } from '../../features/admin/asistencias/exoneracion-service';
import type { Ausentismo } from '../../features/admin/asistencias/models';
import { NoticiaPublicaService } from '../../features/admin/noticias/noticia-service';
import type { NoticiaFeed } from '../../features/admin/noticias/models';
import { PerfilService } from '../../features/admin/perfil/perfil-service';
import { ColAlertaService } from '../../features/colaborador/alerta/alerta-service';
import { NotificacionService } from '../../features/colaborador/notificaciones/notificacion-service';
import { MetaService } from '../../features/admin/metas/meta-service';
import { RevealDirective } from '../../shared/animations/reveal.directive';
import { ParallaxDirective } from '../../shared/animations/parallax.directive';
import { ScrollLinkedDirective } from '../../shared/animations/scroll-linked.directive';

interface Acceso {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
  roles?: string[];
}

@Component({
  imports: [
    DatePipe,
    TitleCasePipe,
    RouterLink,
    TuiButton,
    TuiIcon,
    TuiBadge,
    RevealDirective,
    ParallaxDirective,
    ScrollLinkedDirective,
  ],
  selector: 'app-inicio',
  styleUrl: './inicio.css',
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly personas = inject(PersonaService);
  private readonly alertas = inject(AlertaClinicaService);
  private readonly actividadesSrv = inject(ActividadService);
  private readonly cuarentenaSrv = inject(CuarentenaService);
  private readonly exoneraciones = inject(ExoneracionService);
  private readonly noticiasSrv = inject(NoticiaPublicaService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly misAlertasSrv = inject(ColAlertaService);
  private readonly notiSrv = inject(NotificacionService);
  private readonly metasSrv = inject(MetaService);

  protected readonly esAdmin = computed(() =>
    this.auth.tieneAlgunRol(['Administrador']),
  );

  /** Gestión = ve KPIs operativos y pendientes de acción. */
  protected readonly esGestion = computed(() =>
    this.auth.tieneAlgunRol(['Administrador', 'Coordinador']),
  );

  protected readonly esColaborador = computed(() =>
    this.auth.tieneAlgunRol(['Colaborador']),
  );

  protected readonly loading = signal(true);

  // ── Gestión ──
  protected readonly personal = signal<PersonalAgregado | null>(null);
  protected readonly alertasAgregado = signal<AlertaAgregado | null>(null);
  protected readonly cuarentenaPendiente = signal<number | null>(null);
  protected readonly ausentismo = signal<Ausentismo | null>(null);

  // ── Todos ──
  protected readonly actividadesHoy = signal<Actividad[] | null>(null);
  protected readonly noticias = signal<NoticiaFeed[] | null>(null);

  // ── Colaborador ──
  protected readonly misAlertasPendientes = signal<number | null>(null);
  protected readonly notiNoLeidas = signal<number | null>(null);
  protected readonly misMetasActivas = signal<number | null>(null);

  protected readonly hayKpisGestion = computed(
    () =>
      this.personal() !== null ||
      this.alertasAgregado() !== null ||
      this.cuarentenaPendiente() !== null ||
      (this.actividadesHoy() !== null && this.esGestion()),
  );

  protected readonly hayKpisColaborador = computed(
    () =>
      this.misAlertasPendientes() !== null ||
      this.notiNoLeidas() !== null ||
      this.misMetasActivas() !== null,
  );

  protected readonly hayPendientes = computed(
    () =>
      (this.cuarentenaPendiente() ?? 0) > 0 ||
      this.alertasPendientes() > 0 ||
      (this.ausentismo()?.total ?? 0) > 0,
  );

  protected alertasPendientes(): number {
    const a = this.alertasAgregado();
    if (!a) return 0;
    return Math.max(0, a.totalAlertas - a.totalAtendidas);
  }

  protected readonly accesosVisibles = computed(() =>
    this.accesos.filter(
      (a) => !a.roles || this.auth.tieneAlgunRol(a.roles),
    ),
  );

  ngOnInit(): void {
    this.cargarComun();
    if (this.esGestion()) {
      this.cargarGestion();
    }
    if (this.esColaborador()) {
      this.cargarMiEstado();
    }
  }

  protected irAPanelSegunRol(): void {
    this.router.navigate([homePorRol(this.auth.obtenerRoles())]);
  }

  protected fechaHoy(): string {
    return new Date().toLocaleDateString('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
  }

  // -------------------------------------------------------------------
  // Cargas (cada fuente falla en silencio y oculta su tarjeta)
  // -------------------------------------------------------------------

  private cargarComun(): void {
    this.actividadesSrv
      .deHoy()
      .pipe(catchError(() => of(null)))
      .subscribe((acts) => {
        this.actividadesHoy.set(acts);
        this.revisarFinCarga();
      });

    this.noticiasSrv
      .feed({ page: 0, size: 3 })
      .pipe(catchError(() => of(null)))
      .subscribe((pag) => {
        this.noticias.set(pag ? pag.content : null);
        this.revisarFinCarga();
      });
  }

  private cargarGestion(): void {
    this.personas
      .reporte()
      .pipe(catchError(() => of(null)))
      .subscribe((r) => this.personal.set(r));

    this.alertas
      .reporte()
      .pipe(catchError(() => of(null)))
      .subscribe((r) => this.alertasAgregado.set(r ? r.agregado : null));

    this.cuarentenaSrv
      .contarPendientes()
      .pipe(catchError(() => of(null)))
      .subscribe((r) => this.cuarentenaPendiente.set(r ? r.total : null));

    this.exoneraciones
      .detectarAusentismo(3)
      .pipe(catchError(() => of(null)))
      .subscribe((a) => this.ausentismo.set(a));
  }

  private cargarMiEstado(): void {
    this.misAlertasSrv
      .listarMias({ estado: 'PENDIENTE' })
      .pipe(catchError(() => of(null)))
      .subscribe((list) =>
        this.misAlertasPendientes.set(list ? list.length : null),
      );

    this.perfilSrv
      .obtener()
      .pipe(
        catchError(() => of(null)),
        switchMap((p) => {
          if (!p) return of(null);
          return this.notiSrv.contadorNoLeidas(p.idPersona).pipe(
            catchError(() => of(null)),
          );
        }),
      )
      .subscribe((n) => this.notiNoLeidas.set(n));

    this.perfilSrv
      .obtener()
      .pipe(
        catchError(() => of(null)),
        switchMap((p) => {
          if (!p) return of(null);
          return this.metasSrv.listarPorPersona(p.idPersona, 'ACTIVA').pipe(
            catchError(() => of(null)),
          );
        }),
      )
      .subscribe((metas) =>
        this.misMetasActivas.set(metas ? metas.length : null),
      );
  }

  private readonly pendientesCarga = signal(2);

  private revisarFinCarga(): void {
    this.pendientesCarga.update((n) => n - 1);
    if (this.pendientesCarga() <= 0) {
      this.loading.set(false);
    }
  }

  protected readonly accesos: Acceso[] = [
    {
      titulo: 'Personas',
      descripcion: 'Ficha 360°, filtros y QR',
      icono: '@tui.users',
      ruta: '/personal/personas',
      roles: ['Administrador'],
    },
    {
      titulo: 'Actividades de hoy',
      descripcion: 'Agenda, QR y registro',
      icono: '@tui.calendar',
      ruta: '/actividades',
      roles: ['Administrador', 'Coordinador'],
    },
    {
      titulo: 'Registro de asistencias',
      descripcion: 'Marcaciones y cuarentena',
      icono: '@tui.clipboard-check',
      ruta: '/asistencias/registro',
      roles: ['Administrador', 'Coordinador'],
    },
    {
      titulo: 'Alertas clínicas',
      descripcion: 'Detectadas por las reglas',
      icono: '@tui.bell',
      ruta: '/alertaclinica/alertas',
      roles: ['Administrador', 'Coordinador'],
    },
    {
      titulo: 'Mis asistencias',
      descripcion: 'Tu historial de marcaciones',
      icono: '@tui.clipboard-check',
      ruta: '/colaborador/mis-asistencias',
      roles: ['Colaborador'],
    },
    {
      titulo: 'Mis alertas',
      descripcion: 'Tus alertas de salud',
      icono: '@tui.heart-pulse',
      ruta: '/colaborador/mis-alertas',
      roles: ['Colaborador'],
    },
    {
      titulo: 'Mis metas',
      descripcion: 'Tu progreso y check-ins',
      icono: '@tui.target',
      ruta: '/colaborador/mis-metas',
      roles: ['Colaborador'],
    },
    {
      titulo: 'Noticias',
      descripcion: 'Comunicados institucionales',
      icono: '@tui.newspaper',
      ruta: '/noticias',
    },
    {
      titulo: 'Mi perfil',
      descripcion: 'Tus datos y seguridad',
      icono: '@tui.user',
      ruta: '/mi-perfil',
    },
  ];
}
