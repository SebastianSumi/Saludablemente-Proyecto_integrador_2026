import { Component, computed, input, output } from '@angular/core';
import { CdkDrag, CdkDropList } from '@angular/cdk/drag-drop';
import type { CdkDragDrop } from '@angular/cdk/drag-drop';
import { TuiAvatar } from '@taiga-ui/kit';
import { TuiIcon } from '@taiga-ui/core';
import { FilaMarca, metaEstado, siguienteMarcaDe } from './estado-marca';
import type { MarcaAsistencia } from '../../features/admin/asistencias/models';

interface Columna {
  id: string;
  titulo: string;
  icono: string;
  marca: MarcaAsistencia | null;
  items: FilaMarca[];
  arrastrable: boolean;
}

/**
 * Tablero kanban: tap cicla (bucle) y drag-drop mueve directo a la columna.
 * Las listas son derivadas (solo lectura); el padre aplica el cambio.
 */
@Component({
  selector: 'app-kanban-board',
  imports: [CdkDrag, CdkDropList, TuiAvatar, TuiIcon],
  templateUrl: './kanban-board.html',
  styleUrl: './kanban-board.css',
})
export class KanbanBoard {
  readonly filas = input.required<FilaMarca[]>();
  readonly deshabilitado = input(false);
  readonly ciclar = output<number>();
  readonly marcarA = output<{ idPersona: number; marca: MarcaAsistencia }>();

  protected readonly columnas = computed<Columna[]>(() => {
    const filas = this.filas();
    const en = (pred: (f: FilaMarca) => boolean) => filas.filter(pred);
    return [
      {
        id: 'pendientes',
        titulo: 'Pendientes',
        icono: '@tui.hourglass',
        marca: 'PRESENTE',
        items: en((f) => f.visible == null),
        arrastrable: false,
      },
      {
        id: 'presentes',
        titulo: 'Presentes',
        icono: '@tui.user-check',
        marca: 'PRESENTE',
        items: en((f) => f.visible === 'ASISTIO'),
        arrastrable: true,
      },
      {
        id: 'tarde',
        titulo: 'Tarde',
        icono: '@tui.clock',
        marca: 'TARDE',
        items: en((f) => f.visible === 'TARDE'),
        arrastrable: true,
      },
      {
        id: 'falta',
        titulo: 'Falta',
        icono: '@tui.user-x',
        marca: 'FALTA',
        items: en((f) => f.visible === 'AUSENTE'),
        arrastrable: true,
      },
      {
        id: 'otros',
        titulo: 'Otros',
        icono: '@tui.tag',
        marca: null,
        items: en(
          (f) => f.visible !== null && f.visible !== 'ASISTIO' && f.visible !== 'TARDE' && f.visible !== 'AUSENTE'
        ),
        arrastrable: false,
      },
    ];
  });

  protected readonly idsConectados = computed(() =>
    this.columnas()
      .filter((c) => c.arrastrable)
      .map((c) => c.id)
  );

  protected metaDe = metaEstado;

  protected tocar(f: FilaMarca): void {
    if (this.deshabilitado() || !siguienteMarcaDe(f.visible)) return;
    this.ciclar.emit(f.idPersona);
  }

  protected soltar(event: CdkDragDrop<FilaMarca[]>, marca: MarcaAsistencia | null): void {
    if (this.deshabilitado() || marca == null) return;
    if (event.previousContainer === event.container) return;
    const f = event.item.data as FilaMarca;
    if (!f) return;
    this.marcarA.emit({ idPersona: f.idPersona, marca });
  }
}
