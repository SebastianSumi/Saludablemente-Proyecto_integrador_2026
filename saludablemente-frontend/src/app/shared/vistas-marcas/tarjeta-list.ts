import { Component, input, output } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiAvatar } from '@taiga-ui/kit';
import { FilaMarca } from './estado-marca';
import type { MarcaAsistencia } from '../../features/admin/asistencias/models';

/** Vista tarjetas: card por persona con segmento directo Presente|Tarde|Falta. */
@Component({
  selector: 'app-tarjeta-list',
  imports: [TuiAvatar, TuiButton, TuiIcon],
  templateUrl: './tarjeta-list.html',
  styleUrl: './tarjeta-list.css',
})
export class TarjetaList {
  readonly filas = input.required<FilaMarca[]>();
  readonly deshabilitado = input(false);
  readonly marcarA = output<{ idPersona: number; marca: MarcaAsistencia }>();

  protected elegir(f: FilaMarca, marca: MarcaAsistencia): void {
    if (this.deshabilitado()) return;
    this.marcarA.emit({ idPersona: f.idPersona, marca });
  }

  protected esMarca(f: FilaMarca, marca: MarcaAsistencia): boolean {
    if (f.visible == null) return false;
    if (marca === 'PRESENTE') return f.visible === 'ASISTIO';
    if (marca === 'TARDE') return f.visible === 'TARDE';
    return f.visible === 'AUSENTE';
  }
}
