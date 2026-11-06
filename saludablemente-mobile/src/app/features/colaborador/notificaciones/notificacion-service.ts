import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { BandejaNotificacion } from './models';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/notificaciones';

  misAlertas(idPersona: number): Observable<BandejaNotificacion[]> {
    return this.http.get<BandejaNotificacion[]>(
      this.api.buildUrl(`${this.resource}/mis-alertas`),
      { params: new HttpParams().set('idPersona', String(idPersona)) }
    );
  }

  contadorNoLeidas(idPersona: number): Observable<number> {
    return this.http.get<number>(
      this.api.buildUrl(`${this.resource}/contador-no-leidas`),
      { params: new HttpParams().set('idPersona', String(idPersona)) }
    );
  }

  marcarLeida(idNotificacion: number): Observable<void> {
    return this.http.put<void>(
      this.api.buildUrl(`${this.resource}/${idNotificacion}/marcar-leida`),
      null
    );
  }

  archivar(idNotificacion: number): Observable<void> {
    return this.http.delete<void>(
      this.api.buildUrl(`${this.resource}/${idNotificacion}`)
    );
  }
}
