import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ColAlertaDetail } from './alerta-detail';
import { ColAlertaService } from './alerta-service';
import type { AlertaClinica } from './alerta.model';

const ALERTA: AlertaClinica = {
  idAlerta: 1,
  personaId: 7,
  evaluacionOrigenId: 3,
  tipoEvaluacionOrigen: 'NUTRICIONAL',
  puntajeTotal: 10,
  nivelSeveridad: 'LEVE',
  mensajeResumen: 'Resumen de prueba',
  estado: 'PENDIENTE',
  fechaGeneracion: '2026-01-01T10:00:00',
  fechaAtencion: null,
  usuarioAtiendeId: null,
  observacionAtencion: null,
  detalles: [],
};

describe('ColAlertaDetail', () => {
  it('no conserva datos anteriores cuando otra alerta de la ruta no es accesible', () => {
    const params = new BehaviorSubject(convertToParamMap({ id: '1' }));
    TestBed.configureTestingModule({ providers: [provideRouter([]),
      { provide: ColAlertaService, useValue: { obtenerMio: (id: number) => id === 1 ? of(ALERTA) : throwError(() => new HttpErrorResponse({ status: 404 })) } },
      { provide: ActivatedRoute, useValue: { paramMap: params, snapshot: { paramMap: convertToParamMap({ id: '1' }) } } },
    ] });
    const fixture = TestBed.createComponent(ColAlertaDetail); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Mi alerta #1');
    params.next(convertToParamMap({ id: '999' })); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Mi alerta #1');
    expect(fixture.nativeElement.textContent).toContain('No encontramos esta alerta entre tus alertas.');
  });

  it('muestra la card de etiquetas sin controles de administración', () => {
    const propia = { ...ALERTA, etiquetas: [{ idEtiqueta: 1, nombre: 'Seguimiento', codigo: 'SEG', colorHex: '#123456', asignadaPor: 'MANUAL' as const, fechaAsignacion: '2026-10-20T09:30:00' }] };
    TestBed.configureTestingModule({ providers: [provideRouter([]),
      { provide: ColAlertaService, useValue: { obtenerMio: () => of(propia) } },
      { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })), snapshot: { paramMap: { get: () => '1' } } } },
    ] });
    const fixture = TestBed.createComponent(ColAlertaDetail); fixture.detectChanges();
    const card = (fixture.nativeElement as HTMLElement).querySelector('.etiquetas-card')!;
    expect(card.textContent).toContain('Seguimiento'); expect(card.textContent).toContain('Asignada por el equipo');
    expect(card.querySelector('button,a')).toBeNull();
  });

  it('crea el componente en modo solo lectura', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: ColAlertaService, useValue: { obtenerMio: () => of(ALERTA) } },
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })), snapshot: { paramMap: { get: () => '1' } } } },
      ],
    });
    const fixture = TestBed.createComponent(ColAlertaDetail);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Solo lectura');
  });

  it('enlaza a la evaluación de origen', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: ColAlertaService, useValue: { obtenerMio: () => of(ALERTA) } },
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })), snapshot: { paramMap: { get: () => '1' } } } },
      ],
    });
    const fixture = TestBed.createComponent(ColAlertaDetail);
    fixture.detectChanges();
    const link = (fixture.nativeElement as HTMLElement).querySelector(
      'a[href*="evaluaciones/nutricional/3"]',
    );
    expect(link).toBeTruthy();
  });
});
