import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { LoadingBarService } from '../services/loading-bar-service';

/**
 * Alimenta la barra de carga global con cada petición HTTP: sube al iniciar y
 * completa al recibir respuesta o error.
 */
export const loadingBarInterceptor: HttpInterceptorFn = (req, next) => {
  const barra = inject(LoadingBarService);
  barra.iniciar();
  return next(req).pipe(finalize(() => barra.terminar()));
};
