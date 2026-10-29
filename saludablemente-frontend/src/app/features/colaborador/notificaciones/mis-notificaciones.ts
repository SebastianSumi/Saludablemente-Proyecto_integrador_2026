import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiInput, TuiTextfield } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { NotificacionService } from './notificacion-service';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { UsuarioPerfil } from '../../admin/perfil/perfil.model';
import { AuthService } from '../../../core/auth/auth-service';
import { ConfirmService } from '../../../core/services/confirm-service';
import { haceCuanto } from '../../../core/layout/layout';
import { BandejaNotificacion } from './models';
import {
  OrigenVisual,
  claveModulo,
  etiquetaCanal,
  origenVisual,
  rutaOrigenNotificacion,
} from './origen-notificacion';

type ModoVista = 'tarjetas' | 'lista';
type FiltroEstado = 'todas' | 'no-leidas' | 'leidas';
type Orden = 'fecha' | 'modulo' | 'estado';

@Component({
  selector: 'app-mis-notificaciones',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiIcon,
    TuiInput,
    TuiTextfield,
    TuiTable,
  ],
  templateUrl: './mis-notificaciones.html',
  styleUrl: './mis-notificaciones.css',
})
export class MisNotificaciones implements OnInit {
  private readonly notificaciones = inject(NotificacionService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly confirmar = inject(ConfirmService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly perfil = signal<UsuarioPerfil | null>(null);
  protected readonly items = signal<BandejaNotificacion[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly vista = signal<ModoVista>('tarjetas');
  protected readonly filtroEstado = signal<FiltroEstado>('todas');
  protected readonly filtroModulo = signal('');
  protected readonly filtroDesde = signal('');
  protected readonly filtroHasta = signal('');
  protected readonly filtroTexto = signal('');
  protected readonly orden = signal<Orden>('fecha');

  protected readonly textoCtrl = new FormControl('', { nonNullable: true });

  protected readonly esAdmin = computed(() => this.auth.tieneAlgunRol(['Administrador']));
  protected readonly homeRuta = computed(() => (this.esAdmin() ? '/admin' : '/colaborador'));

  private idPersona = 0;

  protected readonly conteos = computed(() => {
    const all = this.items();
    const por = (fn: (n: BandejaNotificacion) => boolean): number => all.filter(fn).length;
    return {
      total: all.length,
      sinLeer: por((n) => !n.leido),
      leidas: por((n) => n.leido),
    };
  });

  protected readonly modulos = computed(() => {
    const mapa = new Map<string, { clave: string; visual: OrigenVisual; total: number }>();
    for (const n of this.items()) {
      const clave = claveModulo(n.origenModulo) || 'general';
      const actual = mapa.get(clave);
      if (actual) {
        actual.total++;
      } else {
        mapa.set(clave, { clave, visual: origenVisual(n.origenModulo), total: 1 });
      }
    }
    return [...mapa.values()].sort((a, b) => a.visual.etiqueta.localeCompare(b.visual.etiqueta));
  });

  protected readonly filtradas = computed(() => {
    const estado = this.filtroEstado();
    const modulo = this.filtroModulo();
    const desde = this.filtroDesde();
    const hasta = this.filtroHasta();
    const texto = this.filtroTexto().trim().toLowerCase();
    return this.items().filter((n) => {
      if (estado === 'no-leidas' && n.leido) return false;
      if (estado === 'leidas' && !n.leido) return false;
      if (modulo && claveModulo(n.origenModulo) !== modulo) return false;
      const dia = (n.fechaEnvio ?? '').slice(0, 10);
      if (desde && dia < desde) return false;
      if (hasta && dia > hasta) return false;
      if (texto) {
        const blob = [n.titulo, n.mensaje, origenVisual(n.origenModulo).etiqueta]
          .join(' ')
          .toLowerCase();
        if (!blob.includes(texto)) return false;
      }
      return true;
    });
  });

  protected readonly ordenadas = computed(() => {
    const arr = [...this.filtradas()];
    switch (this.orden()) {
      case 'modulo':
        arr.sort((a, b) =>
          origenVisual(a.origenModulo).etiqueta.localeCompare(
            origenVisual(b.origenModulo).etiqueta,
          ),
        );
        break;
      case 'estado':
        arr.sort((a, b) => Number(a.leido) - Number(b.leido));
        break;
      default:
        arr.sort((a, b) => (b.fechaEnvio ?? '').localeCompare(a.fechaEnvio ?? ''));
    }
    return arr;
  });

  ngOnInit(): void {
    this.textoCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((v) => this.filtroTexto.set(v ?? ''));

    this.loading.set(true);
    this.perfilSrv.obtener().subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.idPersona = p.idPersona;
        this.cargar();
      },
      error: () => {
        this.error.set('No se pudo identificar tu perfil.');
        this.loading.set(false);
      },
    });
  }

