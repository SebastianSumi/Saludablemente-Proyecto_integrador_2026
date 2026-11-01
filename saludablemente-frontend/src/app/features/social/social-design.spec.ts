import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SocialActivityMeta } from './social-activity-meta';
import { SocialIcon } from './social-icon';
import { SocialText } from './social-text';
import { SocialTextEditor } from './social-text-editor';
import { SocialService } from './social-service';
import { REACCIONES } from './social-reactions';
import { BACKEND } from './testing/backend-fixtures';
import { of } from 'rxjs';

describe('Social · adaptación MaterialM y metadatos', () => {
  it('el texto corto no queda recortado sin una acción para expandirlo', async () => {
    await TestBed.configureTestingModule({
      imports: [SocialText],
      providers: [provideRouter([])],
    }).compileComponents();
    const f = TestBed.createComponent(SocialText);
    f.componentRef.setInput('text', 'Una experiencia breve para compartir.');
    f.componentRef.setInput('truncate', true);
    f.detectChanges();
    expect(f.nativeElement.querySelector('.collapsed')).toBeNull();
    expect(f.nativeElement.querySelector('button')).toBeNull();
  });
  it('conserva siete reacciones con iconos y sin emojis', () => {
    expect(REACCIONES.map((x) => x.tipo)).toEqual([
      'LIKE',
      'LOVE',
      'HAHA',
      'WOW',
      'SAD',
      'APLAUSO',
      'ENOJADO',
    ]);
    for (const option of REACCIONES) {
      expect(option.icon).toMatch(/^[a-z-]+$/);
      expect('emoji' in option).toBe(false);
    }
  });
  it.each(['surprised', 'applause'])('glifo %s usa SVG propio, no texto emoji', async (name) => {
    await TestBed.configureTestingModule({ imports: [SocialIcon] }).compileComponents();
    const f = TestBed.createComponent(SocialIcon);
    f.componentRef.setInput('name', name);
    f.detectChanges();
    expect(f.nativeElement.querySelector('svg')).toBeTruthy();
    expect(f.nativeElement.textContent.trim()).toBe('');
  });
  it('presenta la actividad en español con etiquetas e iconos', async () => {
    await TestBed.configureTestingModule({ imports: [SocialActivityMeta] }).compileComponents();
    const f = TestBed.createComponent(SocialActivityMeta);
    f.componentRef.setInput('post', {
      ...BACKEND.muro.pinned[0],
      tipoContenido: 'ACTIVIDAD',
      fechaEvento: '2026-10-29',
      horaInicio: '19:06:00',
      horaFin: '20:06:00',
      lugar: 'Fernando Stall',
      estado: 'PROGRAMADA',
      coordinadorNombre: 'Eulalia Soncco Ortiz',
    });
    f.detectChanges();
    const text = f.nativeElement.textContent;
    expect(text).toContain('Programada');
    expect(text).toContain('oct');
    expect(text).not.toContain('October');
    expect(text).toContain('19:06');
    expect(text).not.toContain('19:06:00');
    expect(text).toContain('Fernando Stall');
    expect(text).toContain('Coordina');
    expect(text).toContain('Eulalia Soncco Ortiz');
    expect(f.nativeElement.querySelectorAll('social-icon').length).toBe(4);
  });
  it('no deja etiquetas de campos vacíos en la actividad', async () => {
    await TestBed.configureTestingModule({ imports: [SocialActivityMeta] }).compileComponents();
    const f = TestBed.createComponent(SocialActivityMeta);
    f.componentRef.setInput('post', {
      ...BACKEND.muro.pinned[0],
      fechaEvento: null,
      horaInicio: null,
      horaFin: null,
      lugar: null,
      coordinadorNombre: null,
    });
    f.detectChanges();
    expect(f.nativeElement.querySelectorAll('dt').length).toBe(0);
  });
  it('Ver más expande el texto localmente sin pedir un detalle ni registrar vistas', async () => {
    await TestBed.configureTestingModule({
      imports: [SocialText],
      providers: [provideRouter([])],
    }).compileComponents();
    const f = TestBed.createComponent(SocialText);
    f.componentRef.setInput('text', 'Una experiencia para compartir. '.repeat(12));
    f.componentRef.setInput('truncate', true);
    f.detectChanges();
    expect(f.nativeElement.querySelector('.collapsed')).toBeTruthy();
    f.nativeElement.querySelector('button').click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('.collapsed')).toBeNull();
    expect(f.nativeElement.querySelector('button')).toBeNull();
  });
  it('ofrece un acceso visible a hashtags y coloca # en el texto', async () => {
    await TestBed.configureTestingModule({
      imports: [SocialTextEditor],
      providers: [{ provide: SocialService, useValue: { sugerir: () => of([]) } }],
    }).compileComponents();
    const f = TestBed.createComponent(SocialTextEditor);
    f.componentRef.setInput('value', '');
    f.detectChanges();
    let value = '';
    f.componentInstance.valueChange.subscribe((x) => (value = x));
    const button = Array.from(f.nativeElement.querySelectorAll('button')).find(
      (b: unknown) => (b as HTMLButtonElement).textContent?.trim() === 'Hashtag',
    ) as HTMLButtonElement;
    expect(button).toBeTruthy();
    button.click();
    expect(value).toBe('#');
    expect(f.nativeElement.textContent).toContain('#Salud');
  });
});
