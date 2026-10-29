import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EtiquetasClinicas } from './etiquetas-clinicas';

describe('EtiquetasClinicas de consulta', () => {
  it('muestra origen y fecha sin ofrecer enlaces o acciones administrativos', () => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(EtiquetasClinicas);
    fixture.componentRef.setInput('detallado', true);
    fixture.componentRef.setInput('etiquetas', [
      { idEtiqueta: 1, nombre: 'Riesgo metabólico', codigo: 'RIESGO', colorHex: '#123456', asignadaPor: 'MOTOR', fechaAsignacion: '2026-10-20T09:30:00' },
      { idEtiqueta: 2, nombre: 'Seguimiento', codigo: 'SEGUIMIENTO', colorHex: '#654321', asignadaPor: 'MANUAL', fechaAsignacion: '2026-10-20T10:00:00' },
    ]);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Automática');
    expect(element.textContent).toContain('Asignada por el equipo');
    expect(element.textContent).toContain('20/10/2026');
    expect(element.querySelectorAll('button,a').length).toBe(0);
  });
  it('presenta el estado vacío y rechaza colores fuera del formato permitido', () => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(EtiquetasClinicas); fixture.componentRef.setInput('detallado', true); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('todavía no tiene etiquetas');
    expect(fixture.componentInstance.color('url(https://example.test)')).toBe('var(--sal-primary)');
  });
});
