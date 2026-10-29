import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import {
  COL_MODULOS,
  ColInicio,
  calcularPorcentajeMeta,
} from './col-inicio';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { EvaluacionService as EvalNutricionalService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { EvaluacionService as EvalAptitudService } from '../../admin/aptitud/evaluacion/evaluacion-service';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import { MetaService } from '../../admin/metas/meta-service';
import { AsistenciaService } from '../../admin/asistencias/asistencia-service';
import { AgendaService } from '../../admin/actividades/agenda-service';
import { NotificacionService } from '../notificaciones/notificacion-service';
import { NoticiaPublicaService } from '../../admin/noticias/noticia-service';

const alertaPendiente = {
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

const metaActiva = {
  id: 1,
  idPersona: 1,
  pilar: 'NUTRICION',
  modalidad: 'CUANTITATIVA',
  tipoOrigen: 'PERSONAL',
  tipoCalculo: 'ULTIMO_VALOR',
  indicador: 'peso',
  descripcion: 'Bajar de peso',
  valorInicial: 80,
  valorActual: 75,
  valorObjetivo: 70,
  fechaInicio: '2026-09-01',
  fechaObjetivo: '2026-12-01',
  estado: 'ACTIVA',
};

describe('ColInicio', () => {
  let fixture: ComponentFixture<ColInicio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColInicio],
      providers: [
        provideRouter([]),
        {
          provide: PerfilService,
          useValue: {
            obtener: () =>
              of({
                idUsuario: 1,
                idPersona: 1,
                email: 'ana@saludablemente.pe',
                nombreCompleto: 'Ana Torres',
                documentoIdentidad: '12345678',
                areaTrabajo: 'Enfermería',
                activo: true,
                fechaCreacion: '2026-01-01',
                roles: ['Colaborador'],
              }),
          },
        },
        {
          provide: EvalNutricionalService,
          useValue: { historialPorPersona: () => of([]) },
        },
        {
          provide: EvalAptitudService,
          useValue: { historial: () => of([]) },
        },
        {
          provide: AlertaClinicaService,
          useValue: { listar: () => of([alertaPendiente]) },
        },
        {
          provide: MetaService,
          useValue: { listarPorPersona: () => of([metaActiva]) },
        },
        {
          provide: AsistenciaService,
          useValue: { historialPorPersona: () => of([]) },
        },
        { provide: AgendaService, useValue: { deHoy: () => of([]) } },
        {
          provide: NotificacionService,
          useValue: { contadorNoLeidas: () => of(3) },
        },
        {
          provide: NoticiaPublicaService,
          useValue: { feed: () => of({ content: [] }) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ColInicio);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the KPI cards, alert list and one card per module', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.card.mini').length).toBe(4);
    expect(el.querySelectorAll('.modulo-card').length).toBe(COL_MODULOS.length);
    expect(el.textContent).toContain('Mis alertas pendientes');
    expect(el.textContent).toContain('Ana Torres');
    expect(el.textContent).toContain('IMC elevado');
    expect(el.querySelector('.pill-error')?.textContent).toContain('CRITICA');
  });

  it('calcula el porcentaje de avance de una meta', () => {
    expect(
      calcularPorcentajeMeta({ valorInicial: 80, valorActual: 75, valorObjetivo: 70 }),
    ).toBe(50);
    expect(
      calcularPorcentajeMeta({ valorInicial: 0, valorActual: 10, valorObjetivo: 0 }),
    ).toBe(100);
    expect(
      calcularPorcentajeMeta({ valorInicial: 0, valorActual: 20, valorObjetivo: 10 }),
    ).toBe(100);
  });
});
