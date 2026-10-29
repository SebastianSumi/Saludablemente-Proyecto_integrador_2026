import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import {
  CelularUpdateRequest,
  CredencialResponse,
  Persona,
  PersonaFiltros,
  PersonaRequest,
  PersonaResumen,
  PersonalAgregado,
  PreferenciaRequest,
  PreferenciaResponse,
} from './persona.model';

@Injectable({ providedIn: 'root' })
export class PersonaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/personal';

  // -------------------------------------------------------------------
  // Listados y consultas
  // -------------------------------------------------------------------

  listar(filtros: PersonaFiltros = {}): Observable<Persona[]> {
    let params = new HttpParams();

    if (filtros.estado !== undefined && filtros.estado !== null) {
      params = params.set('estado', String(filtros.estado));
    }
    if (filtros.sexo) {
      params = params.set('sexo', filtros.sexo);
    }
    if (filtros.areaId) {
      params = params.set('areaId', String(filtros.areaId));
    }
    if (filtros.equipoId) {
      params = params.set('equipoId', String(filtros.equipoId));
    }
    if (filtros.texto) {
      params = params.set('texto', filtros.texto);
    }
    if (filtros.ordenarPor) {
      params = params.set('ordenarPor', filtros.ordenarPor);
    }
    if (filtros.direccion) {
      params = params.set('direccion', filtros.direccion);
    }

    return this.http.get<Persona[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Persona> {
    return this.http.get<Persona>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  porEquipo(idEquipo: number): Observable<PersonaResumen[]> {
    return this.http.get<PersonaResumen[]>(
      this.api.buildUrl(`${this.resource}/equipo/${idEquipo}`)
    );
  }

  reporte(): Observable<PersonalAgregado> {
    return this.http.get<PersonalAgregado>(this.api.buildUrl(`${this.resource}/resumen`));
  }

  // -------------------------------------------------------------------
  // Comandos
  // -------------------------------------------------------------------

  crear(request: PersonaRequest): Observable<Persona> {
    return this.http.post<Persona>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: PersonaRequest): Observable<Persona> {
    return this.http.put<Persona>(this.api.buildUrl(`${this.resource}/${id}`), request);
  }

  cambiarCelular(id: number, request: CelularUpdateRequest): Observable<Persona> {
    return this.http.patch<Persona>(
      this.api.buildUrl(`${this.resource}/${id}/celular`),
      request
    );
  }

  inactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  reactivar(id: number): Observable<Persona> {
    return this.http.patch<Persona>(
      this.api.buildUrl(`${this.resource}/${id}/reactivar`),
      null
    );
  }

  subirAvatar(id: number, archivo: File): Observable<Persona> {
    const datos = new FormData();
    datos.append('avatar', archivo);
    return this.http.post<Persona>(
      this.api.buildUrl(`${this.resource}/${id}/avatar`),
      datos
    );
  }

  eliminarAvatar(id: number): Observable<Persona> {
    return this.http.delete<Persona>(this.api.buildUrl(`${this.resource}/${id}/avatar`));
  }

  // -------------------------------------------------------------------
  // Preferencias
  // -------------------------------------------------------------------

  obtenerPreferencias(idPersona: number): Observable<PreferenciaResponse> {
    return this.http.get<PreferenciaResponse>(
      this.api.buildUrl(`${this.resource}/${idPersona}/preferencias`)
    );
  }

  upsertPreferencias(
    idPersona: number,
    request: PreferenciaRequest
  ): Observable<PreferenciaResponse> {
    return this.http.put<PreferenciaResponse>(
      this.api.buildUrl(`${this.resource}/${idPersona}/preferencias`),
      request
    );
  }

  // -------------------------------------------------------------------
  // Credencial QR
  // -------------------------------------------------------------------

  generarCredencial(idPersona: number, incluirQr: boolean): Observable<CredencialResponse> {
    const params = new HttpParams().set('incluirQr', String(incluirQr));
    return this.http.post<CredencialResponse>(
      this.api.buildUrl(`${this.resource}/${idPersona}/credencial`),
      null,
      { params }
    );
  }
}
