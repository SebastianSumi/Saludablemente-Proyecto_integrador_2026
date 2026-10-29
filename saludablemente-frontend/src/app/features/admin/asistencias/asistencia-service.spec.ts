import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AsistenciaService } from './asistencia-service';
import { ApiService } from '../../../core/services/api-service';

describe('AsistenciaService: cabecera y detalle', () => {
  let service: AsistenciaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(), provideHttpClientTesting(),
      { provide: ApiService, useValue: { buildUrl: (path: string) => path } },
    ] });
    service = TestBed.inject(AsistenciaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía marcas diferentes en una sola solicitud manual', () => {
    const body = { idActividad: 8, tipoAsistencia: 'SIMPLE' as const, detalle: [
      { idPersona: 20, marca: 'PRESENTE' as const },
      { idPersona: 21, marca: 'FALTA' as const },
    ] };
    service.guardarManualDetalle(body).subscribe();
    const req = http.expectOne('/api/v1/asistencias/manual/detalle');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ idActividad: 8, nombreActividad: 'Actividad', detalle: [] });
  });

  it('consulta el JSON agrupado y conserva los IDs individuales para las vistas', () => {
    service.listarPorActividad(8).subscribe((detalle) => {
      expect(detalle.map((a) => a.idAsistencia)).toEqual([101, 102]);
    });
    http.expectOne('/api/v1/asistencias/manual/actividad/8').flush({
      idActividad: 8, nombreActividad: 'Actividad',
      detalle: [{ idAsistencia: 101 }, { idAsistencia: 102 }],
    });
  });

  it('propaga el error de guardado para conservar el borrador', () => {
    service.guardarManualDetalle({ idActividad: 8, tipoAsistencia: 'SIMPLE',
      detalle: [{ idPersona: 20, marca: 'TARDE' }],
    }).subscribe({ error: (err) => expect(err.status).toBe(409) });
    http.expectOne('/api/v1/asistencias/manual/detalle')
      .flush({ message: 'Persona inactiva' }, { status: 409, statusText: 'Conflict' });
  });
});
