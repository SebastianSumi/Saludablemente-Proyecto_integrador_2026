import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { MetaService } from '../../admin/metas/meta-service';
import { CheckinModal } from '../../admin/metas/meta/checkin-modal';
import { ProgresoMeta } from '../../admin/metas/models';

@Component({
  selector: 'app-eval-meta-detalle',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiTable],
  templateUrl: './eval-meta-detalle.html',
  styleUrls: ['../evaluador-comun.css'],
})
export class EvalMetaDetalle implements OnInit {
  private readonly service = inject(MetaService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly progreso = signal<ProgresoMeta | null>(null);
  protected readonly historial = signal<ProgresoMeta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  private id = 0;

  ngOnInit(): void {
    const idParam = Number(this.route.snapshot.paramMap.get('id'));
    if (!idParam) {
      this.router.navigate(['/evaluador/metas']);
      return;
    }
    this.id = idParam;
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.progreso(this.id).subscribe({
      next: (p) => {
        this.progreso.set(p);
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

  protected pillEstado(e?: string): string {
    switch (e) {
      case 'ACTIVA':
        return 'pill pill-primary';
      case 'COMPLETADA':
        return 'pill pill-success';
      case 'DESVIADA':
        return 'pill pill-warning';
      default:
        return 'pill pill-neutral';
    }
  }
}
