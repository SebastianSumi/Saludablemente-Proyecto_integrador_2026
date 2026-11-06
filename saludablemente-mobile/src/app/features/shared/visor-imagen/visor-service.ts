import { Injectable, inject } from '@angular/core';
import { TuiDialogService } from '@taiga-ui/core';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { VisorImagen } from './visor-imagen';
import { VisorImagenItem } from './visor-model';

@Injectable({ providedIn: 'root' })
export class VisorImagenService {
  private readonly dialogs = inject(TuiDialogService);

  abrir(imagenes: VisorImagenItem[], indice = 0): void {
    const lista = (imagenes ?? []).filter((i) => !!i?.src);
    if (lista.length === 0) return;
    this.dialogs
      .open<void>(new PolymorpheusComponent(VisorImagen), {
        label: '',
        data: { imagenes: lista, indice },
        size: 'l',
        dismissible: true,
      })
      .subscribe();
  }
}
