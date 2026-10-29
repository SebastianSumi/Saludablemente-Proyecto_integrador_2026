import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { EMPTY, Subscription, catchError, interval, retry } from 'rxjs';
import { QrActividadService } from '../reporte-qr-actividad-service';
import { QrStreamService } from './qr-stream-service';
import { AgendaService } from '../../actividades/agenda-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { QrActividad } from '../models';

@Component({
  selector: 'app-qr-viewer',
  imports: [RouterLink, TuiBadge, TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './qr-viewer.html',
  styleUrl: './qr-viewer.css',
})
export class QrViewer implements OnInit, OnDestroy {
  private readonly qr = inject(QrActividadService);
  private readonly sse = inject(QrStreamService);
  private readonly agenda = inject(AgendaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);

  protected readonly idActividad = signal<number | null>(null);
  protected readonly actual = signal<QrActividad | null>(null);
  protected readonly estadoActividad = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly rotando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly finalizado = signal(false);
  /** Segundos restantes estimados para la rotación (countdown local). */
  protected readonly restante = signal<number | null>(null);
  /** true cuando llegan eventos SSE en vivo; si se pierde, el polling sostiene. */
  protected readonly enVivo = signal(false);

  private readonly recibidoEn = signal<number | null>(null);
  private readonly ultimoSse = signal<number | null>(null);
  private vivo: Subscription | null = null;
  private sseCtrl: AbortController | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('idActividad'));
    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('Actividad inválida.');
      return;
    }
    this.idActividad.set(id);
    this.cargar();
  }

  ngOnDestroy(): void {
    this.limpiarVivo();
  }

  cargar(): void {
    const id = this.idActividad();
    if (!id) return;
    this.loading.set(true);
    this.error.set(null);
    this.finalizado.set(false);
    // Restricción frontend: verificar estado antes de llamar al QR (evita 404/409).
    this.agenda.obtener(id).subscribe({
      next: (act) => {
        const estado = act?.estado ?? null;
        this.estadoActividad.set(estado);
        if (estado !== 'EN_CURSO') {
          this.actual.set(null);
          this.restante.set(null);
          this.limpiarVivo();
          this.loading.set(false);
          this.error.set(
            `QR no disponible: la actividad está ${estado ?? 'DESCONOCIDA'}. Solo puedes ver/rotar QR cuando está EN_CURSO.`
          );
          this.confirmar.advertirQrNoDisponible(estado).subscribe();
          return;
        }
        this.qr.actual(id).subscribe({
          next: (q) => {
            this.aplicarQr(q, false);
            this.loading.set(false);
            this.iniciarVivo(id);
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(
              err.status === 404
                ? 'No hay QR activo para esta actividad. Rota uno nuevo.'
                : 'No se pudo cargar el QR.'
            );
            this.loading.set(false);
            // Igual se inicia el vivo: el próximo tick del scheduler lo traerá.
            this.iniciarVivo(id);
          },
        });
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(err.status === 404 ? 'Actividad no encontrada.' : 'No se pudo cargar la actividad.');
      },
    });
  }

  rotar(): void {
    const id = this.idActividad();
    if (!id) return;
    const estadoConocido = this.estadoActividad();
    // Restricción frontend: modal y sin llamada QR si no está EN_CURSO.
    if (estadoConocido !== null && estadoConocido !== 'EN_CURSO') {
      this.error.set(
        `QR no disponible: la actividad está ${estadoConocido}. Solo puedes ver/rotar QR cuando está EN_CURSO.`
      );
      this.confirmar.advertirQrNoDisponible(estadoConocido).subscribe();
      return;
    }
    if (estadoConocido === null) {
      // Estado aún no verificado: validar vía agenda antes de tocar el QR.
      this.agenda.obtener(id).subscribe({
        next: (act) => {
          const estado = act?.estado ?? null;
          this.estadoActividad.set(estado);
          if (estado !== 'EN_CURSO') {
            this.error.set(
              `QR no disponible: la actividad está ${estado ?? 'DESCONOCIDA'}. Solo puedes ver/rotar QR cuando está EN_CURSO.`
            );
            this.confirmar.advertirQrNoDisponible(estado).subscribe();
            return;
          }
          this.confirmarRotacion(id);
        },
        error: () => {
          this.error.set('No se pudo verificar el estado de la actividad.');
        },
      });
      return;
    }
    this.confirmarRotacion(id);
  }

  private confirmarRotacion(id: number): void {
    this.confirmar
      .confirmar('¿Rotar el código QR? El anterior quedará inválido.', 'Rotar')
      .subscribe((ok) => {
        if (!ok) return;
        this.rotando.set(true);
        this.qr.rotar(id).subscribe({
          next: (q) => {
            this.aplicarQr(q, false);
            this.rotando.set(false);
          },
          error: () => {
            this.error.set('No se pudo rotar el QR.');
            this.rotando.set(false);
          },
        });
      });
  }

  private aplicarQr(q: QrActividad, porSse: boolean): void {
    this.actual.set(q);
    this.recibidoEn.set(Date.now());
    this.restante.set(q.expiraEnSegundos ?? null);
    if (porSse) {
      this.ultimoSse.set(Date.now());
      this.enVivo.set(true);
      this.error.set(null);
    }
  }

  /** SSE en vivo + polling de respaldo cada 30 s + countdown local cada 1 s. */
  private iniciarVivo(id: number): void {
    this.limpiarVivo();
    this.sseCtrl = new AbortController();
    const vivo = new Subscription();

    vivo.add(
      this.sse
        .stream(id, this.sseCtrl.signal)
        .pipe(
          retry({ delay: 10000 }),
          catchError(() => EMPTY)
        )
        .subscribe({
          next: (q) => this.aplicarQr(q, true),
          complete: () => {
            // El backend cierra el stream al finalizar la actividad.
            this.finalizado.set(true);
            this.limpiarVivo();
          },
        })
    );

    vivo.add(
      interval(30000).subscribe(() => {
        const ultimo = this.ultimoSse();
        if (ultimo != null && Date.now() - ultimo < 45000) return;
        // Sin eventos recientes: polling de respaldo.
        this.enVivo.set(false);
        this.qr.actual(id).subscribe({
          next: (q) => this.aplicarQr(q, false),
          error: () => undefined,
        });
      })
    );

    vivo.add(
      interval(1000).subscribe(() => {
        const r = this.recibidoEn();
        const a = this.actual();
        if (r == null || a?.expiraEnSegundos == null) {
          this.restante.set(null);
          return;
        }
        this.restante.set(Math.max(0, a.expiraEnSegundos - Math.floor((Date.now() - r) / 1000)));
      })
    );

    this.vivo = vivo;
  }

  private limpiarVivo(): void {
    this.vivo?.unsubscribe();
    this.vivo = null;
    this.sseCtrl?.abort();
    this.sseCtrl = null;
    this.enVivo.set(false);
  }

  protected formatoRestante(): string {
    const r = this.restante();
    if (r == null) return '—';
    if (r === 0) return 'Rotando…';
    return `${Math.floor(r / 60)}:${String(r % 60).padStart(2, '0')}`;
  }
}
