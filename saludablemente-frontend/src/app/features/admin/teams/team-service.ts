import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Team, TeamRequest } from './team.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/equipos';

  miEquipo(): Observable<Team> {
    return this.http.get<Team>(this.api.buildUrl(`${this.resource}/mi-equipo`));
  }

  puedeEditar(id: number): Observable<boolean> {
    return this.http.get<boolean>(this.api.buildUrl(`${this.resource}/${id}/puede-editar`));
  }

  miembrosMiEquipo(): Observable<{ idUsuario: number; nombre: string }[]> {
    return this.http.get<{ idUsuario: number; nombre: string }[]>(this.api.buildUrl(`${this.resource}/mi-equipo/miembros`));
  }

  candidatos(id: number): Observable<{ idUsuario: number; nombre: string }[]> {
    return this.http.get<{ idUsuario: number; nombre: string }[]>(this.api.buildUrl(`${this.resource}/${id}/candidatos-lider`));
  }

  asignarLider(id: number, idUsuario: number | null): Observable<Team> {
    return this.http.put<Team>(this.api.buildUrl(`${this.resource}/${id}/lider`), { idUsuario });
  }

  subirImagen(id: number, archivo: File): Observable<Team> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<Team>(this.api.buildUrl(`${this.resource}/${id}/imagen`), datos);
  }

  listar(activo?: boolean): Observable<Team[]> {
    let params = new HttpParams();
    if (activo !== undefined && activo !== null) {
      params = params.set('activo', String(activo));
    }
    return this.http.get<Team[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<Team> {
    return this.http.get<Team>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  crear(request: TeamRequest): Observable<Team> {
    return this.http.post<Team>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: TeamRequest): Observable<Team> {
    return this.http.put<Team>(this.api.buildUrl(`${this.resource}/${id}`), request);
  }

  inactivar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
