import { SocialCurrentAuthor } from './social-current-author';
import { SocialModal } from './social-modal';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, Type } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
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

@Component({ imports: [TuiRoot], template: '<tui-root />' })
class SocialTestRoot {}

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
    const rect = new DOMRect(0, 0, 320, 44);
    // JSDOM no calcula layout: Taiga cierra anclas sin rectángulos. El navegador se prueba por separado.
    vi.spyOn(HTMLElement.prototype, 'getClientRects').mockReturnValue({
      0: rect,
      length: 1,
      item: () => rect,
      [Symbol.iterator]: function* () {
        yield rect;
      },
    } as unknown as DOMRectList);
    admin = false;
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    });
  });
  async function create<T>(component: Type<T>, inputs: Record<string, unknown> = {}) {
    await TestBed.configureTestingModule({
      imports: [component, SocialTestRoot],
      providers: [
        provideTaiga(),
        {
          provide: SocialCurrentAuthor,
          useValue: {
            autor$: of({ ...BACKEND.perfil.autor, fotoUrl: 'https://example.invalid/perfil.png' }),
            refresh: vi.fn(),
          },
        },
        provideRouter([]),
        { provide: SocialService, useValue: api },
        {
          provide: TeamService,
          useValue: { miEquipo: () => of({ id: 301, nombre: 'Bienestar', activo: true }) },
        },
        { provide: AuthService, useValue: { tieneRol: () => admin } },
        { provide: ConfirmService, useValue: { confirmar: () => of(true) } },
      ],
    })
      .overrideComponent(SocialModal, { set: { template: '<ng-content />', imports: [] } })
      .compileComponents();
    const root = TestBed.createComponent(SocialTestRoot);
    root.detectChanges();
    await root.whenStable();
    const f = TestBed.createComponent(component);
    for (const [key, value] of Object.entries(inputs)) f.componentRef.setInput(key, value);
    await settle(f);
    return f;
  }
  async function settle<T>(f: ComponentFixture<T>) {
    f.detectChanges();
    await f.whenStable();
    await new Promise((resolve) => setTimeout(resolve, 20));
    f.detectChanges();
    await f.whenStable();
  }
  function click<T>(f: ComponentFixture<T>, text: string) {
    const button = [
      ...Array.from((f.nativeElement as HTMLElement).querySelectorAll('button')),
      ...Array.from(document.querySelectorAll<HTMLButtonElement>('tui-dropdown button')),
    ].find((x) => x.textContent?.replace(/\s+/g, ' ').trim() === text);
    expect(button, `Botón ${text}`).toBeTruthy();
    button!.focus();
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
      cuerpo: null,
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
  // Los portales y sus clics se verifican en Chrome real; aquí se comprueba la transición de estado del componente.
  it('mantiene el guardado real hasta confirmar el backend', async () => {
    const pending = new Subject<void>();
    api.guardar.mockReturnValue(pending);
    const f = await create(SocialPostCard, { post: BACKEND.muro.pinned[0] });
    expect(f.componentInstance['p']().guardadoPorMi).toBe(true);
    f.componentInstance['save']();
    await settle(f);
    expect(api.guardar).toHaveBeenCalledWith(BACKEND.muro.pinned[0].idPost, false);
    expect(f.componentInstance['p']().guardadoPorMi).toBe(true);
    pending.next();
    pending.complete();
    await settle(f);
    expect(f.componentInstance['p']().guardadoPorMi).toBe(false);
  });
  it('un error de guardado conserva el estado confirmado y muestra el error', async () => {
    api.guardar.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    const f = await create(SocialPostCard, { post: BACKEND.muro.pinned[0] });
    f.componentInstance['save']();
    await settle(f);
    expect(f.componentInstance['p']().guardadoPorMi).toBe(true);
    expect(f.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });
  it('usa la foto real del autor y vuelve a intentar cargarla cuando cambia', async () => {
    const c = {
      ...BACKEND.comentarios.content[0],
      autor: {
        ...BACKEND.comentarios.content[0].autor,
        fotoUrl: 'https://example.invalid/primera.png',
      },
      respuestas: [],
    };
    const f = await create(SocialComment, { comentario: c });
    const image = f.nativeElement.querySelector('header app-avatar img') as HTMLImageElement;
    expect(image.src).toBe(c.autor.fotoUrl);
    image.dispatchEvent(new Event('error'));
    await settle(f);
    expect(f.nativeElement.querySelector('header app-avatar img')).toBeNull();
    f.componentRef.setInput('comentario', {
      ...c,
      autor: { ...c.autor, fotoUrl: 'https://example.invalid/nueva.png' },
    });
    await settle(f);
    expect(f.nativeElement.querySelector('header app-avatar img').src).toBe(
      'https://example.invalid/nueva.png',
    );
  });
  it('integra archivo y envío en el editor y muestra el avatar del usuario a la derecha', async () => {
    const f = await create(SocialCommentEditor, { post: BACKEND.detalle.idPost });
    const toolbar = f.nativeElement.querySelector('.editor-toolbar');
    expect(toolbar.textContent).toContain('Mencionar');
    expect(toolbar.textContent).toContain('Hashtag');
    expect(toolbar.textContent).toContain('Añadir archivo');
    expect(f.nativeElement.querySelector('.text-box button[type="submit"]').textContent).toContain(
      'Comentar',
    );
    expect(f.nativeElement.querySelector('.composer-avatar img').src).toBe(
      'https://example.invalid/perfil.png',
    );
    const file = f.nativeElement.querySelector('input[type="file"]') as HTMLInputElement;
    expect(file.hidden).toBe(true);
    const clicked = vi.spyOn(file, 'click').mockImplementation(() => {});
    (
      Array.from(toolbar.querySelectorAll('button')).find((el: unknown) =>
        (el as HTMLElement).textContent?.includes('Añadir archivo'),
      ) as HTMLButtonElement
    ).click();
    expect(clicked).toHaveBeenCalledOnce();
  });
  it('presenta el logo pequeño junto al título y el tipo debajo, con fotos separadas', async () => {
    const post = {
      ...BACKEND.muro.pinned[0],
      tipoContenido: 'ACTIVIDAD',
      titulo: 'Primera actividad',
      imagenUrl: 'https://example.invalid/logo.png',
      imagenes: ['https://example.invalid/foto.jpg'],
    };
    const f = await create(SocialPostCard, { post });
    const heading = f.nativeElement.querySelector('.publication-heading');
    expect(heading.querySelector('.activity-logo').src).toContain('logo.png');
    expect(heading.querySelector('h2').textContent).toContain('Primera actividad');
    expect(heading.querySelector('.post-labels').textContent).toContain('Actividad');
    expect(f.nativeElement.querySelector('.post-gallery img').src).toContain('foto.jpg');
    expect(f.nativeElement.querySelector('.post-gallery img').src).not.toContain('logo.png');
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
  it('responde a una respuesta mencionando al autor y enviando el ID raíz, sin nietos', async () => {
    const c = { ...BACKEND.comentarios.content[0], idComentarioPadre: 999, respuestas: [] };
    const f = await create(SocialComment, { comentario: c });
    const responder = Array.from(f.nativeElement.querySelectorAll('button')).find(
      (x: unknown) => (x as HTMLElement).textContent?.trim() === 'Responder',
    ) as HTMLButtonElement;
    responder.click();
    await settle(f);
    expect(f.nativeElement.querySelector('textarea').value).toBe('@' + c.autor.username + ' ');
    write(f, 'textarea', '@' + c.autor.username + ' Gracias por responder');
    f.nativeElement
      .querySelector('social-comment-editor form')
      .dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(api.comentar).toHaveBeenCalledWith(c.idPost, {
      contenido: '@' + c.autor.username + ' Gracias por responder',
      idComentarioPadre: 999,
      adjuntosUrls: [],
    });
    expect(f.nativeElement.querySelector('social-comment-editor')).toBeNull();
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
    f.componentInstance['react']('LOVE');
    await settle(f);
    expect(api.reaccionar).toHaveBeenCalledWith(BACKEND.detalle.idPost, 'LOVE', false);
    expect(emitted?.reaccionDelUsuario).toBe('LOVE');
    expect(emitted?.total).toBe(1);
  });
  it('reacciones usa tabs con conteo, avatar, nombre enlazado y fecha debajo', async () => {
    api.quienReacciono.mockReturnValue(
      of({
        ...BACKEND.comentarios,
        content: [
          {
            idUsuario: 102,
            autor: {
              ...BACKEND.perfil.autor,
              idUsuario: 102,
              fotoUrl: 'https://example.invalid/foto.png',
            },
            tipo: 'LOVE',
            fechaReaccion: '2026-10-29T10:30:00',
          },
        ],
      }) as never,
    );
    const f = await create(SocialReactions, {
      id: 1,
      value: { total: 3, porTipo: { LIKE: 2, LOVE: 1 }, reaccionDelUsuario: null },
    });
    expect(f.nativeElement.querySelector('.like-button').textContent.trim()).toBe('');
    f.nativeElement.querySelector('.reaction-count').click();
    await settle(f);
    const tabs = f.nativeElement.querySelector('.reaction-tabs');
    expect(tabs.querySelectorAll('button').length).toBe(3);
    expect(tabs.textContent).toContain('Todos (3)');
    expect(f.nativeElement.querySelector('select')).toBeNull();
    const row = f.nativeElement.querySelector('.reaction-person');
    expect(row.querySelector('app-avatar img').src).toContain('foto.png');
    expect(row.querySelector('a').getAttribute('href')).toBe('/social/usuarios/102');
    expect(row.querySelector('time span').textContent).toBe('10:30');
    tabs.querySelector('[title="Me encanta"]').click();
    await settle(f);
    expect(api.quienReacciono).toHaveBeenCalledWith(1, false, 'LOVE', 0);
  });
  it('sin reacciones muestra únicamente Todos (0)', async () => {
    const f = await create(SocialReactions, {
      id: 1,
      value: { total: 0, porTipo: {}, reaccionDelUsuario: null },
    });
    f.nativeElement.querySelector('.reaction-count').click();
    await settle(f);
    expect(f.nativeElement.querySelectorAll('.reaction-tabs button').length).toBe(1);
    expect(f.nativeElement.querySelector('.reaction-tabs').textContent).toContain('Todos (0)');
  });
  it('pulsar la reacción seleccionada la retira', async () => {
    const f = await create(SocialReactions, {
      id: 1,
      value: { total: 1, porTipo: { LIKE: 1 }, reaccionDelUsuario: 'LIKE' },
    });
    f.nativeElement.querySelector('.like-button').click();
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
    f.nativeElement.querySelector('.reaction-count').click();
    await settle(f);
    f.componentInstance['selectType']('LOVE');
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
    expect(f.nativeElement.querySelector('social-modal select')).toBeNull();
    expect(f.nativeElement.querySelector('.mention-result app-avatar')).toBeTruthy();
    expect(f.nativeElement.querySelector('.mention-result').textContent).not.toContain(
      '@ana.lazaro',
    );
    f.nativeElement.querySelector('[aria-label="Ver en tarjetas"]').click();
    await settle(f);
    expect(f.nativeElement.querySelector('.mention-results.cards')).toBeTruthy();
    f.nativeElement.querySelector('.mention-result').click();
    expect(emitted).toContain('@ana.lazaro');
    await settle(f);
    expect(f.nativeElement.querySelector('textarea').value).toContain('@Ana');
    expect(f.nativeElement.querySelector('textarea').value).not.toContain('@ana.lazaro');
  });
  it('comenta con el nombre de actividad visible y envía la referencia contractual', async () => {
    api.sugerir.mockReturnValue(
      of([
        {
          id: 5,
          nombre: 'Primera actividad',
          token: '@actividad{5}',
          tipo: 'ACTIVIDAD',
          imagenUrl: 'https://example.invalid/logo.png',
        },
      ]) as never,
    );
    const f = await create(SocialCommentEditor, { post: BACKEND.detalle.idPost });
    click(f, 'Mencionar');
    await settle(f);
    click(f, 'Actividades');
    write(f, '.mention-search input', 'primera');
    await new Promise((r) => setTimeout(r, 300));
    await settle(f);
    expect(api.sugerir).toHaveBeenLastCalledWith('ACTIVIDAD', 'primera');
    expect(f.nativeElement.querySelector('.mention-result img').src).toContain('logo.png');
    f.nativeElement.querySelector('.mention-result').click();
    await settle(f);
    const area = f.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    expect(area.value).toContain('@Primera actividad');
    expect(area.value).not.toContain('{5}');
    area.value += 'Vamos juntos';
    area.dispatchEvent(new Event('input'));
    await settle(f);
    f.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(f);
    expect(api.comentar).toHaveBeenCalledWith(
      BACKEND.detalle.idPost,
      expect.objectContaining({
        contenido: '@actividad{5} Vamos juntos',
      }),
    );
  });
});
