import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Type } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideTaiga } from '@taiga-ui/core';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth-service';
import { ConfirmService } from '../../core/services/confirm-service';
import { TeamService } from '../admin/teams/team-service';
import { SocialService } from './social-service';
import { SocialText } from './social-text';
import { SocialTextEditor } from './social-text-editor';
import { SocialPostEditor } from './social-post-editor';
import { SocialPostCard } from './social-post-card';
import { SocialComment } from './social-comment';
import { SocialCommentEditor } from './social-comment-editor';
import { SocialReactions } from './social-reactions';
import { SocialReport } from './social-report';
import { BACKEND } from './testing/backend-fixtures';
import { Reacciones } from './social.model';

describe('Social · componentes con JSON exportado del backend', () => {
  let api: ReturnType<typeof mockApi>;
  let admin: boolean;
  function mockApi() {
    return {
      detalle: vi.fn(() => of(structuredClone(BACKEND.detalleAutor))),
      crear: vi.fn(() => of(BACKEND.detalleAutor)),
      editar: vi.fn(() => of(BACKEND.detalleAutor)),
      eliminar: vi.fn(() => of(undefined as void)),
      guardar: vi.fn(() => of(undefined as void)),
      compartir: vi.fn(() =>
        of({ idPostShare: BACKEND.share.idPost, fechaShare: '2026-10-23T10:00:00' }),
      ),
      fijar: vi.fn(() => of(undefined as void)),
      desfijar: vi.fn(() => of(undefined as void)),
      moderar: vi.fn(() => of(undefined as void)),
      comentar: vi.fn(() => of(BACKEND.comentarios.content[0])),
      editarComentario: vi.fn(() => of({ ...BACKEND.comentarios.content[0], editado: true })),
      eliminarComentario: vi.fn(() => of(undefined as void)),
      subirAdjunto: vi.fn(() =>
        of({
          url: 'https://example.invalid/bucket/social/comentarios/38/imagen.png',
          objectKey: 'social/comentarios/38/imagen.png',
          mime: 'image/png',
          sizeBytes: 3,
        }),
      ),
      reaccionar: vi.fn(() => of(undefined as void)),
      reacciones: vi.fn(() =>
        of({ total: 1, porTipo: { LOVE: 1 }, reaccionDelUsuario: 'LOVE' } as Reacciones),
      ),
      quienReacciono: vi.fn(() => of({ ...BACKEND.comentarios, content: [] })),
      sugerir: vi.fn(() =>
        of([{ id: 101, nombre: 'Ana', token: '@ana.lazaro', tipo: 'USUARIO' as const }]),
      ),
      reportar: vi.fn(() => of(BACKEND.reportes.content[0])),
    };
  }
  beforeEach(() => {
    api = mockApi();
    admin = false;
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    });
  });
  async function create<T>(component: Type<T>, inputs: Record<string, unknown> = {}) {
    await TestBed.configureTestingModule({
      imports: [component],
      providers: [
        provideTaiga(),
        provideRouter([]),
        { provide: SocialService, useValue: api },
        {
          provide: TeamService,
          useValue: { miEquipo: () => of({ id: 301, nombre: 'Bienestar', activo: true }) },
        },
        { provide: AuthService, useValue: { tieneRol: () => admin } },
        { provide: ConfirmService, useValue: { confirmar: () => of(true) } },
      ],
    }).compileComponents();
    const f = TestBed.createComponent(component);
    for (const [key, value] of Object.entries(inputs)) f.componentRef.setInput(key, value);
    await settle(f);
    return f;
  }
  async function settle<T>(f: ComponentFixture<T>) {
    f.detectChanges();
    await f.whenStable();
    f.detectChanges();
  }
  function click<T>(f: ComponentFixture<T>, text: string) {
    const button = Array.from((f.nativeElement as HTMLElement).querySelectorAll('button')).find(
      (x) => x.textContent?.trim() === text,
    );
    expect(button, `Botón ${text}`).toBeTruthy();
    button!.click();
  }
  function write<T>(f: ComponentFixture<T>, selector: string, value: string) {
    const el = (f.nativeElement as HTMLElement).querySelector(selector) as HTMLInputElement;
    expect(el).toBeTruthy();
    el.value = value;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }

  it('renderiza HTML como texto y #Salud como enlace seguro', async () => {
    const f = await create(SocialText, { text: '<script>alert(1)</script> #Salud' });
    expect(f.nativeElement.querySelector('script')).toBeNull();
    expect(f.nativeElement.textContent).toContain('<script>');
    expect(f.nativeElement.querySelector('a').getAttribute('href')).toBe('/social/hashtags/salud');
  });
  it('no usa la URL API recibida en las menciones para navegar', async () => {
    const f = await create(SocialText, {
      mentions: [
        {
          tipoMencion: 'EQUIPO',
          refId: 301,
          displayName: 'Bienestar',
          avatarUrl: null,
          url: 'javascript:alert(1)',
        },
      ],
    });
    expect(f.nativeElement.querySelector('a').getAttribute('href')).toBe('/social/equipos/301');
  });
  it('no envía un post vacío', async () => {
    const f = await create(SocialPostEditor);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(api.crear).not.toHaveBeenCalled();
    expect(f.nativeElement.textContent).toContain('Incluye título y resumen');
  });
  it('solo imagen es válido y no envía autor/oficialidad', async () => {
    const f = await create(SocialPostEditor);
    write(f, 'textarea[formControlName="imagenes"]', 'https://example.invalid/pausa.png');
    await settle(f);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(api.crear).toHaveBeenCalledWith({
      titulo: null,
      resumen: null,
      imagenesUrls: ['https://example.invalid/pausa.png'],
      idEquipoAutor: null,
    });
  });
  it('conserva el borrador después de 409 y evita doble envío', async () => {
    const pending = new Subject<never>();
    api.crear.mockReturnValue(pending);
    const f = await create(SocialPostEditor);
    write(f, 'input[formControlName="titulo"]', 'Pausa');
    write(f, 'social-text-editor textarea', 'Para cuidarnos');
    await settle(f);
    const form = f.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit', { cancelable: true }));
    form.dispatchEvent(new Event('submit', { cancelable: true }));
    expect(api.crear).toHaveBeenCalledTimes(1);
    pending.error(
      new HttpErrorResponse({ status: 409, error: { message: 'Conflicto de prueba' } }),
    );
    await settle(f);
    expect(f.nativeElement.querySelector('input[formControlName="titulo"]').value).toBe('Pausa');
    expect(f.nativeElement.textContent).toContain('Conflicto de prueba');
  });
  it('rechaza URL inválida sin subir imágenes como adjuntos', async () => {
    const f = await create(SocialPostEditor);
    write(f, 'textarea[formControlName="imagenes"]', 'javascript:alert(1)');
    await settle(f);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    expect(api.crear).not.toHaveBeenCalled();
    expect(api.subirAdjunto).not.toHaveBeenCalled();
  });
  it('obedece los permisos de la tarjeta para actividades y noticias', async () => {
    const post = {
      ...BACKEND.muro.pinned[0],
      tipoContenido: 'ACTIVIDAD',
      puedoEditar: false,
      puedoEliminar: false,
    };
    const f = await create(SocialPostCard, { post });
    click(f, 'Opciones');
    await settle(f);
    expect(f.nativeElement.textContent).not.toContain('Eliminar');
    expect(f.nativeElement.querySelector('social-post-editor')).toBeNull();
    expect(api.detalle).not.toHaveBeenCalled();
  });
  it('renderiza el root de un share sin registrar vistas ni consultar detalles', async () => {
    const f = await create(SocialPostCard, { post: BACKEND.share });
    expect(f.nativeElement.textContent).toContain(BACKEND.share.postOriginal!.titulo);
    expect(api.detalle).not.toHaveBeenCalled();
    expect(f.nativeElement.textContent).not.toContain('Alcance');
  });
  it('muestra el guardado real y solo cambia tras confirmar el backend', async () => {
    const pending = new Subject<void>();
    api.guardar.mockReturnValue(pending);
    const f = await create(SocialPostCard, { post: BACKEND.muro.pinned[0] });
    click(f, 'Guardado ✓');
    await settle(f);
    expect(api.guardar).toHaveBeenCalledWith(BACKEND.muro.pinned[0].idPost, false);
    expect(f.nativeElement.textContent).toContain('Guardado ✓');
    pending.next();
    pending.complete();
    await settle(f);
    expect(
      Array.from(f.nativeElement.querySelectorAll('button')).some(
        (x: unknown) => (x as HTMLElement).textContent?.trim() === 'Guardar',
      ),
    ).toBe(true);
  });
  it('un error de guardado no deja la UI en un estado ficticio', async () => {
    api.guardar.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    const f = await create(SocialPostCard, { post: BACKEND.muro.pinned[0] });
    click(f, 'Guardado ✓');
    await settle(f);
    expect(f.nativeElement.textContent).toContain('Guardado ✓');
    expect(f.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });
  it('no ofrece respuestas ni acciones en tombstone', async () => {
    const c = {
      ...BACKEND.comentarios.content[0],
      contenido: 'Comentario eliminado',
      eliminado: true,
      puedoEditar: false,
      puedoEliminar: false,
      adjuntosUrls: [],
      respuestas: [],
    };
    const f = await create(SocialComment, { comentario: c });
    expect(f.nativeElement.textContent).toContain('Comentario eliminado');
    expect(f.nativeElement.querySelector('social-reactions')).toBeNull();
    expect(f.nativeElement.querySelector('button')).toBeNull();
  });
  it('no ofrece nietos al renderizar una respuesta', async () => {
    const c = { ...BACKEND.comentarios.content[0], idComentarioPadre: 999, respuestas: [] };
    const f = await create(SocialComment, { comentario: c });
    expect(
      Array.from(f.nativeElement.querySelectorAll('button')).some(
        (x: unknown) => (x as HTMLElement).textContent?.trim() === 'Responder',
      ),
    ).toBe(false);
  });
  it('marca la edición de un comentario según la respuesta del backend', async () => {
    const c = { ...BACKEND.comentarios.content[0], puedoEditar: true };
    const f = await create(SocialComment, { comentario: c });
    click(f, 'Editar');
    await settle(f);
    write(f, 'social-text-editor textarea', 'Comentario corregido');
    await settle(f);
    click(f, 'Guardar');
    await settle(f);
    expect(api.editarComentario).toHaveBeenCalledWith(
      c.idPost,
      c.idComentario,
      'Comentario corregido',
    );
    expect(f.nativeElement.textContent).toContain('Editado');
  });
  it('rechaza cinco adjuntos antes de iniciar una subida', async () => {
    const f = await create(SocialCommentEditor, { post: BACKEND.detalle.idPost });
    const el = f.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(el, 'files', {
      value: Array.from({ length: 5 }, () => new File(['abc'], 'foto.png', { type: 'image/png' })),
      configurable: true,
    });
    el.dispatchEvent(new Event('change'));
    await settle(f);
    expect(api.subirAdjunto).not.toHaveBeenCalled();
    expect(f.nativeElement.textContent).toContain('cuatro imágenes');
  });
  it('no envía un comentario durante un upload pendiente y conserva el texto si falla', async () => {
    const upload = new Subject<never>();
    api.subirAdjunto.mockReturnValue(upload);
    const f = await create(SocialCommentEditor, { post: BACKEND.detalle.idPost });
    write(f, 'textarea', 'Mi comentario');
    const el = f.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(el, 'files', {
      value: [new File(['abc'], 'foto.png', { type: 'image/png' })],
      configurable: true,
    });
    el.dispatchEvent(new Event('change'));
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    expect(api.comentar).not.toHaveBeenCalled();
    upload.error(new HttpErrorResponse({ status: 500 }));
    await settle(f);
    expect(f.nativeElement.querySelector('textarea').value).toBe('Mi comentario');
    expect(f.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });
  it('envía solo las referencias retornadas por el upload', async () => {
    const f = await create(SocialCommentEditor, {
      post: BACKEND.detalle.idPost,
      padre: BACKEND.comentarios.content[0].idComentario,
    });
    write(f, 'textarea', 'Una respuesta');
    const el = f.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(el, 'files', {
      value: [new File(['abc'], 'foto.png', { type: 'image/png' })],
      configurable: true,
    });
    el.dispatchEvent(new Event('change'));
    await settle(f);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(api.comentar).toHaveBeenCalledWith(BACKEND.detalle.idPost, {
      contenido: 'Una respuesta',
      idComentarioPadre: BACKEND.comentarios.content[0].idComentario,
      adjuntosUrls: ['https://example.invalid/bucket/social/comentarios/38/imagen.png'],
    });
  });
  it('LIKE a LOVE usa reemplazo, sin sumar una reacción local ficticia', async () => {
    const f = await create(SocialReactions, {
      id: BACKEND.detalle.idPost,
      value: { total: 1, porTipo: { LIKE: 1 }, reaccionDelUsuario: 'LIKE' },
    });
    let emitted: Reacciones | undefined;
    f.componentInstance.changed.subscribe((x) => (emitted = x));
    f.nativeElement.querySelector('[aria-label="Me encanta"]').click();
    await settle(f);
    expect(api.reaccionar).toHaveBeenCalledWith(BACKEND.detalle.idPost, 'LOVE', false);
    expect(emitted?.reaccionDelUsuario).toBe('LOVE');
    expect(emitted?.total).toBe(1);
  });
  it('pulsar la reacción seleccionada la retira', async () => {
    const f = await create(SocialReactions, {
      id: 1,
      value: { total: 1, porTipo: { LIKE: 1 }, reaccionDelUsuario: 'LIKE' },
    });
    f.nativeElement.querySelector('[aria-label="Me gusta"]').click();
    await settle(f);
    expect(api.reaccionar).toHaveBeenCalledWith(1, null, false);
  });
  it('ignora una lista antigua de personas cuando cambia el filtro', async () => {
    const old = new Subject<never>();
    api.quienReacciono.mockReturnValueOnce(old);
    const f = await create(SocialReactions, {
      id: 1,
      value: { total: 0, porTipo: {}, reaccionDelUsuario: null },
    });
    click(f, '0 reacciones · Ver personas');
    await settle(f);
    const select = f.nativeElement.querySelector('select');
    select.value = select.options[2].value;
    select.dispatchEvent(new Event('change'));
    await settle(f);
    old.error(new HttpErrorResponse({ status: 500 }));
    await settle(f);
    expect(f.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });
  it('reportar duplicado muestra 409 y conserva los detalles', async () => {
    api.reportar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({ status: 409, error: { message: 'Ya reportó este contenido' } }),
      ),
    );
    const f = await create(SocialReport, { post: 1 });
    write(f, 'textarea', 'Detalle para revisar');
    await settle(f);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(f.nativeElement.textContent).toContain('Ya reportó este contenido');
    expect(f.nativeElement.querySelector('textarea').value).toBe('Detalle para revisar');
  });
  it('selecciona una mención por un botón accesible y conserva el token', async () => {
    const f = await create(SocialTextEditor, { value: 'Hola ', max: 500 });
    let emitted = '';
    f.componentInstance.valueChange.subscribe((x) => (emitted = x));
    click(f, 'Mencionar');
    await settle(f);
    const input = f.nativeElement.querySelector('input');
    input.value = 'ana';
    input.dispatchEvent(new Event('input'));
    await new Promise((r) => setTimeout(r, 300));
    await settle(f);
    click(f, 'Ana · @ana.lazaro');
    expect(emitted).toContain('@ana.lazaro');
  });
});
