import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, take } from 'rxjs';
import { AsociacionesCard } from '../etiquetas/asociaciones-card';
import { PaginaConCambios } from '../etiquetas/cambios-pendientes';
import { ClinicaNav } from '../etiquetas/clinica-nav';
import { Component, DestroyRef, OnInit, inject, signal, viewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading, TuiTextarea } from '@taiga-ui/kit';
import { AlertaClinicaService } from './alerta-clinica-service';
import { AlertaClinica } from './alerta-clinica.model';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PerfilService } from '../../perfil/perfil-service';
import { PersonaNombreService } from './persona-nombre-service';

import { imagenPortada } from '../ilustracion-alerta';

@Component({
  selector: 'app-alerta-clinica-detail',
  standalone: true,
  imports: [AsociacionesCard, ClinicaNav, CommonModule, RouterLink, FormsModule, TuiButton, TuiButtonLoading, TuiBadge, TuiIcon, TuiLabel, TuiTextfield, TuiTextarea],
  templateUrl: './alerta-clinica-detail.html',
  styleUrls: ['./alerta-clinica-detail.css'],
})

export class AlertaClinicaDetail extends PaginaConCambios implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly tarjetas = viewChildren(AsociacionesCard);
  readonly anulando = signal(false);
  cambiosPendientes(): boolean { return this.tarjetas().some(c => c.draft.pendiente()); }
  override operacionEnCurso(): boolean { return this.atenderLoading() || this.anulando() || this.tarjetas().some(c => c.ocupado()); }
  actualizarEtiquetas(): void {
    this.service.obtener(this.idAlerta()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: a => this.alerta.set(a), error: () => undefined,
    });
  }
  private puedeCambiarEstado(): boolean {
    if (this.cambiosPendientes() || this.operacionEnCurso()) {
      this.atenderError.set('Guarda o cancela los cambios de etiquetas antes de cambiar el estado de la alerta.');
      return false;
    }
    return true;
  }
  protected readonly portada = imagenPortada();
  private readonly service = inject(AlertaClinicaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly nombresSrv = inject(PersonaNombreService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private idUsuarioActual: number | null = null;

  protected readonly alerta = signal<AlertaClinica | null>(null);
  protected readonly nombres = signal<Map<number, string>>(new Map());
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly observacion = signal<string>('');
  protected readonly atenderLoading = signal(false);
  protected readonly atenderError = signal<string | null>(null);

  private readonly idAlerta = signal<number>(0);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.idAlerta.set(id);
    this.cargar(id);
  }

  protected nombrePersona(idPersona: number): string {
    return this.nombresSrv.nombreDe(this.nombres(), idPersona);
  }

  cargar(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.nombresSrv.nombres().subscribe({
      next: (m) => this.nombres.set(m),
      error: () => undefined,
    });

    this.service.obtener(id).subscribe({
      next: (data) => this.alerta.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.mapearError(err));
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  atender(): void {
    if (!this.puedeCambiarEstado()) return;
    const actual = this.alerta();
    if (!actual || actual.estado !== 'PENDIENTE' || this.atenderLoading()) return;

    // El id del usuario que atiende se resuelve de /mi-perfil (sesión JWT),
    // no de localStorage: ahí nunca se guardaba y por eso fallaba siempre.
    if (this.idUsuarioActual == null) {
      this.atenderLoading.set(true);
      this.atenderError.set(null);
      this.perfilSrv.obtener().subscribe({
        next: (p) => {
          this.idUsuarioActual = p.idUsuario;
          this.ejecutarAtender(actual.idAlerta, p.idUsuario);
        },
        error: () => {
          this.atenderError.set('No se pudo identificar tu usuario. Inténtalo de nuevo.');
          this.atenderLoading.set(false);
        },
      });
      return;
    }

    this.atenderLoading.set(true);
    this.atenderError.set(null);
    this.ejecutarAtender(actual.idAlerta, this.idUsuarioActual);
  }

  private ejecutarAtender(idAlerta: number, idUsuario: number): void {
    this.service
      .atender(idAlerta, {
        idUsuario,
        observacion: this.observacion(),
      })
      .subscribe({
        next: (actualizada) => {
          this.alerta.set(actualizada);
          this.observacion.set('');
          this.atenderLoading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.atenderError.set(this.mapearError(err));
          this.atenderLoading.set(false);
        },
      });
  }

  anular(): void {
    const actual = this.alerta();
    if (!actual || actual.estado === 'ANULADA' || !this.puedeCambiarEstado()) return;
    this.anulando.set(true);
    this.confirmar.confirmar(`¿Anular la alerta #${actual.idAlerta}?`, 'Anular')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe({
        next: ok => {
          if (!ok) { this.anulando.set(false); return; }
          this.service.anular(actual.idAlerta).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.anulando.set(false))).subscribe({
            next: actualizada => this.alerta.set(actualizada),
            error: (err: HttpErrorResponse) => this.atenderError.set(this.mapearError(err)),
          });
        }, error: () => this.anulando.set(false),
      });
  }

  volver(): void {
    this.router.navigate(['/alertaclinica/alertas']);
  }

  private mapearError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
    if (err.status === 404) return err.error?.message ?? 'Recurso no encontrado.';
    if (err.status === 409) return err.error?.message ?? 'Regla de negocio violada.';
    if (err.status === 400) return err.error?.message ?? 'Datos invalidos.';
    if (err.status === 500) return 'Error interno del servidor.';
    return err.error?.message ?? 'Operacion fallida.';
  }

  protected severidadClass(sev: string): string {
    switch (sev) {
      case 'CRITICA': return 'negative';
      case 'MODERADA': return 'neutral';
      case 'LEVE': return 'positive';
      default: return 'primary';
    }
  }

  protected estadoClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'primary';
      case 'ATENDIDA': return 'positive';
      case 'ANULADA': return 'negative';
      default: return 'primary';
    }
  }
}
