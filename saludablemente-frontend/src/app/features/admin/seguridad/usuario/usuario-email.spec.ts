import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { UsuarioList } from './usuario-list';
import { UsuarioService } from '../usuario-service';

@Component({
  template: '<tui-root><app-usuario-list /></tui-root>',
  imports: [TuiRoot, UsuarioList],
})
class Host {}

const U = (email: string, id = 1) => ({
  id,
  email,
  activo: true,
  idPersona: 1,
  roles: [],
});

describe('UsuarioList filtro email', () => {
  let fixture: ComponentFixture<Host>;
  let buscarLlamadas = 0;

  beforeEach(async () => {
    buscarLlamadas = 0;
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
      imports: [UsuarioList],
      providers: [
        provideTaiga(),
        {
          provide: UsuarioService,
          useValue: {
            buscar: () => {
              buscarLlamadas++;
              return of({ content: [U('ana@upeu.edu.pe')], totalElements: 1, totalPages: 1, number: 0, size: 20 });
            },
          },
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

  function inputEmail(): HTMLInputElement {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('label') as NodeListOf<HTMLElement>
    );
    const label = labels.find((el: HTMLElement) => (el.textContent ?? '').includes('Email'));
    if (!label) throw new Error('No hay filtro Email');
    return label.querySelector('input') as HTMLInputElement;
  }

  it('escribir NO consulta al backend', async () => {
    const antes = buscarLlamadas;
    const input = inputEmail();
    input.focus();
    input.value = 'ana';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 200));
    expect(buscarLlamadas).toBe(antes);
  });

  it('Enter con texto filtra la tabla', async () => {
    const antes = buscarLlamadas;
    const input = inputEmail();
    input.value = 'ana@upeu.edu.pe';
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 100));
    expect(buscarLlamadas).toBeGreaterThan(antes);
  });
});
