import { Component, Directive, input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideTaiga } from '@taiga-ui/core';
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
      comentarios: vi.fn(() => of(BACKEND.comentarios)),
      comentario: vi.fn(() => of(BACKEND.comentarios.content[0])),
      buscar: vi.fn((_q: string, tipo: string) =>
        of({ ...BACKEND.busquedaComentarios, tipo } as Busqueda),
      ),
      perfil: vi.fn(() => of(BACKEND.perfil)),
      miContenido: vi.fn(() => of(BACKEND.guardados)),
      reportes: vi.fn(() => of(BACKEND.reportes)),
      resolver: vi.fn(() => of(BACKEND.reportes.content[0])),
      moderar: vi.fn(() => of(undefined as void)),
      metricas: vi.fn(() => of(BACKEND.metricas)),
    };
  }
  beforeEach(async () => {
    api = mockApi();
    admin = false;
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    });
    await TestBed.configureTestingModule({
      providers: [
        provideTaiga(),
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
      .overrideComponent(SocialMetrics, {
        remove: { imports: [NgxEchartsDirective] },
        add: { imports: [ChartStub] },
      })
      .compileComponents();
  });
  async function open(url: string) {
    const h = await RouterTestingHarness.create(url);
    await settle(h);
    return h;
  }
  async function settle(h: RouterTestingHarness) {
    await new Promise((r) => setTimeout(r, 15));
    h.detectChanges();
    await h.fixture.whenStable();
    h.detectChanges();
  }
  function text(h: RouterTestingHarness) {
    return h.fixture.nativeElement.textContent as string;
  }
  function button(h: RouterTestingHarness, label: string) {
    return Array.from((h.fixture.nativeElement as HTMLElement).querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === label,
    )!;
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
    expect(text(h)).toContain('Guardado ✓');
    expect(text(h)).not.toContain('Alcance:');
    expect(text(h)).toContain('Conversemos');
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
    expect(text(h)).toContain('Comentario seleccionado');
    expect(h.fixture.nativeElement.querySelector(`#comentario-${c.idComentario}`)).toBeTruthy();
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
  it('perfil público no ofrece guardados ni consulta contenido privado', async () => {
    const h = await open('/social/usuarios/101');
    expect(api.perfil).toHaveBeenCalledWith(101, 0);
    expect(api.miContenido).not.toHaveBeenCalled();
    expect(text(h)).not.toContain('Mis guardados');
  });
  it.each(['PUBLICACIONES', 'COMENTARIOS', 'REACCIONES', 'GUARDADOS'] as const)(
    'mi contenido consulta la tab %s',
    async (tab) => {
      const h = await open(`/social/mi-contenido?tab=${tab}`);
      expect(api.miContenido).toHaveBeenCalledWith(tab, 0);
      if (tab === 'GUARDADOS') expect(text(h)).toContain('Solo tú puedes ver tus guardados');
    },
  );
  it('RoleGuard rechaza administración por URL directa', async () => {
    const h = await open('/social/admin/reportes');
    await settle(h);
    expect(TestBed.inject(Router).url).toContain('/no-autorizado');
    expect(api.reportes).not.toHaveBeenCalled();
  });
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
