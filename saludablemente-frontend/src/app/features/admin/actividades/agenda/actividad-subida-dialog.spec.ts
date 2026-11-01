import { TestBed } from '@angular/core/testing';
import { provideTaiga } from '@taiga-ui/core';
import { ActividadSubidaDialog } from './actividad-subida-dialog';

describe('Actividad · confirmación de subida separada del detalle', () => {
  beforeEach(() => {
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      value: () => ({ matches: false, addEventListener() {}, removeEventListener() {} }),
    });
  });
  async function create() {
    await TestBed.configureTestingModule({
      imports: [ActividadSubidaDialog],
      providers: [provideTaiga()],
    }).compileComponents();
    const fixture = TestBed.createComponent(ActividadSubidaDialog);
    fixture.componentRef.setInput('fileName', 'actividad.webp');
    fixture.componentRef.setInput('sizeLabel', '150 KB');
    fixture.componentRef.setInput('preview', 'data:image/webp;base64,AA==');
    fixture.componentRef.setInput('description', 'Descripción inicial');
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture;
  }
  it('conserva archivo, vista previa, edición de descripción y confirmación hacia el padre', async () => {
    const f = await create();
    const confirm = vi.fn(),
      description = vi.fn();
    f.componentInstance.confirm.subscribe(confirm);
    f.componentInstance.descriptionChange.subscribe(description);
    expect(f.nativeElement.textContent).toContain('actividad.webp');
    expect(f.nativeElement.textContent).toContain('150 KB');
    expect(f.nativeElement.querySelector('img').src).toContain('data:image/webp');
    const input = f.nativeElement.querySelector('input') as HTMLInputElement;
    expect(input.value).toBe('Descripción inicial');
    input.value = 'Taller saludable';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    await f.whenStable();
    expect(description).toHaveBeenLastCalledWith('Taller saludable');
    const button = [...f.nativeElement.querySelectorAll('button')].find((b: HTMLButtonElement) =>
      b.textContent?.includes('Confirmar y subir'),
    );
    button.click();
    expect(confirm).toHaveBeenCalledTimes(1);
  });
  it('mantiene error, cancelación y bloqueo del botón Cancelar durante la subida', async () => {
    const f = await create();
    const cancel = vi.fn();
    f.componentInstance.cancel.subscribe(cancel);
    f.nativeElement.querySelector('.modal-confirmacion-card').click();
    expect(cancel).not.toHaveBeenCalled();
    f.nativeElement.querySelector('.modal-confirmacion-backdrop').click();
    expect(cancel).toHaveBeenCalledTimes(1);
    f.componentRef.setInput('loading', true);
    f.componentRef.setInput('error', 'No se pudo subir la imagen.');
    f.detectChanges();
    expect(f.nativeElement.querySelector('.modal-error-box').textContent).toContain(
      'No se pudo subir',
    );
    const button = [...f.nativeElement.querySelectorAll('button')].find(
      (b: HTMLButtonElement) => b.textContent?.trim() === 'Cancelar',
    );
    expect(button.disabled).toBe(true);
    button.click();
    expect(cancel).toHaveBeenCalledTimes(1);
  });
});
