import { Component, DestroyRef, OnInit, inject, signal, viewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, take } from 'rxjs';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { EtiquetaClinica } from './etiqueta.model';
import { AsociacionesCard } from './asociaciones-card';
import { PaginaConCambios } from './cambios-pendientes';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-etiqueta-detail', standalone: true,
  imports: [CommonModule, RouterLink, TuiButton, TuiIcon, ClinicaNav, AsociacionesCard],
  templateUrl: './etiqueta-detail.html', styleUrls: ['./clinica.css'] })
export class EtiquetaDetail extends PaginaConCambios implements OnInit {
  private readonly service = inject(EtiquetaClinicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly confirmar = inject(ConfirmService);
  private readonly destroyRef = inject(DestroyRef);
  readonly tarjetas = viewChildren(AsociacionesCard);
  readonly etiqueta = signal<EtiquetaClinica | null>(null);
  readonly cargando = signal(true); readonly ocupado = signal(false); readonly error = signal(''); readonly exito = signal('');
  readonly id = Number(this.route.snapshot.paramMap.get('id'));
  ngOnInit(): void { this.cargar(); }
  cambiosPendientes(): boolean { return this.tarjetas().some(c => c.draft.pendiente()); }
  override operacionEnCurso(): boolean { return this.ocupado() || this.tarjetas().some(c => c.ocupado()); }
  cargar(): void {
    this.cargando.set(true); this.error.set('');
    this.service.obtener(this.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({ next: e => this.etiqueta.set(e), error: err => this.error.set(mensajeClinico(err)) });
  }
  cambiarEstado(): void {
    const e = this.etiqueta(); if (!e || this.operacionEnCurso()) return;
    if (this.cambiosPendientes()) { this.error.set('Guarda o cancela los cambios de las tarjetas antes de cambiar el estado.'); return; }
    const activar = e.estado === 'INACTIVO'; this.ocupado.set(true); this.error.set('');
    this.confirmar.confirmar(`${activar ? 'Reactivar' : 'Desactivar'} «${e.nombre}». ${activar ? 'Volverá a estar disponible para nuevas asociaciones.' : 'Se conservarán las asociaciones históricas.'}`, activar ? 'Reactivar' : 'Desactivar')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe({
        next: ok => {
          if (!ok) { this.ocupado.set(false); return; }
          this.service.estado(e.id, activar).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupado.set(false))).subscribe({
            next: () => { this.exito.set('Estado actualizado correctamente.'); this.cargar(); }, error: err => this.error.set(mensajeClinico(err)),
          });
        }, error: err => { this.ocupado.set(false); this.error.set(mensajeClinico(err)); },
      });
  }
  color(value: string): string { return /^#[0-9a-f]{6}$/i.test(value) ? value : 'var(--sal-primary)'; }
}
