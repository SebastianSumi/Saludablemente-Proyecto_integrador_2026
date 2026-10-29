import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { of } from 'rxjs';
import {
  AdminHome,
  construirOpcionesResumen,
  construirOpcionesSeveridad,
  contarPorSeveridad,
} from './admin-home';
import { AuthService } from '../../../core/auth/auth-service';
import { PersonaService } from '../personal/persona/persona-service';
import { TeamService } from '../teams/team-service';
import { EvaluacionService } from '../nutricional/evaluacion/evaluacion-service';
import { AlertaClinicaService } from '../alertaclinica/alerta/alerta-clinica-service';
import { AlertaClinica } from '../alertaclinica/alerta/alerta-clinica.model';
import { ADMIN_MODULOS } from './admin-modulos';

const alertaBase: AlertaClinica = {
  idAlerta: 1,
  personaId: 1,
  evaluacionOrigenId: 1,
  tipoEvaluacionOrigen: 'NUTRICIONAL',
  puntajeTotal: 8,
  nivelSeveridad: 'CRITICA',
  mensajeResumen: 'IMC elevado',
  estado: 'PENDIENTE',
  fechaGeneracion: '2026-10-01T10:00:00',
  fechaAtencion: null,
  usuarioAtiendeId: null,
  observacionAtencion: null,
  detalles: [],
};

describe('AdminHome', () => {
  let fixture: ComponentFixture<AdminHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHome],
      providers: [
        provideRouter([]),
        provideEchartsCore({ echarts }),
        {
          provide: AuthService,
          useValue: {
            obtenerNombre: () => 'Admin Test',
            obtenerRoles: () => ['Administrador'],
            tieneAlgunRol: () => true,
          },
        },
        {
          provide: PersonaService,
          useValue: {
            listar: () =>
              of([
                { id: 1, activo: true },
                { id: 2, activo: false },
              ]),
          },
        },
        { provide: TeamService, useValue: { listar: () => of([{ id: 1 }]) } },
        {
          provide: EvaluacionService,
          useValue: { agregados: () => of({ total: 4 }) },
        },
        {
          provide: AlertaClinicaService,
          useValue: { listar: () => of([alertaBase]) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminHome);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render one card per module plus dashboard widgets', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Resumen general');
    expect(el.querySelectorAll('.modulo-card').length).toBe(ADMIN_MODULOS.length);
    expect(el.textContent).toContain('Notificaciones');
    expect(el.textContent).toContain('IMC elevado');
    expect(el.querySelectorAll('.tile').length).toBeGreaterThan(9);
    expect(el.querySelector('.pill-error')?.textContent).toContain('CRITICA');
  });

  it('contarPorSeveridad agrupa correctamente', () => {
    const r = contarPorSeveridad([
      alertaBase,
      { ...alertaBase, idAlerta: 2, nivelSeveridad: 'LEVE' },
      { ...alertaBase, idAlerta: 3, nivelSeveridad: 'MODERADA' },
    ]);
    expect(r).toEqual({ LEVE: 1, MODERADA: 1, CRITICA: 1 });
  });

  it('construye opciones de gráficos con datos reales', () => {
    const resumen = construirOpcionesResumen({
      personas: 2,
      personasActivas: 1,
      equipos: 1,
      evaluaciones: 4,
      alertasPendientes: 1,
      alertasCriticas: 1,
    });
    expect(JSON.stringify(resumen)).toContain('Personas');
    const sev = construirOpcionesSeveridad({
      LEVE: 1,
      MODERADA: 1,
      CRITICA: 1,
    });
    expect(JSON.stringify(sev)).toContain('Críticas');
  });
});
