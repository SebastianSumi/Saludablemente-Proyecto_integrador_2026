import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api-service';
import { SocialService } from './social-service';
import { CrearPost } from './social.model';
describe('SocialService · contratos HTTP', () => {
  let service: SocialService;
  let http: HttpTestingController;
  const base = '/backend/api/v1/social';
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiService, useValue: { buildUrl: (path: string) => `/backend${path}` } },
      ],
    });
    service = TestBed.inject(SocialService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it.each([
    ['GLOBAL', null, null, '/muro'],
    ['EQUIPO', 301, null, '/muro/equipos/301'],
    ['ACTIVIDAD', 401, null, '/muro/actividades/401'],
    ['GLOBAL', null, 'salud', '/hashtags/salud/muro'],
  ] as const)('muro %s usa ruta y paginación reales', (scope, id, tag, path) => {
    service.muro(2, 'RECIENTES', scope, id, tag).subscribe();
    const req = http.expectOne((r) => r.url === base + path);
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('tab')).toBe('RECIENTES');
    req.flush({ pinned: [], posts: { content: [] } });
  });
  it('codifica un hashtag como segmento, sin añadir rutas', () => {
    service.muro(0, 'PARA_TI', 'GLOBAL', null, 'salud/bienestar').subscribe();
    http.expectOne((r) => r.url === `${base}/hashtags/salud%2Fbienestar/muro`).flush({});
  });
  it('crea J03 sin aceptar un autor ni oficialidad del cliente', () => {
    const body: CrearPost = {
      titulo: 'Pausa saludable',
      resumen: 'Una pausa para cuidarnos',
      idEquipoAutor: null,
      imagenesUrls: [],
    };
    service.crear(body).subscribe();
    const r = http.expectOne(base + '/posts');
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(body);
    expect(r.request.body.idUsuarioAutor).toBeUndefined();
    expect(r.request.body.esOficial).toBeUndefined();
    r.flush({});
  });
  const writes: [string, (s: SocialService) => Observable<unknown>, string, string, unknown][] = [
    [
      'editar',
      (s) =>
        s.editar(1, {
          titulo: null,
          resumen: null,
          imagenesUrls: ['https://example.invalid/foto.png'],
        }),
      'PATCH',
      '/posts/1',
      { titulo: null, resumen: null, imagenesUrls: ['https://example.invalid/foto.png'] },
    ],
    ['eliminar', (s) => s.eliminar(1), 'DELETE', '/posts/1', null],
    [
      'compartir',
      (s) => s.compartir(1, 'Compartido', 301),
      'POST',
      '/posts/1/share',
      { contenido: 'Compartido', idEquipoAutor: 301 },
    ],
    ['guardar', (s) => s.guardar(1, true), 'POST', '/posts/1/guardar', {}],
    ['quitar guardado', (s) => s.guardar(1, false), 'DELETE', '/posts/1/guardar', null],
    [
      'fijar',
      (s) => s.fijar(1, { scopeTipo: 'GLOBAL', scopeId: null, fechaExpiracion: null }),
      'POST',
      '/posts/1/fijar',
      { scopeTipo: 'GLOBAL', scopeId: null, fechaExpiracion: null },
    ],
    [
      'comentar',
      (s) => s.comentar(1, { contenido: 'Respuesta', idComentarioPadre: 3, adjuntosUrls: [] }),
      'POST',
      '/posts/1/comentarios',
      { contenido: 'Respuesta', idComentarioPadre: 3, adjuntosUrls: [] },
    ],
    [
      'editar comentario',
      (s) => s.editarComentario(1, 3, 'Editado'),
      'PATCH',
      '/posts/1/comentarios/3',
      { contenido: 'Editado' },
    ],
    [
      'eliminar comentario',
      (s) => s.eliminarComentario(1, 3),
      'DELETE',
      '/posts/1/comentarios/3',
      null,
    ],
    [
      'reaccionar post',
      (s) => s.reaccionar(1, 'LOVE'),
      'POST',
      '/posts/1/reacciones',
      { tipo: 'LOVE' },
    ],
    ['quitar reacción', (s) => s.reaccionar(1, null), 'DELETE', '/posts/1/reacciones', null],
    [
      'reaccionar comentario',
      (s) => s.reaccionar(3, 'APLAUSO', true),
      'POST',
      '/comentarios/3/reacciones',
      { tipo: 'APLAUSO' },
    ],
    [
      'quitar reacción comentario',
      (s) => s.reaccionar(3, null, true),
      'DELETE',
      '/comentarios/3/reacciones',
      null,
    ],
    [
      'reportar post',
      (s) => s.reportar(1, 'SPAM', 'Detalle'),
      'POST',
      '/posts/1/reportar',
      { motivo: 'SPAM', detalle: 'Detalle' },
    ],
    [
      'reportar comentario',
      (s) => s.reportar(1, 'OTRO', null, 3),
      'POST',
      '/posts/1/comentarios/3/reportar',
      { motivo: 'OTRO', detalle: null },
    ],
    [
      'resolver',
      (s) => s.resolver(9, 'RECHAZADO', 'Revisado'),
      'PATCH',
      '/admin/reportes/9',
      { estado: 'RECHAZADO', comentario: 'Revisado' },
    ],
    ['ocultar post', (s) => s.moderar(1, false), 'PATCH', '/admin/posts/1/ocultar', {}],
    [
      'restaurar comentario',
      (s) => s.moderar(3, true, true),
      'PATCH',
      '/admin/comentarios/3/restaurar',
      {},
    ],
  ];
  it.each(writes)('%s conserva verbo y JSON', (_label, operation, method, path, body) => {
    operation(service).subscribe();
    const r = http.expectOne(base + path);
    expect(r.request.method).toBe(method);
    expect(r.request.body).toEqual(body);
    r.flush({});
  });
  it('desfija con scope GLOBAL sin enviar null como texto', () => {
    service.desfijar(1, 'GLOBAL', null).subscribe();
    const r = http.expectOne((x) => x.url === base + '/posts/1/fijar');
    expect(r.request.params.get('scopeTipo')).toBe('GLOBAL');
    expect(r.request.params.has('scopeId')).toBe(false);
    r.flush(null);
  });
  it('sube con campo multipart archivo, sin imponer Content-Type', () => {
    const file = new File(['image'], 'foto.png', { type: 'image/png' });
    service.subirAdjunto(1, file).subscribe();
    const r = http.expectOne(base + '/posts/1/comentarios/adjuntos');
    expect(r.request.body.get('archivo')).toBe(file);
    expect(r.request.headers.has('Content-Type')).toBe(false);
    r.flush({
      objectKey: 'social/comentarios/1/foto.png',
      url: 'https://example.invalid/foto.png',
      mime: 'image/png',
      sizeBytes: 5,
    });
  });
  it.each(['USUARIO', 'EQUIPO', 'ACTIVIDAD'] as const)(
    'autocomplete %s genera el token contractual',
    (tipo) => {
      let token = '';
      service.sugerir(tipo, 'ana').subscribe((x) => (token = x[0].token));
      const r = http.expectOne((x) => x.url === base + '/menciones/buscar');
      expect(r.request.params.get('tipo')).toBe(tipo);
      r.flush(
        tipo === 'USUARIO'
          ? [{ idUsuario: 101, username: 'ana.lazaro', nombreCompleto: 'Ana' }]
          : tipo === 'EQUIPO'
            ? [{ idEquipo: 301, nombre: 'Bienestar' }]
            : [{ idActividad: 401, nombre: 'Pausa' }],
      );
      expect(token).toBe(
        tipo === 'USUARIO' ? '@ana.lazaro' : tipo === 'EQUIPO' ? '@equipo{301}' : '@actividad{401}',
      );
    },
  );
  it('busca comentarios y filtra usuarios de reacciones con paginación', () => {
    service.buscar('salud', 'COMENTARIO', 1).subscribe();
    let r = http.expectOne((x) => x.url === base + '/buscar');
    expect(r.request.params.get('tipo')).toBe('COMENTARIO');
    expect(r.request.params.get('page')).toBe('1');
    r.flush({});
    service.quienReacciono(3, true, 'LIKE', 2).subscribe();
    r = http.expectOne((x) => x.url === base + '/comentarios/3/reacciones/usuarios');
    expect(r.request.params.get('tipo')).toBe('LIKE');
    expect(r.request.params.get('page')).toBe('2');
    r.flush({});
  });
  it('consulta perfil público, guardados y reporte usando sus filtros', () => {
    service.perfil(101, 2).subscribe();
    http
      .expectOne((x) => x.url === base + '/usuarios/101/actividad' && x.params.get('page') === '2')
      .flush({});
    service.miContenido('GUARDADOS').subscribe();
    http
      .expectOne((x) => x.url === base + '/mi-contenido' && x.params.get('tab') === 'GUARDADOS')
      .flush({});
    service.reportes('ACEPTADO', 1).subscribe();
    http
      .expectOne((x) => x.url === base + '/admin/reportes' && x.params.get('estado') === 'ACEPTADO')
      .flush({});
  });
  it('conserva fechas ISO y consulta comentario enfocado sin crear vistas', () => {
    service.metricas('2026-10-01', '2026-10-25').subscribe();
    const r = http.expectOne((x) => x.url === base + '/admin/metricas');
    expect(r.request.params.get('desde')).toBe('2026-10-01');
    expect(r.request.params.get('hasta')).toBe('2026-10-25');
    r.flush({});
    service.comentario(1, 3).subscribe();
    http.expectOne(base + '/posts/1/comentarios/3').flush({});
    http.expectNone(base + '/posts/1');
  });
});
