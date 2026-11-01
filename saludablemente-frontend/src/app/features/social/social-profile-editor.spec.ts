import { TestBed } from '@angular/core/testing';
import { provideTaiga } from '@taiga-ui/core';
import { of, Subject } from 'rxjs';
import { SocialProfileEditor } from './social-profile-editor';
import { SocialService } from './social-service';
import { SocialCurrentAuthor } from './social-current-author';
import { PerfilCabecera } from './social.model';
import { BACKEND } from './testing/backend-fixtures';
const perfil: PerfilCabecera = {
  autor: { ...BACKEND.perfil.autor, fotoUrl: 'https://example.invalid/foto.png' },
  portadaUrl: 'https://example.invalid/portada.png',
  presentacion: 'Original',
  propietario: true,
  equipos: [],
};
describe('Social · edición propia del perfil', () => {
  let api: {
    editarMiPresentacion: ReturnType<typeof vi.fn>;
    subirFotoPerfil: ReturnType<typeof vi.fn>;
  };
  const refresh = vi.fn();
  beforeEach(() => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    });
    api = {
      editarMiPresentacion: vi.fn(() => of(perfil)),
      subirFotoPerfil: vi.fn(() => of(perfil)),
    };
    refresh.mockClear();
  });
  async function create(onlyPresentation = false) {
    await TestBed.configureTestingModule({
      imports: [SocialProfileEditor],
      providers: [
        provideTaiga(),
        { provide: SocialService, useValue: api },
        { provide: SocialCurrentAuthor, useValue: { refresh } },
      ],
    }).compileComponents();
    const f = TestBed.createComponent(SocialProfileEditor);
    f.componentRef.setInput('perfil', perfil);
    f.componentRef.setInput('onlyPresentation', onlyPresentation);
    f.detectChanges();
    await f.whenStable();
    return f;
  }
  it('el lápiz edita presentación sin ofrecer cambios de identidad laboral', async () => {
    const f = await create(true);
    expect(f.nativeElement.querySelector('input[type=file]')).toBeNull();
    const area = f.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Presentación nueva';
    area.dispatchEvent(new Event('input'));
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await f.whenStable();
    expect(api.editarMiPresentacion).toHaveBeenCalledExactlyOnceWith('Presentación nueva');
    expect(f.nativeElement.querySelector('input[name="rol"]')).toBeNull();
  });
  it('bloquea envíos duplicados durante el guardado', async () => {
    const pending = new Subject<PerfilCabecera>();
    api.editarMiPresentacion.mockReturnValue(pending);
    const f = await create();
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    f.detectChanges();
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    expect(api.editarMiPresentacion).toHaveBeenCalledTimes(1);
    expect(f.nativeElement.querySelector('button[type=submit]').disabled).toBe(true);
    pending.next(perfil);
    pending.complete();
    await f.whenStable();
    f.detectChanges();
    expect(f.nativeElement.querySelector('button[type=submit]').disabled).toBe(false);
  });
  it('rechaza foto mayor a 2 MiB antes de subir', async () => {
    const f = await create();
    const input = f.nativeElement.querySelector('input[type=file]') as HTMLInputElement;
    Object.defineProperty(input, 'files', {
      value: [new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'foto.png', { type: 'image/png' })],
    });
    input.dispatchEvent(new Event('change'));
    f.detectChanges();
    expect(api.subirFotoPerfil).not.toHaveBeenCalled();
    expect(f.nativeElement.querySelector('[role=alert]').textContent).toContain('2 MiB');
  });
  it('subir foto refresca autor sin borrar la presentación en edición', async () => {
    const f = await create();
    const area = f.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Borrador de presentación';
    area.dispatchEvent(new Event('input'));
    const input = f.nativeElement.querySelector('input[type=file]') as HTMLInputElement;
    const file = new File([new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10])], 'foto.png', {
      type: 'image/png',
    });
    Object.defineProperty(input, 'files', { value: [file] });
    input.dispatchEvent(new Event('change'));
    await f.whenStable();
    f.detectChanges();
    expect(api.subirFotoPerfil).toHaveBeenCalledExactlyOnceWith(file, false);
    expect(refresh).toHaveBeenCalledOnce();
    expect(area.value).toBe('Borrador de presentación');
  });
});
