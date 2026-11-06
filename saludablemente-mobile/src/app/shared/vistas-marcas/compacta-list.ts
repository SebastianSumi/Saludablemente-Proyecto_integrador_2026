import { Component, input, output } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiAvatar } from '@taiga-ui/kit';
import {
  FilaMarca,
  etiquetaMarca,
  iconoAccion,
  metaEstado,
  siguienteMarcaDe,
} from './estado-marca';

/** Vista compacta: filas densas con botón circular de estado (un toque = siguiente). */
@Component({
  selector: 'app-compacta-list',
  imports: [TuiAvatar, TuiButton, TuiIcon],
  templateUrl: './compacta-list.html',
  styleUrl: './compacta-list.css',
})
export class CompactaList {
  readonly filas = input.required<FilaMarca[]>();
  readonly deshabilitado = input(false);
  readonly ciclar = output<number>();

  protected metaDe = metaEstado;
  protected siguienteMarcaDe = siguienteMarcaDe;
  protected etiquetaSiguiente = (f: FilaMarca): string => {
    const s = siguienteMarcaDe(f.visible);
    return s == null ? '—' : f.visible == null ? 'Marcar' : etiquetaMarca(s);
  };

  protected accionar(f: FilaMarca): void {
    if (this.deshabilitado() || !siguienteMarcaDe(f.visible)) return;
    this.ciclar.emit(f.idPersona);
  }

  protected iconoBoton = iconoAccion;
}
