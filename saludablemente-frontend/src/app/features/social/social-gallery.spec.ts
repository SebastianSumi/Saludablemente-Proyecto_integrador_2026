import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { SocialGallery } from './social-gallery';
import { SocialModal } from './social-modal';
import { imagenesPublicacion } from './social.utils';
import { BACKEND } from './testing/backend-fixtures';

@Component({ imports: [TuiRoot], template: '<tui-root />' })
class GalleryRoot {}
describe('Social · mosaico y visor', () => {
  beforeEach(() =>
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    }),
  );
  async function create(count: number) {
    await TestBed.configureTestingModule({
      imports: [SocialGallery, GalleryRoot],
      providers: [provideTaiga()],
    })
      .overrideComponent(SocialModal, { set: { template: '<ng-content />', imports: [] } })
      .compileComponents();
    const root = TestBed.createComponent(GalleryRoot);
    root.detectChanges();
    const f = TestBed.createComponent(SocialGallery);
    f.componentRef.setInput(
      'urls',
      Array.from({ length: count }, (_, i) => 'https://example.invalid/foto' + i + '.jpg'),
    );
    f.componentRef.setInput('title', 'Primera actividad');
    f.detectChanges();
    await f.whenStable();
    f.detectChanges();
    return f;
  }
  it.each([0, 1, 2, 3, 4, 5, 6, 15])(
    'muestra máximo cinco fotos y calcula +N para %i',
    async (count) => {
      const f = await create(count);
      expect(f.nativeElement.querySelectorAll('.photo-tile').length).toBe(Math.min(count, 5));
      const extra = f.nativeElement.querySelector('.photo-extra');
      if (count > 5) expect(extra.textContent.trim()).toBe('+' + (count - 5));
      else expect(extra).toBeNull();
    },
  );
  it.each([
    [600, 1200, 'portrait'],
    [1600, 900, 'landscape'],
    [900, 900, 'square'],
  ])('usa dimensiones naturales %i × %i para el mosaico', async (width, height, orientation) => {
    const f = await create(3);
    const img = f.nativeElement.querySelector('img');
    Object.defineProperty(img, 'naturalWidth', { value: width });
    Object.defineProperty(img, 'naturalHeight', { value: height });
    img.dispatchEvent(new Event('load'));
    f.detectChanges();
    expect(f.nativeElement.querySelector('.photo-grid').dataset.orientation).toBe(orientation);
  });
  it('abre la foto seleccionada, incluye todas las miniaturas y navega/ajusta zoom', async () => {
    const f = await create(15);
    f.nativeElement.querySelectorAll('.photo-tile')[4].click();
    f.detectChanges();
    await f.whenStable();
    f.detectChanges();
    expect(f.nativeElement.querySelectorAll('.photo-thumbs button').length).toBe(15);
    expect(f.nativeElement.querySelector('[role="status"]').textContent).toContain(
      'Imagen 5 de 15',
    );
    f.nativeElement.querySelector('[aria-label="Imagen siguiente"]').click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('[role="status"]').textContent).toContain(
      'Imagen 6 de 15',
    );
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft' }));
    f.detectChanges();
    expect(f.nativeElement.querySelector('[role="status"]').textContent).toContain(
      'Imagen 5 de 15',
    );
    f.nativeElement.querySelector('[aria-label="Acercar imagen"]').click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('[aria-label="Ajustar imagen"]').textContent).toContain(
      '150',
    );
    f.nativeElement.querySelector('[aria-label="Ir a imagen 15"]').click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('[role="status"]').textContent).toContain(
      'Imagen 15 de 15',
    );
    expect(f.nativeElement.querySelector('[aria-label="Ajustar imagen"]').textContent).toContain(
      '100',
    );
    f.nativeElement.querySelector('[aria-label="Imagen siguiente"]').click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('[role="status"]').textContent).toContain(
      'Imagen 1 de 15',
    );
  });
  it('cierra el visor con Escape', async () => {
    const f = await create(1);
    f.nativeElement.querySelector('.photo-tile').click();
    f.detectChanges();
    await f.whenStable();
    f.detectChanges();
    expect(f.nativeElement.querySelector('.photo-viewer')).toBeTruthy();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', cancelable: true }));
    f.detectChanges();
    expect(f.nativeElement.querySelector('.photo-viewer')).toBeNull();
  });
  it('conserva el marco si una imagen falla y permite abrir las demás', async () => {
    const f = await create(2);
    f.nativeElement.querySelector('img').dispatchEvent(new Event('error'));
    f.detectChanges();
    expect(f.nativeElement.querySelectorAll('.photo-tile').length).toBe(2);
    expect(f.nativeElement.querySelector('.photo-failed').textContent).toContain(
      'Imagen no disponible',
    );
    f.nativeElement.querySelectorAll('.photo-tile')[1].click();
    f.detectChanges();
    expect(f.nativeElement.querySelector('.image-canvas img').src).toContain('foto1.jpg');
  });
  it('deduplica imágenes y no convierte el logo de actividad en foto', () => {
    const post = {
      ...BACKEND.muro.pinned[0],
      tipoContenido: 'ACTIVIDAD' as const,
      imagenUrl: 'logo.png',
      imagenes: [],
    };
    expect(imagenesPublicacion(post)).toEqual([]);
    expect(imagenesPublicacion({ ...post, imagenes: ['a.jpg', 'a.jpg', 'b.jpg'] })).toEqual([
      'a.jpg',
      'b.jpg',
    ]);
    expect(imagenesPublicacion({ ...post, tipoContenido: 'NOTICIA' })).toEqual(['logo.png']);
  });
});
