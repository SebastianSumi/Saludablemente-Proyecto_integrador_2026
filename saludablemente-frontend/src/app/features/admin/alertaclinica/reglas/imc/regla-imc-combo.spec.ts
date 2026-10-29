import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { ReglaImcList } from './regla-imc-list';
import { ReglaImcService } from './regla-imc-service';

@Component({
  template: '<tui-root><app-regla-imc-list /></tui-root>',
  imports: [TuiRoot, ReglaImcList],
})
class Host {}

describe('ReglaImcList autocompletado', () => {
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
      imports: [ReglaImcList],
      providers: [
        provideTaiga(),
        { provide: ReglaImcService, useValue: { listar: () => of([]) } },
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

  it('al escribir "inact" solo aparece Inactivo', async () => {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('label') as NodeListOf<HTMLElement>
    );
    const label = labels.find((el: HTMLElement) => (el.textContent ?? '').includes('Estado'));
    if (!label) throw new Error('No hay filtro Estado');
    const input = label.querySelector('input') as HTMLInputElement;
    input.focus();
    input.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    input.value = 'inact';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    const opciones = Array.from(document.body.querySelectorAll('button[tuiOption]')).map((el) =>
      ((el as HTMLElement).textContent ?? '').trim()
    );
    // eslint-disable-next-line no-console
    console.log('opciones tras "inact":', JSON.stringify(opciones));
    expect(opciones.length).toBeGreaterThan(0);
    expect(opciones).toEqual(['Inactivo']);
  });
});
