import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { EtiquetasClinicas } from '../../../shared/components/etiquetas-clinicas/etiquetas-clinicas';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { ColAlertaService } from './alerta-service';
import { imagenPortada } from '../../admin/alertaclinica/ilustracion-alerta';
import type {
  AlertaClinica,
  EstadoAlerta,
  SeveridadAlerta,
  TipoEvaluacionOrigen,
} from './alerta.model';

@Component({
  selector: 'app-col-alerta-detail',
  standalone: true,
  imports: [EtiquetasClinicas,DatePipe, RouterLink, TuiButton, TuiIcon],
  templateUrl: './alerta-detail.html',
  styleUrls: ['./alerta-detail.css'],
})
export class ColAlertaDetail implements OnInit {
  private readonly service = inject(ColAlertaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  reintentar(): void { this.cargar(Number(this.route.snapshot.paramMap.get('id'))); }
  protected readonly portada = imagenPortada();
  protected readonly alerta = signal<AlertaClinica | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const id = Number(params.get('id'));
      if (!Number.isSafeInteger(id) || id <= 0) {
        this.consulta?.unsubscribe(); this.alerta.set(null); this.loading.set(false);
        this.error.set('Identificador inválido.'); return;
      }
      this.cargar(id);
    });
  }

  cargar(id: number): void {
    this.consulta?.unsubscribe();
    this.alerta.set(null);
    this.loading.set(true);
    this.error.set(null);
    this.consulta = this.service.obtenerMio(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.alerta.set(data),
      error: (err: HttpErrorResponse | Error) => {
        this.error.set(
          err instanceof Error && !(err instanceof HttpErrorResponse)
            ? err.message
            : err instanceof HttpErrorResponse && err.status === 404 ? 'No encontramos esta alerta entre tus alertas.'
              : err instanceof HttpErrorResponse && err.status === 403 ? 'No tienes acceso a esta alerta.'
              : 'No se pudo cargar tu alerta. Inténtalo nuevamente.',
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected volver(): void {
    this.router.navigate(['/colaborador/mis-alertas']);
  }

  protected etiquetaSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA' ? 'Crítica' : sev === 'MODERADA' ? 'Moderada' : 'Leve';
  }

  protected pillSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-success';
  }

  protected iconoSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? '@tui.triangle-alert'
      : sev === 'MODERADA'
        ? '@tui.info'
        : '@tui.check';
  }

  protected etiquetaEstado(estado: EstadoAlerta): string {
    return estado === 'PENDIENTE' ? 'Pendiente' : estado === 'ATENDIDA' ? 'Atendida' : 'Anulada';
  }

  protected pillEstado(estado: EstadoAlerta): string {
    return estado === 'PENDIENTE'
      ? 'pill pill-warning'
      : estado === 'ATENDIDA'
        ? 'pill pill-success'
        : 'pill pill-neutral';
  }

  protected etiquetaOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? 'Nutricional' : 'Aptitud física';
  }

  protected iconoOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? '@tui.apple' : '@tui.activity';
  }

  protected pillOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? 'pill pill-success' : 'pill pill-violet';
  }

  protected rutaEvaluacion(a: AlertaClinica): string[] {
    const base = a.tipoEvaluacionOrigen === 'NUTRICIONAL' ? 'nutricional' : 'aptitud';
    return ['/colaborador/evaluaciones', base, String(a.evaluacionOrigenId)];
  }

  protected rango(valor: number | null): string {
    return valor === null ? '—' : String(valor);
  }
}
