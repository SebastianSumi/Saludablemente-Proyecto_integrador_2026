import { TestBed } from '@angular/core/testing';
import { ImagenMini } from './imagen-mini';
import { VisorImagenService } from './visor-service';

function servicioEspia() {
  const llamadas: unknown[][] = [];
  const abrir = (...args: unknown[]): void => {
    llamadas.push(args);
  };
  return { llamadas, abrir };
}

describe('ImagenMini', () => {
  it('crea el componente', () => {
    TestBed.configureTestingModule({
      providers: [{ provide: VisorImagenService, useValue: { abrir: () => undefined } }],
    });
    const fixture = TestBed.createComponent(ImagenMini);
    fixture.componentRef.setInput('src', 'https://ejemplo.test/foto.jpg');
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('abre el visor con la imagen al hacer clic', () => {
    const espia = servicioEspia();
    TestBed.configureTestingModule({
      providers: [{ provide: VisorImagenService, useValue: { abrir: espia.abrir } }],
    });
    const fixture = TestBed.createComponent(ImagenMini);
    fixture.componentRef.setInput('src', 'https://ejemplo.test/foto.jpg');
    fixture.componentRef.setInput('titulo', 'Foto');
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.mini-btn') as HTMLButtonElement).click();
    expect(espia.llamadas.length).toBe(1);
    expect(espia.llamadas[0]).toEqual([
      [{ src: 'https://ejemplo.test/foto.jpg', titulo: 'Foto', subtitulo: undefined }],
      0,
    ]);
  });

  it('usa la galería e índice cuando se proveen', () => {
    const espia = servicioEspia();
    TestBed.configureTestingModule({
      providers: [{ provide: VisorImagenService, useValue: { abrir: espia.abrir } }],
    });
    const galeria = [
      { src: 'https://ejemplo.test/1.jpg', titulo: 'Uno' },
      { src: 'https://ejemplo.test/2.jpg', titulo: 'Dos' },
    ];
    const fixture = TestBed.createComponent(ImagenMini);
    fixture.componentRef.setInput('src', 'https://ejemplo.test/2.jpg');
    fixture.componentRef.setInput('galeria', galeria);
    fixture.componentRef.setInput('indice', 1);
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.mini-btn') as HTMLButtonElement).click();
    expect(espia.llamadas[0]).toEqual([galeria, 1]);
  });

  it('no abre nada sin src ni galería', () => {
    const espia = servicioEspia();
    TestBed.configureTestingModule({
      providers: [{ provide: VisorImagenService, useValue: { abrir: espia.abrir } }],
    });
    const fixture = TestBed.createComponent(ImagenMini);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.mini-btn')).toBeNull();
    expect(espia.llamadas.length).toBe(0);
  });

  it('servicio ignora listas vacías', () => {
    TestBed.configureTestingModule({});
    const srv = TestBed.inject(VisorImagenService);
    expect(() => srv.abrir([], 0)).not.toThrow();
  });
});
