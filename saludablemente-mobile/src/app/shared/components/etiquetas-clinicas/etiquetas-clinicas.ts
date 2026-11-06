import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import type { EtiquetaClinicaAsignada } from '../../models/etiqueta-clinica-asignada';

/** Presentacion sin acciones ni consultas al catalogo administrativo. */
@Component({
  selector: 'app-etiquetas-clinicas', standalone: true, imports: [DatePipe, RouterLink],
  template: `
    @if (etiquetas().length) {
      <ul class="etiquetas" [class.detalladas]="detallado()" aria-label="Etiquetas clínicas de la alerta">
        @for (e of etiquetas(); track e.idEtiqueta) {
          <li class="etiqueta" [class.detalle]="detallado()">
            <div class="nombre"><span class="color" [style.background]="color(e.colorHex)" aria-hidden="true"></span>@if (rutaBase()) { <a [routerLink]="[rutaBase(), e.idEtiqueta]">{{ e.nombre }}</a> } @else { <span>{{ e.nombre }}</span> }</div>
            @if (detallado()) {
              <div class="informacion"><span class="origen">{{ e.asignadaPor === 'MOTOR' ? 'Automática' : 'Asignada por el equipo' }}</span>
                @if (e.fechaAsignacion) { <span>Asignada el {{ e.fechaAsignacion | date:'dd/MM/yyyy HH:mm' }}</span> }
              </div>
            }
          </li>
        }
      </ul>
    } @else {
      <p class="sin-etiquetas">{{ detallado() ? 'Esta alerta todavía no tiene etiquetas clínicas asignadas.' : 'Sin etiquetas' }}</p>
    }
  `,
  styles: [`:host{display:block;min-width:0}.etiquetas{display:flex;gap:.4rem;flex-wrap:wrap;list-style:none;padding:0;margin:0}.etiqueta{max-width:100%;box-sizing:border-box;border:1px solid var(--sal-line);border-radius:999px;background:var(--sal-hover);padding:.3rem .65rem;color:var(--sal-ink);font-size:.78rem;line-height:1.5}.nombre{display:flex;align-items:center;gap:.5rem;font-weight:600;min-width:0}.nombre>:last-child{overflow-wrap:anywhere;min-width:0}.nombre a{color:inherit;text-decoration:none}.nombre a:hover{text-decoration:underline}.nombre a:focus-visible{outline:2px solid var(--sal-action);outline-offset:3px}.color{width:.65rem;height:.65rem;border-radius:50%;flex:0 0 .65rem;border:1px solid var(--sal-line)}.detalladas{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,250px),1fr));gap:.8rem}.detalle{border-radius:16px;padding:1rem;background:var(--sal-card);font-size:.9rem}.informacion{display:flex;gap:.6rem;flex-wrap:wrap;align-items:center;margin-top:.7rem;color:var(--sal-ink-soft);font-size:.8rem;line-height:1.6}.origen{background:var(--sal-hover);border:1px solid var(--sal-line);border-radius:999px;padding:.2rem .6rem;color:var(--sal-ink)}.sin-etiquetas{margin:.2rem 0;color:var(--sal-ink-soft);font-size:.85rem;line-height:1.6}`],
})
export class EtiquetasClinicas {
  readonly etiquetas = input<readonly EtiquetaClinicaAsignada[]>([]);
  readonly detallado = input(false);
  readonly rutaBase = input('');
  color(value: string): string { return /^#[0-9a-f]{6}$/i.test(value || '') ? value : 'var(--sal-primary)'; }
}

