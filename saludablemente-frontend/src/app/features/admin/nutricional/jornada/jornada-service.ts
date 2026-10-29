import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import {
  EstadoInscripcion,
  EstadoJornada,
  InscripcionJornadaResponse,
  JornadaEvaluacionRequest,
  JornadaEvaluacionResponse,
  Page,
} from './jornada.model';

@Injectable({ providedIn: 'root' })
export class JornadaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/jornadas-nutricionales';

  // ---------------- Consultas ----------------

  buscar(
    estado?: EstadoJornada,
    idPeriodo?: number,
    page = 0,
    size = 20
  ): Observable<Page<JornadaEvaluacionResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (estado) params = params.set('estado', estado);
    if (idPeriodo) params = params.set('idPeriodo', idPeriodo);

    return this.http.get<Page<JornadaEvaluacionResponse>>(
      this.api.buildUrl(this.resource),
      { params }
    );
  }

  obtener(id: number): Observable<JornadaEvaluacionResponse> {
    return this.http.get<JornadaEvaluacionResponse>(
      this.api.buildUrl(`${this.resource}/${id}`)
    );
  }

  misInscripciones(): Observable<InscripcionJornadaResponse[]> {
    return this.http.get<InscripcionJornadaResponse[]>(
      this.api.buildUrl(`${this.resource}/mis-inscripciones`)
    );
  }

  listarInscritos(idJornada: number): Observable<InscripcionJornadaResponse[]> {
    return this.http.get<InscripcionJornadaResponse[]>(
      this.api.buildUrl(`${this.resource}/${idJornada}/inscripciones`)
    );
  }

  // ---------------- Comandos ----------------

  crear(request: JornadaEvaluacionRequest): Observable<JornadaEvaluacionResponse> {
    return this.http.post<JornadaEvaluacionResponse>(
      this.api.buildUrl(this.resource),
      request
    );
  }

  actualizar(
    id: number,
    request: JornadaEvaluacionRequest
  ): Observable<JornadaEvaluacionResponse> {
    return this.http.put<JornadaEvaluacionResponse>(
      this.api.buildUrl(`${this.resource}/${id}`),
      request
    );
  }

  cambiarEstado(
    id: number,
    estado: EstadoJornada
  ): Observable<JornadaEvaluacionResponse> {
    return this.http.patch<JornadaEvaluacionResponse>(
      this.api.buildUrl(`${this.resource}/${id}/estado`),
      { estado }
    );
  }

  inscribirme(idJornada: number): Observable<InscripcionJornadaResponse> {
    return this.http.post<InscripcionJornadaResponse>(
      this.api.buildUrl(`${this.resource}/${idJornada}/inscripciones`),
      null
    );
  }

  cambiarEstadoInscripcion(
    idJornada: number,
    idInscripcion: number,
    estado: EstadoInscripcion
  ): Observable<InscripcionJornadaResponse> {
    return this.http.patch<InscripcionJornadaResponse>(
      this.api.buildUrl(
        `${this.resource}/${idJornada}/inscripciones/${idInscripcion}/estado`
      ),
      { estado }
    );
  }
}