  cargar(): void {
    if (!this.idPersona) return;
    this.loading.set(true);
    this.error.set(null);
    this.notificaciones.misAlertas(this.idPersona).subscribe({
      next: (data) => {
        this.items.set(data ?? []);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar tus notificaciones.',
        );
        this.loading.set(false);
      },
    });
  }

  protected marcarLeida(n: BandejaNotificacion): void {
    if (n.leido) return;
    this.notificaciones.marcarLeida(n.idNotificacion).subscribe({
      next: () => this.aplicarLeida(n.idNotificacion),
      error: () => this.error.set('No se pudo marcar como leída.'),
    });
  }

  protected marcarTodas(): void {
    const pendientes = this.items().filter((n) => !n.leido);
    if (pendientes.length === 0) return;
    this.confirmar
      .confirmar(
        `¿Marcar ${pendientes.length} notificación(es) como leídas?`,
        'Marcar leídas',
      )
      .subscribe((ok) => {
        if (!ok) return;
        const marcadas = new Set<number>();
        let restantes = pendientes.length;
        const finalizar = (): void => {
          if (--restantes > 0) return;
          this.items.update((lista) =>
            lista.map((n) =>
              marcadas.has(n.idNotificacion) ? { ...n, leido: true } : n,
            ),
          );
        };
        for (const n of pendientes) {
          this.notificaciones.marcarLeida(n.idNotificacion).subscribe({
            next: () => {
              marcadas.add(n.idNotificacion);
              finalizar();
            },
            error: () => finalizar(),
          });
        }
      });
  }

  protected archivar(n: BandejaNotificacion): void {
    this.confirmar
      .confirmar(`¿Archivar la notificación "${n.titulo}"?`, 'Archivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.notificaciones.archivar(n.idNotificacion).subscribe({
          next: () =>
            this.items.update((lista) =>
              lista.filter((x) => x.idNotificacion !== n.idNotificacion),
            ),
          error: (err: HttpErrorResponse) =>
            this.error.set(err.error?.message ?? 'No se pudo archivar.'),
        });
      });
  }

  protected irAlOrigen(n: BandejaNotificacion): void {
    const destino = this.rutaOrigen(n);
    if (destino) this.router.navigateByUrl(destino);
  }

  protected rutaOrigen(n: BandejaNotificacion): string | null {
    return rutaOrigenNotificacion(n, this.esAdmin());
  }

  protected origen(n: BandejaNotificacion): OrigenVisual {
    return origenVisual(n.origenModulo);
  }

  protected canal(n: BandejaNotificacion): string {
    return etiquetaCanal(n.canal);
  }

  protected tiempo(n: BandejaNotificacion): string {
    return haceCuanto(n.fechaEnvio, Date.now());
  }

  protected filtrarEstado(valor: FiltroEstado): void {
    this.filtroEstado.set(valor);
  }

  protected filtrarModulo(valor: string): void {
    this.filtroModulo.set(valor);
  }

  protected setOrden(valor: Orden): void {
    this.orden.set(valor);
  }

  protected limpiarFiltros(): void {
    this.filtroEstado.set('todas');
    this.filtroModulo.set('');
    this.filtroDesde.set('');
    this.filtroHasta.set('');
    this.filtroTexto.set('');
    this.orden.set('fecha');
    this.textoCtrl.setValue('', { emitEvent: false });
  }

  private aplicarLeida(id: number): void {
    this.items.update((lista) =>
      lista.map((n) => (n.idNotificacion === id ? { ...n, leido: true } : n)),
    );
  }
}
