import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiProgressBar } from '@taiga-ui/kit';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { MetaService } from '../meta-service';
import { CheckinModal } from './checkin-modal';
import { Meta, ProgresoMeta } from '../models';

@Component({
  selector: 'app-meta-detail',
  imports: [RouterLink, TuiBadge, TuiButton, TuiButtonLoading, TuiIcon, TuiProgressBar, TuiTable],
  templateUrl: './meta-detail.html',
  styleUrl: './meta-detail.css',
})
export class MetaDetail implements OnInit {
  private readonly service = inject(MetaService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly route = inject(ActivatedRoute);
  protected readonly router = inject(Router);

  protected readonly meta = signal<Meta | null>(null);
  protected readonly progreso = signal<ProgresoMeta | null>(null);
  protected readonly historial = signal<ProgresoMeta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  private id = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/metas']);
        return;
      }
      this.id = Number(idParam);
      this.meta.set(null);
      this.progreso.set(null);
      this.historial.set([]);
      this.cargar();
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.progreso(this.id).subscribe({
      next: (p) => {
        this.progreso.set(p);
        this.meta.set({
          id: p.idMeta,
          idPersona: p.idPersona,
          pilar: p.pilar,
          modalidad: p.modalidad,
          tipoOrigen: p.tipoOrigen,
          tipoCalculo: 'ACUMULADO',
          indicador: p.indicador,
          unidad: p.unidad,
          descripcion: p.descripcion,
          valorInicial: p.valorInicial,
          valorObjetivo: p.valorObjetivo,
          valorActual: p.valorActual,
          fechaInicio: p.fechaInicio,
          fechaObjetivo: p.fechaObjetivo,
          estado: p.estado,
        });
        this.loading.set(false);
        this.cargarHistorial();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 404 ? 'Meta no encontrada.' : 'No se pudo cargar la meta.');
        this.loading.set(false);
      },
    });
  }

  private cargarHistorial(): void {
    this.service.historial(this.id).subscribe({
      next: (h) => this.historial.set(h),
      error: () => undefined,
    });
  }

  registrarCheckin(): void {
    const p = this.progreso();
    if (!p) return;
    this.dialogs
      .open<ProgresoMeta | null>(new PolymorpheusComponent(CheckinModal), {
        label: 'Registrar check-in',
        data: { idMeta: p.idMeta, idPersona: p.idPersona, modalidad: p.modalidad },
      })
      .subscribe({
        next: (res) => {
          if (res) {
            this.progreso.set(res);
            this.cargarHistorial();
          }
        },
        complete: () => this.cargar(),
      });
  }

  protected aparienciaEstado(e?: string): string {
    switch (e) {
      case 'ACTIVA':
        return 'primary';
      case 'COMPLETADA':
        return 'positive';
      case 'DESVIADA':
        return 'warning';
      default:
        return 'neutral';
    }
  }
}
