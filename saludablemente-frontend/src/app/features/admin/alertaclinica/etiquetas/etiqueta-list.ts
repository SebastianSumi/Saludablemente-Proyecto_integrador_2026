import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, finalize, take } from 'rxjs';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { EtiquetaResumen } from './etiqueta.model';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-etiqueta-list', standalone: true,
  imports: [FormsModule, RouterLink, TuiButton, TuiIcon, ClinicaNav], templateUrl: './etiqueta-list.html', styleUrls: ['./clinica.css'] })
export class EtiquetaList implements OnInit {
  private readonly service = inject(EtiquetaClinicaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly destroyRef = inject(DestroyRef);
  readonly items = signal<EtiquetaResumen[]>([]);
  readonly cargando = signal(false); readonly ocupado = signal(false); readonly error = signal(''); readonly exito = signal('');
  readonly pagina = signal(0); readonly total = signal(0); readonly paginas = signal(0);
  texto = ''; estado = ''; private busqueda?: Subscription;
  ngOnInit(): void { this.cargar(); }
  cargar(pagina = 0): void {
    this.busqueda?.unsubscribe(); this.cargando.set(true); this.error.set('');
    this.busqueda = this.service.consultar({ texto: this.texto, estado: this.estado, pagina, tamanio: 12 })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({
        next: page => { this.items.set(page.content); this.pagina.set(page.number); this.total.set(page.totalElements); this.paginas.set(page.totalPages); },
        error: err => this.error.set(mensajeClinico(err)),
      });
  }
  limpiar(): void { this.texto = ''; this.estado = ''; this.cargar(); }
  cambiarEstado(etiqueta: EtiquetaResumen): void {
    if (this.ocupado()) return;
    const activar = etiqueta.estado === 'INACTIVO';
    this.ocupado.set(true);
    this.confirmar.confirmar(`${activar ? 'Reactivar' : 'Desactivar'} «${etiqueta.nombre}» (${etiqueta.codigo}). ${activar ? 'Volverá a estar disponible para nuevas asociaciones.' : 'Se conservarán sus asociaciones históricas y dejará de asignarse a nuevas alertas.'}`, activar ? 'Reactivar' : 'Desactivar')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe({
        next: ok => {
          if (!ok) { this.ocupado.set(false); return; }
          this.service.estado(etiqueta.id, activar).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupado.set(false))).subscribe({
            next: () => { this.exito.set(`Etiqueta ${activar ? 'reactivada' : 'desactivada'} correctamente.`); this.cargar(this.items().length === 1 ? 0 : this.pagina()); },
            error: err => this.error.set(mensajeClinico(err)),
          });
        }, error: err => { this.error.set(mensajeClinico(err)); this.ocupado.set(false); },
      });
  }
  color(value: string): string { return /^#[0-9a-f]{6}$/i.test(value) ? value : 'var(--sal-primary)'; }
}
