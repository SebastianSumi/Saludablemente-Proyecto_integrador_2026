import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColMisEvaluaciones } from './col-mis-evaluaciones';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { EvaluacionService as NutSrv } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { EvaluacionService as AptSrv } from '../../admin/aptitud/evaluacion/evaluacion-service';

const perfil = {
  idUsuario: 1,
  idPersona: 1,
  email: 'ana@saludablemente.pe',
  nombreCompleto: 'Ana Torres',
  documentoIdentidad: '12345678',
  areaTrabajo: 'Enfermería',
  activo: true,
  fechaCreacion: '2026-01-01',
  roles: ['Colaborador'],
};

const nutResumen = {
  idEvaluacion: 11,
  idPersona: 1,
  documentoIdentidad: '12345678',
  nombresCompletos: 'Ana Torres',
  areaTrabajoAlMomento: 'Enfermería',
  idPeriodo: '2026-1',
  fechaEvaluacion: '2026-03-01',
  estadoEvaluacion: 'COMPLETA',
};

const aptResumen = {
  id: 22,
  documentoIdentidadAlMomento: '12345678',
  nombresCompletosAlMomento: 'Ana Torres',
  areaTrabajoAlMomento: 'Enfermería',
  fechaEvaluacion: '2026-04-01',
  estadoEvaluacion: 'PENDIENTE',
  dictamenGlobal: 'APTO',
  fitnessScore: 75,
};

describe('ColMisEvaluaciones', () => {
  let fixture: ComponentFixture<ColMisEvaluaciones>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColMisEvaluaciones],
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: { obtener: () => of(perfil) } },
        { provide: NutSrv, useValue: { historialPorPersona: () => of([nutResumen]) } },
        { provide: AptSrv, useValue: { historial: () => of([aptResumen]) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ColMisEvaluaciones);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('unifica ambos tipos y muestra contadores y tarjetas', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Nutricional: 1');
    expect(el.textContent).toContain('Aptitud: 1');
    expect(el.textContent).toContain('Aptitud física');
    expect(el.querySelectorAll('.eval-card').length).toBe(2);
  });
});
