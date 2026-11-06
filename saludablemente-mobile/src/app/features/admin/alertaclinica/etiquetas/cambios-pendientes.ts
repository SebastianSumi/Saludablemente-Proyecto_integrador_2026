import { Directive, HostListener, inject } from '@angular/core';
import { CanDeactivateFn } from '@angular/router';
import { ConfirmService } from '../../../../core/services/confirm-service';

@Directive()
export abstract class PaginaConCambios {
  abstract cambiosPendientes(): boolean;
  operacionEnCurso(): boolean { return false; }
  @HostListener('window:beforeunload', ['$event'])
  protegerSalida(event: BeforeUnloadEvent): void {
    if (this.cambiosPendientes() || this.operacionEnCurso()) { event.preventDefault(); event.returnValue = ''; }
  }
}
export const cambiosClinicosGuard: CanDeactivateFn<PaginaConCambios> = component => {
  if (component.operacionEnCurso()) return false;
  return component.cambiosPendientes() ? inject(ConfirmService).confirmar('Tienes cambios sin guardar. ¿Quieres descartarlos y salir?', 'Descartar y salir') : true;
};
