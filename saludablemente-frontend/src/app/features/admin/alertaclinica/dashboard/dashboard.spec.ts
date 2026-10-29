import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ThemeService } from '../../../../core/theme/theme-service';
import {
  Dashboard,
  agruparPorDia,
  agruparPorRango,
  construirOpcionesOrigen,
  construirOpcionesSeveridad,
  construirOpcionesTendencia,
  contarPorOrigen,
  contarPorSeveridad,
  rangoFechasPeriodo,
  tasaAtencion,
  temaGrafico,
  topPersonasEnRiesgo,
  type TemaGrafico,
} from './dashboard';
import { AlertaClinica } from '../alerta/alerta-clinica.model';

const alertaBase = (parche: Partial<AlertaClinica>): AlertaClinica => ({
  idAlerta: 1,
  personaId: 21,
  evaluacionOrigenId: 5,
  tipoEvaluacionOrigen: 'NUTRICIONAL',
  puntajeTotal: 10,
  nivelSeveridad: 'CRITICA',
  mensajeResumen: 'IMC elevado',
  estado: 'PENDIENTE',
  fechaGeneracion: new Date().toISOString(),
  fechaAtencion: null,
  usuarioAtiendeId: null,
  observacionAtencion: null,
  detalles: [],
  ...parche,
});

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: ThemeService, useValue: { enOscuro: () => false } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

describe('Dashboard funciones puras', () => {
  const alertas: AlertaClinica[] = [
    alertaBase({ idAlerta: 1, personaId: 21, nivelSeveridad: 'CRITICA', estado: 'PENDIENTE', puntajeTotal: 12 }),
    alertaBase({ idAlerta: 2, personaId: 21, nivelSeveridad: 'MODERADA', estado: 'PENDIENTE', puntajeTotal: 6 }),
    alertaBase({ idAlerta: 3, personaId: 22, nivelSeveridad: 'LEVE', estado: 'PENDIENTE', tipoEvaluacionOrigen: 'APTITUD_FISICA', puntajeTotal: 2 }),
    alertaBase({ idAlerta: 4, personaId: 22, nivelSeveridad: 'LEVE', estado: 'ATENDIDA', puntajeTotal: 1 }),
  ];

  it('contarPorSeveridad agrupa correctamente', () => {
    expect(contarPorSeveridad(alertas)).toEqual({ LEVE: 2, MODERADA: 1, CRITICA: 1 });
  });

  it('contarPorOrigen separa nutricional de aptitud', () => {
    expect(contarPorOrigen(alertas)).toEqual({ NUTRICIONAL: 3, APTITUD_FISICA: 1 });
  });

  it('tasaAtencion calcula el porcentaje de atendidas', () => {
    expect(tasaAtencion(alertas)).toBe(25);
    expect(tasaAtencion([])).toBe(0);
  });

  it('agruparPorDia genera N días con totales coherentes', () => {
    const t = agruparPorDia(alertas, 7);
    expect(t.dias.length).toBe(7);
    const total = [...t.leve, ...t.moderada, ...t.critica].reduce((a, b) => a + b, 0);
    expect(total).toBe(4);
  });

  it('topPersonasEnRiesgo ordena por críticas y usa nombres', () => {
    const nombres = new Map([
      [21, 'Ana Lazaro'],
      [22, 'Eulalia Soncco'],
    ]);
    const top = topPersonasEnRiesgo(alertas, nombres, 8);
    expect(top.length).toBe(2);
    expect(top[0].personaId).toBe(21);
    expect(top[0].nombre).toBe('Ana Lazaro');
    expect(top[0].criticas).toBe(1);
    expect(top[1].idAlertaReciente).toBe(3);
  });

  it('topPersonasEnRiesgo excluye atendidas y usa fallback', () => {
    const top = topPersonasEnRiesgo(alertas, new Map(), 8);
    expect(top.find((t) => t.personaId === 22)?.nombre).toBe('Persona #22');
    expect(top.every((t) => t.total >= 1)).toBe(true);
  });

  it('rangoFechasPeriodo cubre el periodo completo', () => {
    const r = rangoFechasPeriodo({
      codigo: '2026-I',
      descripcion: 'Primer semestre',
      fechaInicio: '2026-01-01',
      fechaFin: '2026-06-30',
    });
    const d = new Date(r.desde);
    expect([d.getFullYear(), d.getMonth(), d.getDate()]).toEqual([2026, 0, 1]);
    const h = new Date(r.hasta);
    expect([h.getFullYear(), h.getMonth(), h.getDate()]).toEqual([2026, 5, 30]);
    expect(d.getTime()).toBeLessThan(h.getTime());
  });

  it('agruparPorRango agrega por día en rangos cortos', () => {
    const t = agruparPorRango(alertas, new Date('2026-01-01'), new Date('2026-01-07'));
    expect(t.dias.length).toBe(7);
    expect(t.dias[0]).toContain('/');
  });

  it('temaGrafico devuelve paleta completa', () => {
    const t = temaGrafico();
    expect(t.texto.length).toBeGreaterThan(0);
    expect(t.titulo.length).toBeGreaterThan(0);
    expect(t.linea.length).toBeGreaterThan(0);
    expect(t.fondo.length).toBeGreaterThan(0);
    expect(t.borde.length).toBeGreaterThan(0);
  });

  it('los builders usan el tema inyectado en ejes, leyenda y tooltip', () => {
    const tema: TemaGrafico = {
      texto: '#aaaaaa',
      titulo: '#bbbbbb',
      linea: '#cccccc',
      fondo: '#dddddd',
      borde: '#eeeeee',
    };
    const jsonTendencia = JSON.stringify(
      construirOpcionesTendencia({ dias: ['01/01'], leve: [1], moderada: [0], critica: [0] }, tema),
    );
    expect(jsonTendencia.includes('#aaaaaa')).toBe(true);
    expect(jsonTendencia.includes('#dddddd')).toBe(true);
    const jsonSev = JSON.stringify(construirOpcionesSeveridad({ LEVE: 1, MODERADA: 0, CRITICA: 0 }, tema));
    expect(jsonSev.includes('#aaaaaa')).toBe(true);
    expect(jsonSev.includes('#dddddd')).toBe(true);
    const jsonOrigen = JSON.stringify(
      construirOpcionesOrigen({ NUTRICIONAL: 2, APTITUD_FISICA: 1 }, tema),
    );
    expect(jsonOrigen.includes('#aaaaaa')).toBe(true);
    expect(jsonOrigen.includes('#cccccc')).toBe(true);
  });

  it('agruparPorRango agrega por semana en semestres', () => {
    const muchas: typeof alertas = [];
    for (let i = 0; i < 40; i++) {
      muchas.push(
        alertaBase({ idAlerta: 100 + i, fechaGeneracion: `2026-02-${String((i % 27) + 1).padStart(2, '0')}T10:00:00` }),
      );
    }
    const t = agruparPorRango(muchas, new Date('2026-01-01'), new Date('2026-06-30'));
    expect(t.dias.length).toBeLessThan(31);
    expect(t.dias[0].startsWith('sem ')).toBe(true);
    const total = [...t.leve, ...t.moderada, ...t.critica].reduce((a, b) => a + b, 0);
    expect(total).toBe(40);
  });
});
