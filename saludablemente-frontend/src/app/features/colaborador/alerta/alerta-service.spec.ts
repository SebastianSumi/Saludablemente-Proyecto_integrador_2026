import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ColAlertaService } from './alerta-service';

describe('ColAlertaService: recursos propios', () => {
  let service: ColAlertaService; let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ColAlertaService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('consulta mis-alertas sin enviar una persona seleccionada por el cliente', () => {
    const filtros = { estado: 'PENDIENTE' as const, personaId: 999 };
    service.listarMias(filtros).subscribe();
    const request = http.expectOne(r => r.url.endsWith('/api/v1/mis-alertas'));
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('estado')).toBe('PENDIENTE');
    expect(request.request.params.has('personaId')).toBe(false);
    request.flush([]);
  });
  it('el detalle usa el recurso propio y no intenta el endpoint global si recibe 404', () => {
    let status = 0;
    service.obtenerMio(42).subscribe({ error: err => status = err.status });
    http.expectOne(r => r.url.endsWith('/api/v1/mis-alertas/42')).flush({ message: 'Alerta no encontrada' }, { status: 404, statusText: 'Not Found' });
    expect(status).toBe(404);
    http.expectNone(r => r.url.includes('/api/v1/alertas'));
  });
  it('rechaza identificadores inválidos antes de enviar una petición', () => {
    let errores = 0;
    for (const id of [0, -1, 1.5, NaN]) service.obtenerMio(id).subscribe({ error: () => errores++ });
    expect(errores).toBe(4); http.expectNone(() => true);
  });
});
