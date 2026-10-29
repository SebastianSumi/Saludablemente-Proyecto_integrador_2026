import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { EvaluacionList } from './evaluacion-list';
import { AptitudService } from '../aptitud-service';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';

@Component({
  template: '<tui-root><app-evaluacion-list /></tui-root>',
  imports: [TuiRoot, EvaluacionList],
})
class Host {}

const ITEMS = [
  {
    id: 1,
    nombresCompletosAlMomento: 'Ana Lázaro',
    fechaEvaluacion: '2026-10-01',
    estadoEvaluacion: 'COMPLETA',
    dictamenGlobal: 'APTO',
    fitnessScore: 85,
  },
  {
    id: 2,
    nombresCompletosAlMomento: 'Juan Pérez',
    fechaEvaluacion: '2026-10-02',
    estadoEvaluacion: 'PENDIENTE',
  },
];

describe('EvaluacionList aptitud', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({
        matches: false,
        addEventListener: () => undefined,
        removeEventListener: () => undefined,
      }),
    });
    (document as unknown as { execCommand: () => boolean }).execCommand = () => false;
    await TestBed.configureTestingModule({
      imports: [EvaluacionList],
      providers: [
        provideTaiga(),
        {
          provide: AptitudService,
          useValue: {
            buscar: () => of({ content: ITEMS, totalElements: 2, totalPages: 1, number: 0, size: 20 }),
            agregados: () =>
              of({
                total: 2,
                pendientes: 1,
                completas: 1,
                anuladas: 0,
                aptos: 1,
                aptosConRestricciones: 0,
                noAptos: 0,
              }),
            anular: () => of(undefined),
          },
        },
        {
          provide: PeriodoService,
          useValue: { listar: () => of([]), obtenerVigente: () => of(null) },
        },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => null } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('muestra KPIs y filas', () => {
    const texto = fixture.nativeElement.textContent as string;
    expect(texto).toContain('Ana Lázaro');
    expect(texto).toContain('Juan Pérez');
    expect(texto).toContain('2 evaluación(es)');
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
  });
});
