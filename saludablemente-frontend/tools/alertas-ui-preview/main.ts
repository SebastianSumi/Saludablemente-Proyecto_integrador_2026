import { ColAlertaList } from '../../src/app/features/colaborador/alerta/alerta-list';
import { ColAlertaDetail } from '../../src/app/features/colaborador/alerta/alerta-detail';
import { ColAlertaService } from '../../src/app/features/colaborador/alerta/alerta-service';
import type { AlertaClinica } from '../../src/app/features/admin/alertaclinica/alerta/alerta-clinica.model';
/** Vista QA aislada: solo datos sinteticos en memoria. Nunca forma parte del bootstrap de produccion. */
import { Component, provideZonelessChangeDetection, signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, RouterLink, RouterOutlet, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { of, throwError } from 'rxjs';
import { EtiquetaList } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta-list';
import { EtiquetaForm } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta-form';
import { EtiquetaDetail } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta-detail';
import { ParametroList } from '../../src/app/features/admin/alertaclinica/etiquetas/parametro-list';
import { ParametroDetail } from '../../src/app/features/admin/alertaclinica/etiquetas/parametro-detail';
import { EtiquetaReporte } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta-reporte';
import { cambiosClinicosGuard } from '../../src/app/features/admin/alertaclinica/etiquetas/cambios-pendientes';
import { EtiquetaClinicaService } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta-service';
import { PersonaNombreService } from '../../src/app/features/admin/alertaclinica/alerta/persona-nombre-service';
import { TipoAsociacion, FilaAsociacion, EtiquetaRequest } from '../../src/app/features/admin/alertaclinica/etiquetas/etiqueta.model';

const parametros = [{ id: 1, codigo: 'IMC', nombre: 'Índice de masa corporal', unidad: 'kg/m²' },
  { id: 2, codigo: 'GLUCOSA', nombre: 'Glucosa en sangre', unidad: 'mg/dL' }, { id: 3, codigo: 'COLESTEROL', nombre: 'Colesterol total', unidad: 'mg/dL' }];
const etiquetas = [{ id: 1, codigo: 'RIESGO_METABOLICO', nombre: 'Riesgo metabólico', colorHex: '#2E7D5B', estado: 'ACTIVO', cantidadParametros: 2, cantidadAlertas: 1 },
  { id: 2, codigo: 'SEGUIMIENTO', nombre: 'Seguimiento nutricional', colorHex: '#8965E5', estado: 'ACTIVO', cantidadParametros: 1, cantidadAlertas: 1 },
  { id: 3, codigo: 'CONTROL_PREVENTIVO', nombre: 'Control preventivo', colorHex: '#00A1FF', estado: 'ACTIVO', cantidadParametros: 0, cantidadAlertas: 0 }];
const alerta = { idAlerta: 7, personaId: 1, nivelSeveridad: 'CRITICA', estado: 'PENDIENTE', fechaGeneracion: '2026-10-20T09:30:00', asignadaPor: 'MOTOR', fechaAsignacion: '2026-10-20T09:30:00' };
const tarjetas = new Map<string, FilaAsociacion[]>();
const key = (tipo: TipoAsociacion, id: number) => `${tipo}:${id}`;
const data = (tipo: TipoAsociacion, id: number): FilaAsociacion[] => {
  const k = key(tipo, id);
  if (!tarjetas.has(k)) tarjetas.set(k, tipo === 'etiqueta-alertas'
    ? [{ id: 7, nombre: 'Alerta #7', codigo: 'Persona #1', descripcion: 'CRITICA · PENDIENTE', origen: 'MOTOR', fecha: alerta.fechaAsignacion }]
    : tipo === 'etiqueta-parametros' ? parametros.slice(0, 2).map(p => ({ id: p.id, nombre: p.nombre, codigo: p.codigo, descripcion: p.unidad }))
    : etiquetas.slice(0, 1).map(e => ({ id: e.id, nombre: e.nombre, codigo: e.codigo, color: e.colorHex })));
  return tarjetas.get(k)!;
};
let version = 1;
const detalle = (id: number) => ({ ...etiquetas.find(e => e.id === id)!, descripcion: 'Clasificación para el seguimiento de indicadores metabólicos. Sus parámetros orientan las asignaciones automáticas del motor.',
  fechaBaja: null, revision: `r${version}`, parametros: parametros.slice(0, 2).map(p => ({ ...p, idParametro: p.id })), alertas: [alerta] });
const service = {
  consultar: (f: Record<string, string | number> = {}) => { const content = etiquetas.filter(e => (!f['texto'] || `${e.nombre} ${e.codigo}`.toLowerCase().includes(String(f['texto']).toLowerCase())) && (!f['estado'] || e.estado === f['estado'])); return of({ content, number: 0, totalPages: 1, totalElements: content.length }); },
  todas: () => of(etiquetas), parametros: () => of(parametros), parametro: (id: number) => of(parametros.find(p => p.id === id)), obtener: (id: number) => of(detalle(id)),
  guardar: (id: number | null, request: EtiquetaRequest) => { if (id === null) { id = etiquetas.length + 1; etiquetas.push({ ...request, id, estado: 'ACTIVO', cantidadParametros: request.detalles.length, cantidadAlertas: 0 }); } else Object.assign(etiquetas.find(e => e.id === id)!, request); version++; return of(detalle(id)); },
  estado: (id: number, activar: boolean) => { etiquetas.find(e => e.id === id)!.estado = activar ? 'ACTIVO' : 'INACTIVO'; return of(null); },
  cargarTarjeta: (tipo: TipoAsociacion, id: number) => of({ revision: `r${version}`, detalles: [...data(tipo, id)] }),
  guardarTarjeta: (tipo: TipoAsociacion, id: number, revision: string, filas: FilaAsociacion[]) => {
    if (revision !== `r${version}`) return throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'La revisión cambió. Recarga la tarjeta.' } }));
    tarjetas.set(key(tipo, id), filas.map(f => ({ ...f, origen: tipo.includes('alerta') ? f.origen || 'MANUAL' : undefined }))); version++;
    return of({ revision: `r${version}`, detalles: data(tipo, id) });
  },
  opciones: (tipo: TipoAsociacion) => of({ content: tipo === 'etiqueta-alertas' ? [{ id: 7, nombre: 'Alerta #7', codigo: 'Persona #1', descripcion: 'CRITICA · PENDIENTE' }, { id: 8, nombre: 'Alerta #8', codigo: 'Persona #2', descripcion: 'MODERADA · PENDIENTE' }] : tipo === 'etiqueta-parametros' ? parametros.map(p => ({ ...p, descripcion: p.unidad })) : etiquetas.map(e => ({ ...e, color: e.colorHex })), totalElements: 3, totalPages: 1, number: 0 }),
  reporte: () => of({ cantidadEtiquetas: 3, etiquetasActivas: 3, cantidadAsociaciones: 2, alertasUnicas: 1, alertasCriticasUnicas: 1,
    detalles: etiquetas.map(e => ({ idEtiqueta: e.id, ...e, parametros: detalle(e.id).parametros, ultimaAsignacion: e.cantidadAlertas ? alerta.fechaAsignacion : null, alertas: e.cantidadAlertas ? [alerta] : [] })) }),
};
const misAlertas: AlertaClinica[] = [
  { idAlerta: 7, personaId: 1, evaluacionOrigenId: 3, tipoEvaluacionOrigen: 'NUTRICIONAL', puntajeTotal: 8,
    nivelSeveridad: 'CRITICA', mensajeResumen: 'Se identificaron indicadores que requieren revisión por el equipo responsable.',
    estado: 'PENDIENTE', fechaGeneracion: '2026-10-20T09:30:00', fechaAtencion: null, usuarioAtiendeId: null, observacionAtencion: null,
    detalles: [{ idDetalle: 1, tipoIndicador: 'IMC', valorMedido: 30, valorMin: 18.5, valorMax: 24.9, clasificacion: 'Fuera de rango', puntosRiesgo: 4, mensaje: 'Indicador de demostración, sin datos clínicos reales.', motorDetonante: 'VALOR_ABSOLUTO' }],
    etiquetas: [{ idEtiqueta: 1, codigo: 'RIESGO_METABOLICO', nombre: 'Riesgo metabólico', colorHex: '#2E7D5B', asignadaPor: 'MOTOR', fechaAsignacion: '2026-10-20T09:30:00' },
      { idEtiqueta: 2, codigo: 'SEGUIMIENTO', nombre: 'Seguimiento nutricional', colorHex: '#8965E5', asignadaPor: 'MANUAL', fechaAsignacion: '2026-10-20T10:00:00' }] },
  { idAlerta: 8, personaId: 1, evaluacionOrigenId: 4, tipoEvaluacionOrigen: 'APTITUD_FISICA', puntajeTotal: 4,
    nivelSeveridad: 'MODERADA', mensajeResumen: 'Alerta de demostración para consulta y seguimiento.', estado: 'PENDIENTE',
    fechaGeneracion: '2026-10-18T11:00:00', fechaAtencion: null, usuarioAtiendeId: null, observacionAtencion: null, detalles: [],
    etiquetas: [{ idEtiqueta: 2, codigo: 'SEGUIMIENTO', nombre: 'Seguimiento nutricional', colorHex: '#8965E5', asignadaPor: 'MANUAL', fechaAsignacion: '2026-10-18T11:05:00' }] },
];
@Component({ selector: 'app-root', standalone: true, imports: [RouterLink, RouterOutlet, TuiRoot],
  template: `<tui-root><div class="qa-shell" [attr.data-theme]="oscuro() ? 'dark' : 'light'"><div class="qa-strip"><strong>Vista de prueba · Datos sintéticos</strong><a routerLink="/colaborador/mis-alertas">Vista colaborador</a><button (click)="tema()">Cambiar tema</button><div><button (click)="ancho.set('390px')">Móvil 390 px</button><button (click)="ancho.set('100%')">Escritorio</button></div></div><main [style.max-width]="ancho()"><router-outlet /></main></div></tui-root>`,
  styles: [`.qa-shell{min-height:100vh;background:var(--sal-page)}main{box-sizing:border-box;margin:auto;padding:24px;transition:none}.qa-strip{display:flex;gap:16px;align-items:center;flex-wrap:wrap;padding:12px 24px;background:var(--sal-card);border-bottom:1px solid var(--sal-line);margin-bottom:12px;font-size:12px;color:var(--sal-ink-soft)}button{padding:8px 12px;border:1px solid var(--sal-line);border-radius:8px;background:var(--sal-card);color:var(--sal-ink);cursor:pointer}@media(max-width:600px){main{padding:12px}}`],
})
class Preview {
  readonly oscuro = signal(false); readonly ancho = signal('100%');
  tema(): void { this.oscuro.update(v => !v); document.body.setAttribute('tuiTheme', this.oscuro() ? 'dark' : 'light'); }
}
bootstrapApplication(Preview, { providers: [provideZonelessChangeDetection(), provideTaiga(), provideHttpClient(),
  { provide: EtiquetaClinicaService, useValue: service },
  { provide: ColAlertaService, useValue: { listarMias: () => of(misAlertas), obtenerMio: (id: number) => misAlertas.some(a => a.idAlerta === id) ? of(misAlertas.find(a => a.idAlerta === id)) : throwError(() => new HttpErrorResponse({ status: 404 })) } },
  { provide: PersonaNombreService, useValue: { nombres: () => of(new Map([[1, 'Persona de demostración A'], [2, 'Persona de demostración B']])), nombreDe: (m: Map<number, string>, id: number) => m.get(id) || `Persona #${id}` } },
  provideRouter([
    { path: 'colaborador/mis-alertas', component: ColAlertaList },
    { path: 'colaborador/mis-alertas/:id', component: ColAlertaDetail },
    { path: 'alertaclinica/etiquetas', component: EtiquetaList },
    { path: 'alertaclinica/etiquetas/nueva', component: EtiquetaForm, canDeactivate: [cambiosClinicosGuard] },
    { path: 'alertaclinica/etiquetas/:id/editar', component: EtiquetaForm, canDeactivate: [cambiosClinicosGuard] },
    { path: 'alertaclinica/etiquetas/:id', component: EtiquetaDetail, canDeactivate: [cambiosClinicosGuard] },
    { path: 'alertaclinica/parametros', component: ParametroList },
    { path: 'alertaclinica/parametros/:id', component: ParametroDetail, canDeactivate: [cambiosClinicosGuard] },
    { path: 'alertaclinica/reportes/etiquetas', component: EtiquetaReporte },
    { path: '**', redirectTo: 'alertaclinica/etiquetas' },
  ], withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })),
] }).catch(console.error);
