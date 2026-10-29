import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import {
  BioquimicoManualRequest,
  BioquimicoResponse,
  ImportacionReporte,
} from './bioquimico.model';

@Injectable({ providedIn: 'root' })
export class BioquimicoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/evaluaciones-nutricionales';

  /**
   * Registro manual del laboratorio para un colaborador puntual.
   */
  registrarManual(
    idEvaluacion: number,
    request: BioquimicoManualRequest
  ): Observable<BioquimicoResponse> {
    return this.http.post<BioquimicoResponse>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/bioquimico/manual`),
      request
    );
  }

  /**
   * Actualiza el bioquímico existente.
   * Permitido en PENDIENTE y COMPLETA (correcciones del laboratorio).
   */
  actualizarManual(
    idEvaluacion: number,
    request: BioquimicoManualRequest
  ): Observable<BioquimicoResponse> {
    return this.http.put<BioquimicoResponse>(
      this.api.buildUrl(`${this.resource}/${idEvaluacion}/bioquimico/manual`),
      request
    );
  }

  /**
   * Importación masiva del Excel de laboratorio.
   * Cruce por celular. Devuelve el reporte de procesadas y rechazadas.
   */
  importarExcel(archivo: File, periodo: string): Observable<ImportacionReporte> {
    const formData = new FormData();
    formData.append('file', archivo);
    formData.append('periodo', periodo);

    return this.http.post<ImportacionReporte>(
      this.api.buildUrl(`${this.resource}/importar-bioquimico`),
      formData
    );
  }
}
