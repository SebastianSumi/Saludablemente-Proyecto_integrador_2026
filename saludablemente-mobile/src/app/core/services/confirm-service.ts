import { Injectable, inject } from '@angular/core';
import { TuiDialogService } from '@taiga-ui/core';
import { TUI_CONFIRM } from '@taiga-ui/kit';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly dialogs = inject(TuiDialogService);

  confirmar(mensaje: string, botonSi = 'Confirmar'): Observable<boolean> {
    return this.dialogs.open<boolean>(TUI_CONFIRM, {
      data: { content: mensaje, yes: botonSi, no: 'Cancelar' },
    });
  }

  /** Modal informativo de una sola acción (no bloquea con error en consola). */
  advertir(mensaje: string, titulo = 'Acción no disponible'): Observable<boolean> {
    return this.dialogs.open<boolean>(TUI_CONFIRM, {
      label: titulo,
      data: { content: mensaje, yes: 'Entendido', no: 'Cerrar' },
    });
  }

  /** Advertencia estándar para QR: no llamar al backend si no está EN_CURSO. */
  advertirQrNoDisponible(estado: string | null | undefined): Observable<boolean> {
    const e = estado ?? 'DESCONOCIDA';
    return this.advertir(
      `La actividad está ${e}. Solo puedes ver/rotar QR cuando está EN_CURSO.`,
      'QR no disponible'
    );
  }
}
