import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  BandejaNotificacion,
  Notificacion,
  Plantilla,
  PlantillaRequest,
} from './models';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/notificaciones';

  listar(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(this.api.buildUrl(this.resource));
  }

  obtener(id: number): Observable<Notificacion> {
    return this.http.get<Notificacion>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  misAlertas(idPersona: number): Observable<BandejaNotificacion[]> {
    const params = new HttpParams().set('idPersona', String(idPersona));
    return this.http.get<BandejaNotificacion[]>(
      this.api.buildUrl(`${this.resource}/mis-alertas`),
      { params },
    );
  }

  contadorNoLeidas(idPersona: number): Observable<number> {
    const params = new HttpParams().set('idPersona', String(idPersona));
    return this.http.get<number>(
      this.api.buildUrl(`${this.resource}/contador-no-leidas`),
      { params },
    );
  }

  marcarLeida(id: number): Observable<void> {
    return this.http.put<void>(
      this.api.buildUrl(`${this.resource}/${id}/marcar-leida`),
      null,
    );
  }

  archivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}

@Injectable({ providedIn: 'root' })
export class PlantillaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/notificaciones/plantillas';

  listar(): Observable<Plantilla[]> {
    return this.http.get<Plantilla[]>(this.api.buildUrl(this.resource));
  }

  obtener(id: number): Observable<Plantilla> {
    return this.http.get<Plantilla>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  crear(request: PlantillaRequest): Observable<Plantilla> {
    return this.http.post<Plantilla>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: PlantillaRequest): Observable<Plantilla> {
    return this.http.put<Plantilla>(
      this.api.buildUrl(`${this.resource}/${id}`),
      request,
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
