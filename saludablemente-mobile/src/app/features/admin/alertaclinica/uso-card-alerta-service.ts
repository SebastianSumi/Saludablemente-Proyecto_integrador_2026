import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';

/**
 * Frecuencia de uso de los cards del menú de alertas clínicas (por usuario).
 * El backend resuelve el usuario desde el token.
 */
@Injectable({ providedIn: 'root' })
export class UsoCardAlertaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/alertaclinica/uso-cards';

  obtener(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(this.api.buildUrl(this.resource));
  }

  registrar(ruta: string): Observable<void> {
    return this.http.post<void>(this.api.buildUrl(this.resource), { ruta });
  }

  reiniciar(): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(this.resource));
  }
}
