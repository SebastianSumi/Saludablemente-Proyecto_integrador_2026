import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Actividad, ActividadImagen, ActividadRequest } from './models';

@Injectable({ providedIn: 'root' })
export class AgendaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/agenda';

  listar(): Observable<Actividad[]> {
    return this.http.get<Actividad[]>(this.api.buildUrl(`${this.resource}/actividades`));
  }

  deHoy(): Observable<Actividad[]> {
    return this.http.get<Actividad[]>(this.api.buildUrl(`${this.resource}/hoy`));
  }

  obtener(id: number): Observable<Actividad> {
    return this.http.get<Actividad>(this.api.buildUrl(`${this.resource}/actividades/${id}`));
  }

  listarInscritos(id: number): Observable<import('./models').InscritoDetalle[]> {
    return this.http.get<import('./models').InscritoDetalle[]>(
      this.api.buildUrl(`${this.resource}/actividades/${id}/inscripciones`)
    );
  }

  crear(request: ActividadRequest): Observable<Actividad> {
    return this.http.post<Actividad>(
      this.api.buildUrl(`${this.resource}/actividades`),
      request
    );
  }

  actualizarSede(
    id: number,
    request: { latitudOficial: number; longitudOficial: number; toleranciaGeocercaMetros?: number }
  ): Observable<Actividad> {
    return this.http.put<Actividad>(
      this.api.buildUrl(`${this.resource}/actividades/${id}/sede`),
      request
    );
  }
}

@Injectable({ providedIn: 'root' })
export class OperacionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/operacion';

  reprogramar(
    id: number,
    request: { nuevaFecha: string; nuevaHoraInicio: string; nuevaHoraFin: string }
  ): Observable<Actividad> {
    return this.http.put<Actividad>(
      this.api.buildUrl(`${this.resource}/${id}/tiempo`),
      request
    );
  }

  cancelar(id: number, motivo: string): Observable<Actividad> {
    return this.http.patch<Actividad>(this.api.buildUrl(`${this.resource}/${id}/cancelar`), {
      motivo,
    });
  }

  iniciar(id: number): Observable<Actividad> {
    return this.http.patch<Actividad>(
      this.api.buildUrl(`${this.resource}/${id}/iniciar`),
      null
    );
  }

  finalizar(id: number): Observable<Actividad> {
    return this.http.patch<Actividad>(
      this.api.buildUrl(`${this.resource}/${id}/finalizar`),
      null
    );
  }

  transferir(id: number, idNuevoCoordinador: number): Observable<Actividad> {
    return this.http.put<Actividad>(this.api.buildUrl(`${this.resource}/${id}/coordinador`), {
      idNuevoCoordinador,
    });
  }
}

@Injectable({ providedIn: 'root' })
export class InscripcionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  private base(idActividad: number): string {
    return this.api.buildUrl(`/api/v1/actividades/${idActividad}/inscripciones`);
  }

  listar(idActividad: number): Observable<number[]> {
    return this.http.get<number[]>(this.base(idActividad));
  }

  contar(idActividad: number): Observable<{ total: number }> {
    return this.http.get<{ total: number }>(`${this.base(idActividad)}/count`);
  }

  inscribirTodos(idActividad: number): Observable<{ idActividad: number; inscritos: number; modo: string }> {
    return this.http.post<{ idActividad: number; inscritos: number; modo: string }>(
      `${this.base(idActividad)}/todos`,
      null
    );
  }

  inscribirPersonas(
    idActividad: number,
    idsPersonas: number[]
  ): Observable<{ idActividad: number; agregados: number; modo: string }> {
    return this.http.post<{ idActividad: number; agregados: number; modo: string }>(
      this.base(idActividad),
      { idsPersonas }
    );
  }

  eliminar(idActividad: number, idPersona: number): Observable<void> {
    return this.http.delete<void>(`${this.base(idActividad)}/${idPersona}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ConfiguracionActividadService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/configuracion';

  obtener(idActividad: number): Observable<import('./models').Configuracion> {
    return this.http.get<import('./models').Configuracion>(
      this.api.buildUrl(`${this.resource}/${idActividad}`)
    );
  }

  asignarMateriales(idActividad: number, materiales: string[]): Observable<import('./models').Configuracion> {
    return this.http.post<import('./models').Configuracion>(
      this.api.buildUrl(`${this.resource}/${idActividad}/materiales`),
      { materiales }
    );
  }

  asignarPuntaje(idActividad: number, puntos: number): Observable<import('./models').Configuracion> {
    return this.http.put<import('./models').Configuracion>(
      this.api.buildUrl(`${this.resource}/${idActividad}/puntaje`),
      { puntos }
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ReporteActividadService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  porActividad(id: number): Observable<import('./models').ReporteActividad> {
    return this.http.get<import('./models').ReporteActividad>(
      this.api.buildUrl(`/api/v1/reportes/actividades/${id}`)
    );
  }

  porPeriodo(fechaInicio: string, fechaFin: string, periodo?: string): Observable<import('./models').ReportePeriodo> {
    let params = `?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
    if (periodo) params += `&periodo=${encodeURIComponent(periodo)}`;
    return this.http.get<import('./models').ReportePeriodo>(
      this.api.buildUrl(`/api/v1/reportes/agenda${params}`)
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ActividadImagenService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/agenda/actividades';

  subirLogo(idActividad: number, archivo: File): Observable<{ logoUrl: string }> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<{ logoUrl: string }>(
      this.api.buildUrl(`${this.resource}/${idActividad}/logo`),
      formData
    );
  }

  eliminarLogo(idActividad: number): Observable<void> {
    return this.http.delete<void>(
      this.api.buildUrl(`${this.resource}/${idActividad}/logo`)
    );
  }

  subirImagen(idActividad: number, archivo: File, descripcion?: string): Observable<ActividadImagen> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    if (descripcion && descripcion.trim().length > 0) {
      formData.append('descripcion', descripcion.trim());
    }
    return this.http.post<ActividadImagen>(
      this.api.buildUrl(`${this.resource}/${idActividad}/imagenes`),
      formData
    );
  }

  listarImagenes(idActividad: number): Observable<ActividadImagen[]> {
    return this.http.get<ActividadImagen[]>(
      this.api.buildUrl(`${this.resource}/${idActividad}/imagenes`)
    );
  }

  eliminarImagen(idActividad: number, idImagen: number): Observable<void> {
    return this.http.delete<void>(
      this.api.buildUrl(`${this.resource}/${idActividad}/imagenes/${idImagen}`)
    );
  }
}
