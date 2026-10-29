import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  CheckInRequest,
  EstadoMeta,
  Meta,
  MetaClinicaRequest,
  MetaRequest,
  MetaSugeridaRequest,
  ProgresoMeta,
} from './models';

@Injectable({ providedIn: 'root' })
export class MetaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  crearPersonal(request: MetaRequest): Observable<Meta> {
    return this.http.post<Meta>(this.api.buildUrl('/api/v1/metas/personales'), request);
  }

  crearClinica(request: MetaClinicaRequest): Observable<Meta> {
    return this.http.post<Meta>(this.api.buildUrl('/api/v1/metas/clinicas'), request);
  }

  crearSugerida(request: MetaSugeridaRequest): Observable<Meta> {
    return this.http.post<Meta>(this.api.buildUrl('/api/v1/metas/sugeridas'), request);
  }

  listarPorPersona(idPersona: number, estado?: EstadoMeta): Observable<Meta[]> {
    let params = new HttpParams();
    if (estado) params = params.set('estado', estado);
    return this.http.get<Meta[]>(
      this.api.buildUrl(`/api/v1/metas/personales/persona/${idPersona}`),
      { params }
    );
  }

  progreso(idMeta: number): Observable<ProgresoMeta> {
    return this.http.get<ProgresoMeta>(
      this.api.buildUrl(`/api/v1/metas/personales/${idMeta}/progreso`)
    );
  }

  registrarCheckIn(request: CheckInRequest): Observable<ProgresoMeta> {
    return this.http.post<ProgresoMeta>(
      this.api.buildUrl('/api/v1/metas/checkins'),
      request
    );
  }

  historial(idMeta: number): Observable<ProgresoMeta[]> {
    return this.http.get<ProgresoMeta[]>(
      this.api.buildUrl(`/api/v1/metas/checkins/meta/${idMeta}`)
    );
  }
}
