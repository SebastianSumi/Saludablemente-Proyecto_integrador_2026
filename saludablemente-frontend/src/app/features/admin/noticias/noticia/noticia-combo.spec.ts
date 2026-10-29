import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { NoticiaList } from './noticia-list';
import { NoticiaService } from '../noticia-service';
import { CategoriaNoticiaService } from '../categoria-service';

@Component({
  template: '<tui-root><app-noticia-list /></tui-root>',
  imports: [TuiRoot, NoticiaList],
})
class Host {}

describe('NoticiaList autocompletado', () => {
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
      imports: [NoticiaList],
      providers: [
        provideTaiga(),
        {
          provide: NoticiaService,
          useValue: { listar: () => of({ content: [], totalElements: 0, totalPages: 1, number: 0, size: 20 }) },
        },
        { provide: CategoriaNoticiaService, useValue: { listarTodas: () => of([]) } },
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

  it('el input de Estado acepta texto y filtra', async () => {
    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('label') as NodeListOf<HTMLElement>
    );
    const label = labels.find((el: HTMLElement) => (el.textContent ?? '').includes('Estado'));
    if (!label) throw new Error('No hay filtro Estado');
    const input = label.querySelector('input') as HTMLInputElement;
    // eslint-disable-next-line no-console
    console.log('readonly=', input.readOnly, 'disabled=', input.disabled);
    expect(input.readOnly).toBe(false);
    expect(input.disabled).toBe(false);

    input.focus();
    input.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    input.value = 'pub';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 300));
    fixture.detectChanges();

    expect(input.value).toBe('pub');
    const opciones = Array.from(document.body.querySelectorAll('button[tuiOption]')).map((el) =>
      ((el as HTMLElement).textContent ?? '').trim()
    );
    // eslint-disable-next-line no-console
    console.log('opciones tras "pub":', JSON.stringify(opciones));
    expect(opciones).toEqual(['Publicado']);
  });
});
