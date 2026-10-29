import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { AntropometricoRequest, AntropometricoResponse } from './antropometrico.model';

@Injectable({ providedIn: 'root' })
export class AntropometricoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  private url(idEvaluacion: number): string {
    return this.api.buildUrl(
      `/api/v1/evaluaciones-nutricionales/${idEvaluacion}/antropometrico`
    );
  }

  registrar(idEvaluacion: number, request: AntropometricoRequest): Observable<AntropometricoResponse> {
    return this.http.post<AntropometricoResponse>(this.url(idEvaluacion), request);
  }

  actualizar(idEvaluacion: number, request: AntropometricoRequest): Observable<AntropometricoResponse> {
    return this.http.put<AntropometricoResponse>(this.url(idEvaluacion), request);
  }

  recalcular(idEvaluacion: number): Observable<AntropometricoResponse> {
    return this.http.put<AntropometricoResponse>(`${this.url(idEvaluacion)}/recalcular`, {});
  }
}
