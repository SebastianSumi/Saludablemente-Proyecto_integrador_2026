import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColAlertaList } from './alerta-list';
import { ColAlertaService } from './alerta-service';
import type { AlertaClinica } from './alerta.model';

function alerta(over: Partial<AlertaClinica> = {}): AlertaClinica {
  return {
    idAlerta: 1,
    personaId: 7,
    evaluacionOrigenId: 3,
    tipoEvaluacionOrigen: 'NUTRICIONAL',
    puntajeTotal: 10,
    nivelSeveridad: 'LEVE',
    mensajeResumen: '',
    estado: 'PENDIENTE',
    fechaGeneracion: '2026-01-01T10:00:00',
    fechaAtencion: null,
    usuarioAtiendeId: null,
    observacionAtencion: null,
    detalles: [],
    ...over,
  };
}

describe('ColAlertaList', () => {
  it('deriva sus filtros de las etiquetas propias y filtra tarjetas por código', () => {
    const data = [
      alerta({ idAlerta: 1, etiquetas: [{ idEtiqueta: 1, nombre: 'Riesgo metabólico', codigo: 'RIESGO', colorHex: '#123456', asignadaPor: 'MOTOR', fechaAsignacion: '' }] }),
      alerta({ idAlerta: 2, etiquetas: [{ idEtiqueta: 2, nombre: 'Seguimiento', codigo: 'SEGUIMIENTO', colorHex: '#654321', asignadaPor: 'MANUAL', fechaAsignacion: '' }] }),
    ];
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: ColAlertaService, useValue: { listarMias: () => of(data) } }] });
    const fixture = TestBed.createComponent(ColAlertaList); fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('.alerta-card').length).toBe(2);
    const select = element.querySelector('.etiqueta-select') as HTMLSelectElement;
    expect(select.options.length).toBe(3);
    select.value = 'SEGUIMIENTO'; select.dispatchEvent(new Event('change')); fixture.detectChanges();
    expect(element.querySelectorAll('.alerta-card').length).toBe(1);
    expect(element.querySelector('.alerta-card')?.textContent).toContain('Alerta #2');
    expect(element.querySelector('a[href*="/alertaclinica/etiquetas"]')).toBeNull();
  });

  it('incluye las etiquetas en la búsqueda y mantiene la presentación al cambiar a lista', async () => {
    const data = [alerta({ etiquetas: [{ idEtiqueta: 1, nombre: 'Riesgo metabólico', codigo: 'RIESGO', colorHex: '#123456', asignadaPor: 'MOTOR', fechaAsignacion: '' }] }), alerta({ idAlerta: 2 })];
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: ColAlertaService, useValue: { listarMias: () => of(data) } }] });
    const fixture = TestBed.createComponent(ColAlertaList); fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const buscar = element.querySelector('input[placeholder="Mensaje, indicador o etiqueta"]') as HTMLInputElement;
    buscar.value = 'metabólico'; buscar.dispatchEvent(new Event('input'));
    await new Promise(resolve => setTimeout(resolve, 350)); fixture.detectChanges();
    expect(element.querySelectorAll('.alerta-card').length).toBe(1);
    const lista = [...element.querySelectorAll('button')].find(b => b.textContent?.trim() === 'Lista')!;
    lista.click(); fixture.detectChanges();
    expect(element.querySelector('.etiquetas-columna')?.textContent).toContain('Riesgo metabólico');
  });

  it('crea el componente y carga solo alertas propias', () => {
    const calls: unknown[][] = [];
    const svc = {
      listarMias: (...args: unknown[]) => {
        calls.push(args);
        return of<AlertaClinica[]>([]);
      },
    };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: ColAlertaService, useValue: svc }],
    });
    const fixture = TestBed.createComponent(ColAlertaList);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect(calls.length).toBeGreaterThan(0);
  });

  it('muestra contadores y filtra por estado pendiente por defecto', () => {
    const data = [
      alerta({ idAlerta: 1, estado: 'PENDIENTE', nivelSeveridad: 'CRITICA' }),
      alerta({ idAlerta: 2, estado: 'PENDIENTE', nivelSeveridad: 'LEVE' }),
      alerta({ idAlerta: 3, estado: 'ATENDIDA', nivelSeveridad: 'MODERADA' }),
    ];
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: ColAlertaService, useValue: { listarMias: () => of(data) } }],
    });
    const fixture = TestBed.createComponent(ColAlertaList);
    fixture.detectChanges();
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Pendientes (2)');
    expect(texto).toContain('Atendidas (1)');
    expect(texto).toContain('Crítica (1)');
  });
});
