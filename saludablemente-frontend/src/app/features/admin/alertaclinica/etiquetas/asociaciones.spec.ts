import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { AsociacionesDraft } from './asociaciones-draft';
import { AsociacionesCard } from './asociaciones-card';
import { EtiquetaClinicaService } from './etiqueta-service';

describe('Colección de asociaciones clínicas', () => {
  it('impide duplicados y protege las asignaciones del motor', () => {
    const draft = new AsociacionesDraft();
    draft.cargar([{ id: 1, nombre: 'Riesgo', codigo: 'RIESGO', origen: 'MOTOR' }]);
    expect(draft.agregar({ id: 1, nombre: 'Duplicada', codigo: 'DUP' })).toBe(false);
    draft.quitar(1);
    expect(draft.filas().length).toBe(1);
    expect(draft.pendiente()).toBe(false);
  });
  it('calcula el cambio real, conserva datos históricos y puede cancelar', () => {
    const draft = new AsociacionesDraft();
    const original = { id: 1, nombre: 'Nombre histórico', codigo: 'COD', fecha: '2026-01-01', origen: 'MANUAL' as const };
    draft.cargar([original]); draft.agregar({ id: 2, nombre: 'Nueva', codigo: 'NUEVA' }); draft.quitar(1);
    expect(draft.agregadas().map(f => f.id)).toEqual([2]);
    expect(draft.retiradas()).toEqual([original]);
    expect(draft.resumen()).toContain('Nombre histórico');
    draft.cancelar();
    expect(draft.filas()).toEqual([original]); expect(draft.pendiente()).toBe(false);
  });
  it('reagregar una fila retirada deja la colección sin cambios', () => {
    const draft = new AsociacionesDraft(); const fila = { id: 1, nombre: 'Etiqueta', codigo: 'E' };
    draft.cargar([fila]); draft.quitar(1); draft.agregar({ ...fila, nombre: 'Nombre nuevo del catálogo' });
    expect(draft.pendiente()).toBe(false);
    expect(draft.filas()).toEqual([fila]);
  });
});

describe('Contrato frontend de asociaciones', () => {
  let http: HttpTestingController; let service: EtiquetaClinicaService;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController); service = TestBed.inject(EtiquetaClinicaService);
  });
  afterEach(() => http.verify());
  it.each([
    ['alerta-etiquetas', '/api/v1/alertas/8/etiquetas', 'idEtiqueta'],
    ['etiqueta-alertas', '/api/v1/etiquetas-clinicas/8/alertas', 'idAlerta'],
    ['etiqueta-parametros', '/api/v1/etiquetas-clinicas/8/parametros', 'idParametro'],
    ['parametro-etiquetas', '/api/v1/parametros-evaluacion/8/etiquetas', 'idEtiqueta'],
  ] as const)('envía un único PUT para %s con revisión y detalles', (tipo, ruta, campo) => {
    service.guardarTarjeta(tipo, 8, 'revision-actual', [{ id: 1, nombre: 'Uno', codigo: 'UNO' }, { id: 2, nombre: 'Dos', codigo: 'DOS' }]).subscribe();
    const request = http.expectOne(r => r.url.endsWith(ruta));
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ revision: 'revision-actual', detalles: [{ [campo]: 1 }, { [campo]: 2 }] });
    request.flush({ revision: 'nueva', detalles: [] });
  });
  it('carga todas las páginas del catálogo para los filtros', () => {
    let ids: number[] = [];
    service.todas().subscribe(items => ids = items.map(i => i.id));
    http.expectOne(r => r.url.endsWith('/consulta')).flush({ content: [{ id: 1 }], number: 0, totalPages: 2, totalElements: 2 });
    const segunda = http.expectOne(r => r.url.endsWith('/consulta'));
    expect(segunda.request.params.get('pagina')).toBe('1');
    segunda.flush({ content: [{ id: 2 }], number: 1, totalPages: 2, totalElements: 2 });
    expect(ids).toEqual([1, 2]);
  });
});

describe('Card transaccional de etiquetas', () => {
  let http: HttpTestingController;
  const confirmar = { confirmar: vi.fn(() => of(true)) };
  beforeEach(() => {
    confirmar.confirmar.mockReset().mockReturnValue(of(true));
    TestBed.configureTestingModule({ imports: [AsociacionesCard], providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), { provide: ConfirmService, useValue: confirmar }] });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  function tarjeta() {
    const fixture = TestBed.createComponent(AsociacionesCard);
    fixture.componentRef.setInput('tipo', 'alerta-etiquetas'); fixture.componentRef.setInput('entidadId', 9);
    fixture.detectChanges();
    http.expectOne(r => r.url.endsWith('/alertas/9/etiquetas/edicion')).flush({ revision: 'r1', detalles: [
      { idEtiqueta: 1, nombre: 'Automática', codigo: 'AUTO', colorHex: '#123456', asignadaPor: 'MOTOR', fechaAsignacion: '2026-10-01T12:00:00' },
    ] });
    fixture.detectChanges(); return fixture;
  }
  it('incluye MOTOR en el guardado y acepta la nueva revisión del servidor', () => {
    const fixture = tarjeta(); const card = fixture.componentInstance;
    card.agregar({ id: 2, nombre: 'Manual', codigo: 'MANUAL' }); card.guardar();
    const request = http.expectOne(r => r.method === 'PUT');
    expect(request.request.body).toEqual({ revision: 'r1', detalles: [{ idEtiqueta: 1 }, { idEtiqueta: 2 }] });
    request.flush({ revision: 'r2', detalles: [
      { idEtiqueta: 1, nombre: 'Automática', codigo: 'AUTO', asignadaPor: 'MOTOR' },
      { idEtiqueta: 2, nombre: 'Manual', codigo: 'MANUAL', asignadaPor: 'MANUAL' },
    ] });
    expect(card.draft.pendiente()).toBe(false); expect(card.ocupado()).toBe(false); expect(card.exito()).toBeTruthy();
  });
  it('conserva el borrador ante 409 y exige recargar antes de repetir', () => {
    const card = tarjeta().componentInstance;
    card.agregar({ id: 2, nombre: 'Manual', codigo: 'MANUAL' }); card.guardar();
    http.expectOne(r => r.method === 'PUT').flush({ message: 'La revisión cambió' }, { status: 409, statusText: 'Conflict' });
    expect(card.draft.filas().length).toBe(2); expect(card.conflicto()).toBe(true); expect(card.ocupado()).toBe(false);
    card.guardar(); http.expectNone(r => r.method === 'PUT');
  });
  it('cancelar la confirmación no envía peticiones y mantiene la edición', () => {
    const card = tarjeta().componentInstance;
    confirmar.confirmar.mockReturnValue(of(false)); card.agregar({ id: 2, nombre: 'Manual', codigo: 'MANUAL' }); card.guardar();
    http.expectNone(r => r.method === 'PUT'); expect(card.draft.pendiente()).toBe(true); expect(card.ocupado()).toBe(false);
  });
});
