import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { EMPTY, expand, map, Observable, reduce } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { AlertaClinicaResumen } from '../alerta/alerta-clinica.model';
import * as M from './etiqueta.model';

export function mensajeClinico(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return 'No se pudo completar la operación. Inténtalo nuevamente.';
  if (error.status === 0) return 'No se pudo conectar con el servidor. Tus cambios se conservan.';
  if (error.status === 403) return 'Tu usuario no tiene permiso para realizar esta operación.';
  if (error.status >= 500) return 'El servidor no pudo completar la operación. Tus cambios se conservan.';
  return error.error?.message || 'No se pudo completar la operación.';
}

@Injectable({ providedIn: 'root' })
export class EtiquetaClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly recurso = '/api/v1/etiquetas-clinicas';
  private params(filtros: Record<string, string | number | undefined | null>): HttpParams {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(filtros)) if (value !== '' && value != null) params = params.set(key, value);
    return params;
  }
  consultar(filtros: Record<string, string | number | undefined | null> = {}): Observable<M.Pagina<M.EtiquetaResumen>> {
    return this.http.get<M.Pagina<M.EtiquetaResumen>>(this.api.buildUrl(`${this.recurso}/consulta`), { params: this.params(filtros) });
  }
  todas(): Observable<M.EtiquetaResumen[]> {
    return this.consultar({ tamanio: 100 }).pipe(
      expand(p => p.number + 1 < p.totalPages ? this.consultar({ tamanio: 100, pagina: p.number + 1 }) : EMPTY),
      reduce((filas, pagina) => [...filas, ...pagina.content], [] as M.EtiquetaResumen[]),
    );
  }
  obtener(id: number): Observable<M.EtiquetaClinica> { return this.http.get<M.EtiquetaClinica>(this.api.buildUrl(`${this.recurso}/${id}`)); }
  guardar(id: number | null, request: M.EtiquetaRequest): Observable<M.EtiquetaClinica> {
    return id === null ? this.http.post<M.EtiquetaClinica>(this.api.buildUrl(this.recurso), request)
      : this.http.put<M.EtiquetaClinica>(this.api.buildUrl(`${this.recurso}/${id}`), request);
  }
  estado(id: number, activar: boolean): Observable<unknown> {
    return activar ? this.http.patch(this.api.buildUrl(`${this.recurso}/${id}/reactivar`), {})
      : this.http.delete(this.api.buildUrl(`${this.recurso}/${id}`));
  }
  parametros(estado = 'ACTIVO'): Observable<M.ParametroClinico[]> {
    return this.http.get<M.ParametroClinico[]>(this.api.buildUrl('/api/v1/parametros-evaluacion'), { params: { estado } });
  }
  parametro(id: number): Observable<M.ParametroClinico> {
    return this.http.get<M.ParametroClinico>(this.api.buildUrl(`/api/v1/parametros-evaluacion/${id}`));
  }
  reporte(filtros: Record<string, string | number | undefined | null>): Observable<M.ReporteEtiquetas> {
    return this.http.get<M.ReporteEtiquetas>(this.api.buildUrl(`${this.recurso}/reporte`), { params: this.params(filtros) });
  }
  private ruta(tipo: M.TipoAsociacion, id: number): string {
    switch (tipo) {
      case 'alerta-etiquetas': return `/api/v1/alertas/${id}/etiquetas`;
      case 'etiqueta-alertas': return `${this.recurso}/${id}/alertas`;
      case 'etiqueta-parametros': return `${this.recurso}/${id}/parametros`;
      case 'parametro-etiquetas': return `/api/v1/parametros-evaluacion/${id}/etiquetas`;
    }
  }
  private transformar(tipo: M.TipoAsociacion, response: M.TarjetaResponse<unknown>): M.TarjetaAsociaciones {
    return { revision: response.revision, detalles: response.detalles.map(item => {
      switch (tipo) {
        case 'alerta-etiquetas': {
          const e = item as M.EtiquetaAsignada;
          return { id: e.idEtiqueta, nombre: e.nombre, codigo: e.codigo, color: e.colorHex, origen: e.asignadaPor,
            fecha: e.fechaAsignacion, ruta: `/alertaclinica/etiquetas/${e.idEtiqueta}` };
        }
        case 'etiqueta-alertas': {
          const a = item as M.AlertaAsignada;
          return { id: a.idAlerta, nombre: `Alerta #${a.idAlerta}`, codigo: `Persona #${a.personaId}`,
            descripcion: `${a.nivelSeveridad} · ${a.estado}`, origen: a.asignadaPor, fecha: a.fechaAsignacion,
            ruta: `/alertaclinica/alertas/${a.idAlerta}` };
        }
        case 'etiqueta-parametros': {
          const p = item as M.ParametroAsignado;
          return { id: p.idParametro, nombre: p.nombre, codigo: p.codigo, descripcion: p.unidad,
            ruta: `/alertaclinica/parametros/${p.idParametro}` };
        }
        case 'parametro-etiquetas': return this.opcionEtiqueta(item as M.EtiquetaResumen);
      }
    }) };
  }
  cargarTarjeta(tipo: M.TipoAsociacion, id: number): Observable<M.TarjetaAsociaciones> {
    return this.http.get<M.TarjetaResponse<unknown>>(this.api.buildUrl(this.ruta(tipo, id) + (tipo === 'alerta-etiquetas' ? '/edicion' : '')))
      .pipe(map(response => this.transformar(tipo, response)));
  }
  guardarTarjeta(tipo: M.TipoAsociacion, id: number, revision: string, filas: M.FilaAsociacion[]): Observable<M.TarjetaAsociaciones> {
    const campo = tipo === 'etiqueta-alertas' ? 'idAlerta' : tipo === 'etiqueta-parametros' ? 'idParametro' : 'idEtiqueta';
    return this.http.put<M.TarjetaResponse<unknown>>(this.api.buildUrl(this.ruta(tipo, id)),
      { revision, detalles: filas.map(f => ({ [campo]: f.id })) }).pipe(map(response => this.transformar(tipo, response)));
  }
  opciones(tipo: M.TipoAsociacion, filtros: Record<string, string | number | undefined | null>): Observable<M.Pagina<M.FilaAsociacion>> {
    if (tipo === 'etiqueta-parametros') return this.parametros().pipe(map(parametros => {
      const texto = String(filtros['texto'] || '').toLocaleLowerCase();
      const content = parametros.filter(p => `${p.nombre} ${p.codigo}`.toLocaleLowerCase().includes(texto))
        .map(p => ({ id: p.id, nombre: p.nombre, codigo: p.codigo, descripcion: p.unidad, ruta: `/alertaclinica/parametros/${p.id}` }));
      return { content, totalElements: content.length, totalPages: 1, number: 0 };
    }));
    if (tipo === 'etiqueta-alertas') return this.http.get<M.Pagina<AlertaClinicaResumen>>(this.api.buildUrl('/api/v1/alertas/opciones'),
      { params: this.params(filtros) }).pipe(map(page => ({ ...page, content: page.content.map(a => ({
        id: a.idAlerta, nombre: `Alerta #${a.idAlerta}`, codigo: `Persona #${a.personaId}`,
        descripcion: `${a.nivelSeveridad} · ${a.estado}`, ruta: `/alertaclinica/alertas/${a.idAlerta}`,
      })) })));
    return this.consultar({ ...filtros, estado: 'ACTIVO' }).pipe(map(page => ({ ...page, content: page.content.map(e => this.opcionEtiqueta(e)) })));
  }
  private opcionEtiqueta(e: M.EtiquetaResumen): M.FilaAsociacion {
    return { id: e.id, nombre: e.nombre, codigo: e.codigo, color: e.colorHex, descripcion: e.estado === 'INACTIVO' ? 'Inactiva' : undefined,
      ruta: `/alertaclinica/etiquetas/${e.id}` };
  }
}
