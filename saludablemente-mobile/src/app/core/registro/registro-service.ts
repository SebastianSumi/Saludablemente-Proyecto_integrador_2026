import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../services/api-service';

export interface VerificarRegistroRequest {
  documentoIdentidad: string;
  email: string;
}

export interface VerificarRegistroResponse {
  nombreMostrado: string;
}

export interface RegistroRequest {
  email: string;
  password: string;
  documentoIdentidad: string;
}

@Injectable({ providedIn: 'root' })
export class RegistroService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/registro';

  verificar(request: VerificarRegistroRequest): Observable<VerificarRegistroResponse> {
    return this.http.post<VerificarRegistroResponse>(
      this.api.buildUrl(`${this.resource}/verificar`),
      request,
    );
  }

  registrar(request: RegistroRequest): Observable<unknown> {
    return this.http.post(this.api.buildUrl(this.resource), request);
  }
}
