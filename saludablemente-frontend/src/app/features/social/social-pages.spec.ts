import { SocialCurrentAuthor } from './social-current-author';
import { SocialModal } from './social-modal';
import { By } from '@angular/platform-browser';
import { SocialPostCard } from './social-post-card';
import { Component, Directive, input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth-service';
import { ConfirmService } from '../../core/services/confirm-service';
import { TeamService } from '../admin/teams/team-service';
import { SocialService } from './social-service';
import { SOCIAL_ROUTES } from './social.routes';
import { SocialMetrics } from './social-metrics';
import { BACKEND } from './testing/backend-fixtures';
import { Busqueda, Metricas } from './social.model';
import {
  rutaOrigenNotificacion,
  origenVisual,
} from '../colaborador/notificaciones/origen-notificacion';

@Component({ imports: [TuiRoot], template: '<tui-root />' })
class PageTestRoot {}
@Component({ template: 'Sin permiso' })
class Denied {}
@Directive({ selector: '[echarts]' })
class ChartStub {
  readonly options = input<unknown>();
}
describe('Social · rutas, páginas y entregables U01–U09', () => {
  let api: ReturnType<typeof mockApi>;
  let admin: boolean;
  function mockApi() {
    return {
      muro: vi.fn(() => of(BACKEND.muro)),
      equipo: vi.fn(() => of(BACKEND.muro.pinned[0].equipo!)),
      tendencias: vi.fn(() => of([{ tag: 'salud', total: 1 }])),
      sugerir: vi.fn(() => of([])),
      detalle: vi.fn(() => of(BACKEND.detalle)),
      comentar: vi.fn(() => of(BACKEND.comentarios.content[0])),
      comentarios: vi.fn(() => of(BACKEND.comentarios)),
      comentario: vi.fn(() => of(BACKEND.comentarios.content[0])),
      buscar: vi.fn((_q: string, tipo: string) =>
        of({ ...BACKEND.busquedaComentarios, tipo } as Busqueda),
      ),
      cabeceraPerfil: vi.fn(() =>
        of({
          autor: BACKEND.perfil.autor,
          portadaUrl: null,
          presentacion: 'Mi presentación',
          propietario: false,
          equipos: [
            {
              idUsuario: 101,
              idEquipo: 301,
              nombreEquipo: 'Bienestar',
              logoEquipoUrl: null,
              rolEnEquipo: 'Colaborador',
              activo: true,
            },
          ],
        }),
      ),
      miPerfilSocial: vi.fn(() =>
        of({
          autor: BACKEND.perfil.autor,
          portadaUrl: null,
          presentacion: 'Mi presentación',
          propietario: true,
          equipos: [],
        }),
      ),
      companeros: vi.fn(() =>
        of({
          ...BACKEND.comentarios,
          content: [
            {
              ...BACKEND.perfil.autor,
              idUsuario: 102,
              nombreCompleto: 'Compañero de equipo',
              fotoUrl: 'https://example.invalid/companero.png',
            },
          ],
          totalElements: 1,
          totalPages: 1,
        }),
      ),
      editarMiPresentacion: vi.fn(() =>
        of({
          autor: BACKEND.perfil.autor,
          portadaUrl: null,
          presentacion: 'Actualizada',
          propietario: true,
          equipos: [],
        }),
      ),
      subirFotoPerfil: vi.fn(() =>
        of({
          autor: BACKEND.perfil.autor,
          portadaUrl: null,
          presentacion: 'Actualizada',
          propietario: true,
          equipos: [],
        }),
      ),
      perfil: vi.fn(() => of(BACKEND.perfil)),
      miContenido: vi.fn(() =>
        of({ ...BACKEND.guardados, publicaciones: BACKEND.perfil.publicaciones }),
      ),
      reportes: vi.fn(() => of(BACKEND.reportes)),
      resolver: vi.fn(() => of(BACKEND.reportes.content[0])),
      moderar: vi.fn(() => of(undefined as void)),
      metricas: vi.fn(() => of(BACKEND.metricas)),
    };
  }
  beforeEach(async () => {
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
    await TestBed.configureTestingModule({
      imports: [PageTestRoot],
      providers: [
        provideTaiga(),
        {
          provide: SocialCurrentAuthor,
          useValue: {
            autor$: of({ ...BACKEND.perfil.autor, fotoUrl: 'https://example.invalid/perfil.png' }),
            refresh: vi.fn(),
          },
        },
        provideRouter([
          { path: 'social', children: SOCIAL_ROUTES },
          { path: 'no-autorizado', component: Denied },
        ]),
        { provide: SocialService, useValue: api },
        { provide: AuthService, useValue: { tieneRol: () => admin, tieneAlgunRol: () => admin } },
        {
          provide: TeamService,
          useValue: { miEquipo: () => of({ id: 301, nombre: 'Bienestar', activo: true }) },
        },
        { provide: ConfirmService, useValue: { confirmar: () => of(true) } },
      ],
    })
      .overrideComponent(SocialModal, { set: { template: '<ng-content />', imports: [] } })
      .overrideComponent(SocialMetrics, {
        remove: { imports: [NgxEchartsDirective] },
        add: { imports: [ChartStub] },
      })
      .compileComponents();
  });
  async function open(url: string) {
    const root = TestBed.createComponent(PageTestRoot);
    root.detectChanges();
    await root.whenStable();
    const h = await RouterTestingHarness.create(url);
    await settle(h);
    return h;
  }
  async function settle(h: RouterTestingHarness) {
    await new Promise((r) => setTimeout(r, 100));
    h.detectChanges();
    await h.fixture.whenStable();
    h.detectChanges();
  }
  function text(h: RouterTestingHarness) {
    return h.fixture.nativeElement.textContent as string;
  }
  function button(h: RouterTestingHarness, label: string) {
    const target = Array.from(
      (h.fixture.nativeElement as HTMLElement).querySelectorAll('button'),
    ).find((b) => b.textContent?.trim() === label)!;
    target?.focus();
    return target;
  }

  it('muro renderiza pins, equipo/líder y shares desde el JSON real', async () => {
    const h = await open('/social');
    expect(text(h)).toContain('Publicación fijada');
    expect(text(h)).toContain(BACKEND.muro.pinned[0].titulo!);
    expect(text(h)).toContain('Líder');
    expect(api.detalle).not.toHaveBeenCalled();
  });
  it('cambia tab reiniciando la página y conserva enlaces recargables', async () => {
    const h = await open('/social?tab=RECIENTES&page=2');
    expect(api.muro).toHaveBeenLastCalledWith(2, 'RECIENTES', 'GLOBAL', null, null);
    button(h, 'Noticias').click();
    await settle(h);
    expect(TestBed.inject(Router).url).toContain('page=0');
    expect(api.muro).toHaveBeenLastCalledWith(0, 'SOLO_NOTICIAS', 'GLOBAL', null, null);
  });
  it('abre el editor desde la entrada compacta con avatar autenticado', async () => {
    const h = await open('/social');
    const prompt = h.fixture.nativeElement.querySelector('.composer-prompt') as HTMLButtonElement;
    expect(prompt.textContent).toContain('Nueva publicación');
    expect(h.fixture.nativeElement.querySelector('.feed-composer app-avatar')).toBeTruthy();
    prompt.click();
    await settle(h);
    expect(h.fixture.nativeElement.querySelector('social-modal social-post-editor')).toBeTruthy();
    expect(api.detalle).not.toHaveBeenCalled();
  });
  it('el detalle de conversación reutiliza el post cargado y mantiene un único editor', async () => {
    const h = await open(`/social/posts/${BACKEND.detalle.idPost}?comentar=1`);
    const context = h.fixture.nativeElement.querySelector('.discussion-post') as HTMLElement;
    expect(context.textContent).toContain(BACKEND.detalle.autor.nombreCompleto);
    expect(context.textContent).toContain(BACKEND.detalle.titulo!);
    expect(
      h.fixture.nativeElement.querySelectorAll('.conversation-composer social-comment-editor')
        .length,
    ).toBe(1);
    expect(api.detalle).toHaveBeenCalledTimes(1);
  });
  it.each([
    ['/social/equipos/301', 'EQUIPO', 301, null],
    ['/social/actividades/401', 'ACTIVIDAD', 401, null],
    ['/social/hashtags/salud', 'GLOBAL', null, 'salud'],
  ] as const)('ruta %s aplica scope sin mezclar fijados', async (url, scope, id, tag) => {
    await open(url);
    expect(api.muro).toHaveBeenLastCalledWith(0, 'PARA_TI', scope, id, tag);
  });
  it('presenta vacío y permite crear sin datos de muestra productivos', async () => {
    api.muro.mockReturnValue(
      of({
        pinned: [],
        posts: { ...BACKEND.muro.posts, content: [], totalPages: 0, totalElements: 0 },
      }),
    );
    const h = await open('/social');
    expect(text(h)).toContain('No hay publicaciones para este filtro');
    expect(text(h)).not.toContain(BACKEND.muro.pinned[0].titulo!);
  });
  it('muestra errores de feed con reintento', async () => {
    api.muro.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));
    const h = await open('/social');
    expect(h.fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
    button(h, 'Reintentar').click();
    await settle(h);
    expect(text(h)).toContain('Publicación fijada');
  });
  it('en detalle muestra guardado real y no expone alcance de terceros', async () => {
    const h = await open(`/social/posts/${BACKEND.detalle.idPost}`);
    expect(api.detalle).toHaveBeenCalledWith(BACKEND.detalle.idPost);
    const card = h.fixture.debugElement.query(By.directive(SocialPostCard))
      .componentInstance as SocialPostCard;
    expect(card.post().guardadoPorMi).toBe(true);
    expect(text(h)).not.toContain('Alcance:');
    expect(h.fixture.nativeElement.querySelector('a[aria-label="Comentar"]')).toBeTruthy();
    expect(h.fixture.nativeElement.querySelector('social-comment-editor')).toBeNull();
    expect(document.querySelector('tui-dialog')).toBeNull();
  });
  it('abre Comentar en un modal desde la acción de comentarios y permite cerrarlo', async () => {
    const h = await open(`/social/posts/${BACKEND.detalle.idPost}`);
    const action = h.fixture.nativeElement.querySelector(
      'a[aria-label="Comentar"]',
    ) as HTMLAnchorElement;
    expect(action.getAttribute('href')).toContain('comentar=1');
    action.click();
    await settle(h);
    expect(
      h.fixture.nativeElement.querySelector('social-modal social-comment-editor'),
    ).toBeTruthy();
    expect(h.fixture.nativeElement.querySelector('social-modal')?.textContent).toContain(
      'Comentar',
    );
    expect(h.fixture.nativeElement.querySelector('social-modal input[type="file"]')).toBeTruthy();
    const modal = h.fixture.debugElement.query(By.directive(SocialModal))
      .componentInstance as SocialModal;
    expect(modal.title()).toBe('Comentar');
    modal.closed.emit();
    await settle(h);
    expect(h.fixture.nativeElement.querySelector('social-modal')).toBeNull();
    expect(TestBed.inject(Router).url).not.toContain('comentar=1');
  });
  it('mantiene el modal abierto al intentar cerrar durante el envío y permite cerrar al terminar', async () => {
    const request = new Subject<(typeof BACKEND.comentarios.content)[0]>();
    api.comentar.mockReturnValue(request);
    const h = await open(`/social/posts/${BACKEND.detalle.idPost}?comentar=1`);
    const textarea = h.fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    textarea.value = 'Comentario de prueba';
    textarea.dispatchEvent(new Event('input', { bubbles: true }));
    h.fixture.nativeElement
      .querySelector('social-comment-editor form')
      .dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(h);
    const modal = h.fixture.debugElement.query(By.directive(SocialModal))
      .componentInstance as SocialModal;
    expect(modal.busy()).toBe(true);
    modal.closed.emit();
    await settle(h);
    expect(h.fixture.nativeElement.querySelector('social-modal')).toBeTruthy();
    request.next(BACKEND.comentarios.content[0]);
    request.complete();
    await settle(h);
    expect(modal.busy()).toBe(false);
    modal.closed.emit();
    await settle(h);
    expect(h.fixture.nativeElement.querySelector('social-modal')).toBeNull();
  });
  it('Responder utiliza el editor inferior sin añadir otro formulario a la lista', async () => {
    const h = await open(`/social/posts/${BACKEND.detalle.idPost}?comentar=1`);
    const list = h.fixture.nativeElement.querySelector('.comments-scroll');
    const responder = Array.from(list.querySelectorAll('button')).find(
      (x: unknown) => (x as HTMLElement).textContent?.trim() === 'Responder',
    ) as HTMLButtonElement;
    responder.click();
    await settle(h);
    const footer = h.fixture.nativeElement.querySelector('.conversation-composer');
    expect(footer.textContent).toContain('Respondiendo a');
    expect(list.querySelector('social-comment-editor')).toBeNull();
    const area = footer.querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Respuesta desde el editor inferior';
    area.dispatchEvent(new Event('input', { bubbles: true }));
    footer.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(h);
    expect(api.comentar).toHaveBeenCalledWith(BACKEND.detalle.idPost, {
      contenido: 'Respuesta desde el editor inferior',
      idComentarioPadre: BACKEND.comentarios.content[0].idComentario,
      adjuntosUrls: [],
    });
    expect(footer.textContent).not.toContain('Respondiendo a');
  });
  it('el autor sí ve su alcance', async () => {
    api.detalle.mockReturnValue(of(BACKEND.detalleAutor));
    const h = await open(`/social/posts/${BACKEND.detalleAutor.idPost}`);
    expect(text(h)).toContain('Alcance:');
  });
  it('un aviso a contenido oculto muestra 404 comprensible', async () => {
    api.detalle.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    const h = await open('/social/posts/9999');
    expect(text(h)).toContain('El contenido no está disponible');
    expect(h.fixture.nativeElement.querySelector('social-comment-editor')).toBeNull();
  });
  it('navega al comentario aunque no esté en la primera página de raíces', async () => {
    api.comentarios.mockReturnValue(of({ ...BACKEND.comentarios, content: [] }));
    const c = BACKEND.comentarios.content[0];
    const h = await open(`/social/posts/${c.idPost}#comentario-${c.idComentario}`);
    expect(api.comentario).toHaveBeenCalledWith(c.idPost, c.idComentario);
    expect(document.body.textContent).toContain('Comentario seleccionado');
    expect(document.querySelector(`#comentario-${c.idComentario}`)).toBeTruthy();
  });
  it('comentarios buscados enlazan a su post y fragmento reales', async () => {
    const h = await open('/social/buscar?q=salud&tipo=COMENTARIO');
    const c = BACKEND.busquedaComentarios.comentarios.content[0];
    expect(api.buscar).toHaveBeenCalledWith('salud', 'COMENTARIO', 0);
    const link = Array.from((h.fixture.nativeElement as HTMLElement).querySelectorAll('a')).find(
      (a) => a.textContent?.includes('Ver en la publicación'),
    );
    expect(link?.getAttribute('href')).toBe(
      `/social/posts/${c.idPost}#comentario-${c.idComentario}`,
    );
  });
  it('una búsqueda antigua no reemplaza una nueva', async () => {
    const old = new Subject<Busqueda>();
    api.buscar.mockReturnValueOnce(old);
    const h = await open('/social/buscar?q=antigua&tipo=COMENTARIO');
    await h.navigateByUrl('/social/buscar?q=nueva&tipo=COMENTARIO');
    await settle(h);
    old.next({
      ...BACKEND.busquedaComentarios,
      comentarios: { ...BACKEND.busquedaComentarios.comentarios, content: [] },
    });
    await settle(h);
    expect(text(h)).toContain('Ver en la publicación');
    expect(TestBed.inject(Router).url).toContain('q=nueva');
  });
  it('perfil público tiene portada, datos y compañeros sin edición ajena', async () => {
    const h = await open('/social/usuarios/101');
    expect(h.fixture.nativeElement.querySelector('.profile-cover')).toBeTruthy();
    expect(text(h)).toContain('Datos personales');
    expect(text(h)).toContain('Compañeros de equipo');
    expect(h.fixture.nativeElement.querySelector('.friend-tile app-avatar')).toBeTruthy();
    expect(h.fixture.nativeElement.querySelector('.friend-tile a').getAttribute('href')).toBe(
      '/social/usuarios/102',
    );
    expect(h.fixture.nativeElement.querySelector('.cover-edit')).toBeNull();
    expect(text(h)).not.toContain('Editar perfil');
    expect(h.fixture.nativeElement.querySelector('[aria-label="Editar presentación"]')).toBeNull();
  });
  it('el propietario ve edición y cambia a cuadrícula sin recargar las publicaciones', async () => {
    const h = await open('/social/mi-contenido');
    expect(text(h)).toContain('Editar perfil');
    expect(
      h.fixture.nativeElement.querySelector('[aria-label="Editar presentación"]'),
    ).toBeTruthy();
    const before = api.miContenido.mock.calls.length;
    h.fixture.nativeElement.querySelector('[aria-label="Vista de cuadrícula"]').click();
    await settle(h);
    expect(api.miContenido.mock.calls.length).toBe(before);
    const card = h.fixture.nativeElement.querySelector('.profile-post-preview');
    expect(card).toBeTruthy();
    expect(card.getAttribute('href')).toContain('comentar=1');
    expect(card.querySelector('.preview-meta app-avatar')).toBeTruthy();
    expect(card.querySelector('time')).toBeTruthy();
  });
  it('abre la edición de portada solo para el propietario', async () => {
    const h = await open('/social/mi-contenido');
    h.fixture.nativeElement.querySelector('.cover-edit').click();
    await settle(h);
    const editor = h.fixture.nativeElement.querySelector('social-profile-editor');
    expect(editor.textContent).toContain('Cambiar portada');
    expect(editor.textContent).not.toContain('Cambiar foto');
    expect(editor.querySelector('textarea')).toBeNull();
    expect(api.editarMiPresentacion).not.toHaveBeenCalled();
  });
  it.each(['/social/mi-contenido', '/social/usuarios/101'])(
    'cambia solo la sección de publicaciones sin navegar ni consultar otra vez: %s',
    async (url) => {
      const h = await open(url);
      const host = h.fixture.nativeElement as HTMLElement;
      const header = host.querySelector('.profile-header');
      const info = host.querySelector('.profile-info');
      const friends = host.querySelector('.friend-mosaic');
      const router = TestBed.inject(Router);
      const navigate = vi.spyOn(router, 'navigate');
      const before = [
        api.miContenido,
        api.perfil,
        api.miPerfilSocial,
        api.cabeceraPerfil,
        api.companeros,
      ].map((fn) => fn.mock.calls.length);
      for (const label of ['Vista de cuadrícula', 'Vista de lista', 'Vista de cuadrícula']) {
        host.querySelector<HTMLButtonElement>('[aria-label="' + label + '"]')!.click();
        await settle(h);
        expect(host.querySelector('.profile-header')).toBe(header);
        expect(host.querySelector('.profile-info')).toBe(info);
        expect(host.querySelector('.friend-mosaic')).toBe(friends);
        expect(host.querySelector('.profile-content .loading')).toBeNull();
      }
      expect(navigate).not.toHaveBeenCalled();
      expect(router.url).toBe(url);
      expect(
        [api.miContenido, api.perfil, api.miPerfilSocial, api.cabeceraPerfil, api.companeros].map(
          (fn) => fn.mock.calls.length,
        ),
      ).toEqual(before);
      const card = host.querySelector('.profile-post-preview')!;
      expect(card.querySelector('.preview-author strong')?.textContent).toBeTruthy();
      expect(card.querySelectorAll('.preview-stats span')).toHaveLength(3);
      expect(card.querySelector('.preview-summary')).toBeTruthy();
    },
  );
  it('la cuadrícula conserva un respaldo visible cuando falla una imagen', async () => {
    api.perfil.mockReturnValue(
      of({
        ...BACKEND.perfil,
        publicaciones: {
          ...BACKEND.perfil.publicaciones,
          content: [
            {
              ...BACKEND.muro.posts.content[0],
              tipoContenido: 'MANUAL',
              postOriginal: null,
              imagenes: ['https://example.invalid/foto.png'],
            },
          ],
        },
      }) as never,
    );
    const h = await open('/social/usuarios/101?vista=cuadricula');
    const img = h.fixture.nativeElement.querySelector('.preview-media img') as HTMLImageElement;
    expect(img).toBeTruthy();
    img.dispatchEvent(new Event('error'));
    await settle(h);
    expect(h.fixture.nativeElement.querySelector('.preview-placeholder')?.textContent).toContain(
      'Imagen no disponible',
    );
    expect(
      h.fixture.nativeElement.querySelector('.profile-post-preview')?.getAttribute('href'),
    ).toContain('/social/posts/');
  });
  it('los compañeros llevan a un perfil público sin acciones de edición', async () => {
    const h = await open('/social/usuarios/101');
    h.fixture.nativeElement.querySelector('.friend-tile a').click();
    await settle(h);
    expect(TestBed.inject(Router).url).toBe('/social/usuarios/102');
    expect(api.cabeceraPerfil).toHaveBeenLastCalledWith(102);
    expect(text(h)).not.toContain('Editar perfil');
    expect(text(h)).not.toContain('Editar portada');
  });
  it('un perfil sin publicaciones muestra el estado vacío', async () => {
    api.perfil.mockReturnValue(
      of({
        ...BACKEND.perfil,
        publicaciones: {
          ...BACKEND.perfil.publicaciones,
          content: [],
          totalElements: 0,
          totalPages: 0,
        },
      }),
    );
    const h = await open('/social/usuarios/101');
    expect(text(h)).toContain('Sin publicaciones.');
  });
  it('perfil público no ofrece guardados ni consulta contenido privado', async () => {
    const h = await open('/social/usuarios/101');
    expect(api.perfil).toHaveBeenCalledWith(101, 0);
    expect(api.miContenido).not.toHaveBeenCalled();
    expect(text(h)).not.toContain('Mis guardados');
  });
  it('comentarios del perfil muestran autor, respuesta, adjuntos y enlace exacto', async () => {
    const comment = { ...BACKEND.comentarios.content[0], idComentarioPadre: 12,
      editado: true, adjuntosUrls: ['https://example.invalid/adjunto.png'] };
    api.perfil.mockReturnValue(of({ ...BACKEND.perfil,
      comentarios: { ...BACKEND.comentarios, content: [comment] },
    }));
    const h = await open('/social/usuarios/101?tab=COMENTARIOS');
    const card = h.fixture.nativeElement.querySelector('.profile-comment') as HTMLElement;
    expect(card.textContent).toContain(comment.autor.nombreCompleto);
    expect(card.textContent).toContain('Respuesta');
    expect(card.textContent).toContain('Editado');
    expect(card.querySelector('time')?.getAttribute('datetime')).toBe(comment.fechaComentario);
    expect(card.querySelector('.profile-comment-open')?.getAttribute('href'))
      .toBe(`/social/posts/${comment.idPost}#comentario-${comment.idComentario}`);
    const image = card.querySelector('img[loading="lazy"]')!;
    image.dispatchEvent(new Event('error'));
    await settle(h);
    expect(card.textContent).toContain('Imagen no disponible');
    expect(api.detalle).not.toHaveBeenCalled();
  });
  it('comentarios vacíos de otro perfil presentan orientación sin acciones propias', async () => {
    api.perfil.mockReturnValue(of({ ...BACKEND.perfil,
      comentarios: { ...BACKEND.comentarios, content: [], totalElements: 0 },
    }));
    const h = await open('/social/usuarios/101?tab=COMENTARIOS');
    expect(text(h)).toContain('Todavía no hay comentarios');
    expect(text(h)).not.toContain('Tu próxima conversación');
    expect(h.fixture.nativeElement.querySelector('.profile-comment')).toBeNull();
  });
  it.each(['PUBLICACIONES', 'COMENTARIOS', 'REACCIONES', 'GUARDADOS'] as const)(
    'mi contenido consulta la tab %s',
    async (tab) => {
      const h = await open(`/social/mi-contenido?tab=${tab}`);
      expect(api.miContenido).toHaveBeenCalledWith(tab, 0);
      if (tab === 'GUARDADOS') expect(text(h)).toContain('Solo tú puedes ver tus guardados');
    },
  );
  it.each(['reportes', 'metricas'])(
    'RoleGuard rechaza %s por URL directa sin rol',
    async (section) => {
      const h = await open('/social/admin/' + section);
      await settle(h);
      expect(TestBed.inject(Router).url).toContain('/no-autorizado');
      expect(api.reportes).not.toHaveBeenCalled();
      expect(api.metricas).not.toHaveBeenCalled();
      expect(h.fixture.nativeElement.querySelector('a[href="/social/admin/reportes"]')).toBeNull();
      expect(h.fixture.nativeElement.querySelector('a[href="/social/admin/metricas"]')).toBeNull();
    },
  );
  it('administrador resuelve un reporte y conserva la auditoría', async () => {
    admin = true;
    const h = await open('/social/admin/reportes');
    expect(text(h)).toContain('Comentario reportado');
    button(h, 'Aceptar y ocultar').click();
    await settle(h);
    expect(api.resolver).toHaveBeenCalledWith(
      BACKEND.reportes.content[0].idReporte,
      'ACEPTADO',
      null,
    );
    expect(text(h)).toContain('Decisión registrada');
  });
  it('métricas coinciden con los datos del contrato y ofrecen tabla accesible', async () => {
    admin = true;
    const h = await open('/social/admin/metricas');
    expect(text(h)).toContain('La comunidad en cifras');
    expect(text(h)).toContain('Ver datos del gráfico');
    const rows = h.fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(BACKEND.metricas.volumenPorDia.length);
  });
  it('rechaza un rango invertido sin volver a pedir métricas', async () => {
    admin = true;
    const h = await open('/social/admin/metricas');
    const initial = api.metricas.mock.calls.length;
    const dates = h.fixture.nativeElement.querySelectorAll(
      'input[type="date"]',
    ) as NodeListOf<HTMLInputElement>;
    dates[0].value = '2026-10-25';
    dates[0].dispatchEvent(new Event('input'));
    dates[1].value = '2026-10-01';
    dates[1].dispatchEvent(new Event('input'));
    await settle(h);
    h.fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { cancelable: true }));
    await settle(h);
    expect(api.metricas).toHaveBeenCalledTimes(initial);
    expect(text(h)).toContain('Selecciona fechas válidas');
  });
  it('notificación social lleva al mismo detalle para cualquier rol', () => {
    for (const role of [true, false])
      expect(
        rutaOrigenNotificacion(
          { origenModulo: 'social', idOrigenReferencia: BACKEND.detalle.idPost },
          role,
        ),
      ).toBe(`/social/posts/${BACKEND.detalle.idPost}`);
    expect(rutaOrigenNotificacion({ origenModulo: 'social' }, false)).toBe('/social');
    expect(origenVisual('social').etiqueta).toBe('Social');
  });
});
