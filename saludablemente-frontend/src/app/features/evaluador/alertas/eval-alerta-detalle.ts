import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import { AlertaClinica } from '../../admin/alertaclinica/alerta/alerta-clinica.model';
import { PerfilService } from '../../admin/perfil/perfil-service';

@Component({
  selector: 'app-eval-alerta-detalle',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiIcon,
    TuiLabel,
    TuiTextarea,
    TuiTextfield,
  ],
  templateUrl: './eval-alerta-detalle.html',
  styleUrls: ['../evaluador-comun.css', '../evaluador-tablas-detalle.css'],
})
export class EvalAlertaDetalle implements OnInit {
  private readonly service = inject(AlertaClinicaService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly route = inject(ActivatedRoute);

  protected readonly alerta = signal<AlertaClinica | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly observacion = signal<string>('');
  protected readonly atenderLoading = signal(false);
  protected readonly atenderError = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);

  private idAlerta = 0;
  private idUsuarioActual: number | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;
    this.idAlerta = id;
    // El id de usuario se resuelve de la sesión (JWT propio) vía /mi-perfil.
    this.perfilSrv.obtener().subscribe({
      next: (p) => (this.idUsuarioActual = p.idUsuario),
      error: () => undefined,
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.obtener(this.idAlerta).subscribe({
      next: (data) => {
        this.alerta.set(data);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404 ? 'Alerta no encontrada.' : 'No se pudo cargar la alerta.'
        );
        this.loading.set(false);
      },
    });
  }

  atender(): void {
    const actual = this.alerta();
    if (!actual || actual.estado !== 'PENDIENTE' || this.atenderLoading()) return;

    if (this.idUsuarioActual == null) {
      this.perfilSrv.obtener().subscribe({
        next: (p) => {
          this.idUsuarioActual = p.idUsuario;
          this.ejecutarAtender(actual.idAlerta, p.idUsuario);
        },
        error: () => this.atenderError.set('No se pudo identificar tu usuario. Reintenta.'),
      });
      return;
    }
    this.ejecutarAtender(actual.idAlerta, this.idUsuarioActual);
  }

  private ejecutarAtender(idAlerta: number, idUsuario: number): void {
    this.atenderLoading.set(true);
    this.atenderError.set(null);
    this.mensajeExito.set(null);
    this.service
      .atender(idAlerta, { idUsuario, observacion: this.observacion().trim() })
      .subscribe({
        next: (actualizada) => {
          this.alerta.set(actualizada);
          this.observacion.set('');
          this.mensajeExito.set('Alerta atendida correctamente.');
          this.atenderLoading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.atenderError.set(err.error?.message ?? 'No se pudo atender la alerta.');
          this.atenderLoading.set(false);
        },
      });
  }

  protected pillSeveridad(sev: string): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-secondary';
  }

  protected pillEstado(estado: string): string {
    if (estado === 'ATENDIDA') return 'pill pill-success';
    if (estado === 'ANULADA') return 'pill pill-neutral';
    return 'pill pill-warning';
  }
}
