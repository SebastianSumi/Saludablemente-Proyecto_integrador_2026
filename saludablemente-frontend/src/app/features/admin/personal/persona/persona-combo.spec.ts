import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { TuiRoot, provideTaiga } from '@taiga-ui/core';
import { PersonaList } from './persona-list';
import { PersonaService } from './persona-service';
import { TeamService } from '../../teams/team-service';
import { AreaService } from '../../catalogo/area/area-service';

@Component({
  template: '<tui-root><app-persona-list /></tui-root>',
  imports: [TuiRoot, PersonaList],
})
class Host {}

const AREAS = [
  { id: 1, nombre: 'Limpieza' },
  { id: 2, nombre: 'Logística' },
  { id: 3, nombre: 'DTI-Redes' },
];

describe('PersonaList autocompletado', () => {
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
      imports: [PersonaList],
      providers: [
        provideTaiga(),
        { provide: PersonaService, useValue: { listar: () => of([]), inactivar: () => of(undefined) } },
        { provide: TeamService, useValue: { listar: () => of([]) } },
        { provide: AreaService, useValue: { listar: () => of(AREAS) } },
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

  function inputDeFiltro(etiqueta: string): HTMLInputElement {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('label') as NodeListOf<HTMLElement>
    );
    const label = labels.find((el: HTMLElement) => (el.textContent ?? '').includes(etiqueta));
    if (!label) throw new Error(`No hay filtro ${etiqueta}`);
    const input = label.querySelector('input');
    if (!input) throw new Error(`Sin input en ${etiqueta}`);
    return input as HTMLInputElement;
  }

  function opcionesVisibles(): string[] {
    return Array.from(document.body.querySelectorAll('button[tuiOption]')).map((el) =>
      ((el as HTMLElement).textContent ?? '').trim()
    );
  }

  it('al escribir "lim" solo aparece Limpieza', async () => {
    const input = inputDeFiltro('Área');
    input.focus();
    input.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    input.value = 'lim';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    const opciones = opcionesVisibles();
    // eslint-disable-next-line no-console
    console.log('opciones tras "lim":', JSON.stringify(opciones));
    expect(opciones.length).toBeGreaterThan(0);
    expect(opciones.every((t) => t.toLowerCase().includes('lim'))).toBe(true);
    expect(opciones.some((t) => t.includes('Limpieza'))).toBe(true);
  });
});
