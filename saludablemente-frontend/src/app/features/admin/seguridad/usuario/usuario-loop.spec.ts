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

describe('UsuarioList sin bucles', () => {
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
              return of({
                content: [U('ana@upeu.edu.pe'), U('bob@upeu.edu.pe', 2)],
                totalElements: 2,
                totalPages: 1,
                number: 0,
                size: 20,
              });
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

  it('sin tocar nada, las llamadas se estabilizan', async () => {
    await new Promise((r) => setTimeout(r, 1500));
    fixture.detectChanges();
    // eslint-disable-next-line no-console
    console.log(`llamadas tras 1.5s quieto: ${buscarLlamadas}`);
    expect(buscarLlamadas).toBeLessThanOrEqual(2);

    const html = fixture.nativeElement.innerHTML as string;
    const cabeceras = fixture.nativeElement.querySelectorAll('.page-banner');
    // eslint-disable-next-line no-console
    console.log(`cabeceras: ${cabeceras.length}`);
    expect(cabeceras.length).toBe(1);
  });
});
