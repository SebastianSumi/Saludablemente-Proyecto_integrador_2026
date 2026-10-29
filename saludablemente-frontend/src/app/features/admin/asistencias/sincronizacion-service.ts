import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { LoteRequest, LoteResponse } from './models';

@Injectable({ providedIn: 'root' })
export class SincronizacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/sincronizacion';

  ping(): Observable<{ status: string; timestamp: number }> {
    return this.http.get<{ status: string; timestamp: number }>(
      this.api.buildUrl(`${this.resource}/ping`)
    );
  }

  procesarLote(request: LoteRequest): Observable<LoteResponse> {
    return this.http.post<LoteResponse>(
      this.api.buildUrl(`${this.resource}/lote`),
      request
    );
  }
}
