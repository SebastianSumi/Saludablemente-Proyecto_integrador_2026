/** Entrada exclusiva de revisión visual. No se importa desde main.ts ni desde rutas productivas. */
import { Component, provideZonelessChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, RouterOutlet } from '@angular/router';
import {
  HttpErrorResponse,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { AuthService } from '../../../core/auth/auth-service';
import { SOCIAL_ROUTES } from '../social.routes';
import { BACKEND } from './backend-fixtures';

echarts.use([BarChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);
const fixtures = JSON.parse(
  JSON.stringify(BACKEND).replaceAll('https://example.invalid/equipo.png', '/img/logo-icon.svg'),
) as typeof BACKEND;
fixtures.perfil.autor.fotoUrl = new URL('/img/logo-icon.svg', location.origin).href;
fixtures.comentarios.content[0].autor.fotoUrl = new URL('/img/logo-icon.svg', location.origin).href;
// Lista explícitamente simulada para verificar desplazamiento y editor permanente.
const firstComment = structuredClone(fixtures.comentarios.content[0]);
fixtures.comentarios.content = Array.from({ length: 12 }, (_, i) => ({
  ...structuredClone(firstComment),
  idComentario: i ? 7000 + i : firstComment.idComentario,
}));
fixtures.comentarios.totalElements = 12;

// Galería simulada: el servidor de revisión genera SVG locales con dimensiones conocidas.
const photoOptions = new URLSearchParams(location.search);
const photoCount = Math.max(1, Math.min(30, Number(photoOptions.get('photos') || 15) || 15));
const firstOrientation = photoOptions.get('orientation') === 'landscape' ? 'landscape' : 'portrait';
const galleryUrls = Array.from(
  { length: photoCount },
  (_, i) =>
    new URL(
      '/social-gallery-fixtures/' +
        (i ? ['landscape', 'portrait', 'square'][i % 3] : firstOrientation) +
        '-' +
        i +
        '.svg',
      location.origin,
    ).href,
);
fixtures.detalleAutor.imagenes = galleryUrls;
fixtures.detalle.imagenes = galleryUrls;
// Variante explícitamente simulada para revisar el bloque de actividad del usuario.
fixtures.muro.posts.content.push({
  ...fixtures.muro.pinned[0],
  idPost: 9001,
  tipoContenido: 'ACTIVIDAD',
  refId: 401,
  titulo: 'Primera actividad',
  imagenUrl: new URL('/img/logo-icon.svg', location.origin).href,
  imagenes: galleryUrls,
  resumen: 'Compartimos una nueva actividad para conectar y cuidarnos. #Bienestar',
  fechaEvento: '2026-10-29',
  horaInicio: '19:06:00',
  horaFin: '20:06:00',
  lugar: 'Fernando Stall',
  estado: 'PROGRAMADA',
  coordinadorNombre: 'Eulalia Soncco Ortiz',
  puedoEditar: false,
  puedoEliminar: false,
});
fixtures.muro.posts.totalElements = fixtures.muro.posts.content.length;
// Perfil/reacciones exclusivamente simulados para aceptación visual.
const profileHeader = (id: number) => ({
  autor: {
    ...fixtures.perfil.autor,
    idUsuario: id,
    username: 'usuario' + id,
    nombreCompleto: 'Usuario ' + id,
    fotoUrl: new URL('/img/logo-icon.svg', location.origin).href,
  },
  portadaUrl: galleryUrls.find((u) => u.includes('landscape')) || galleryUrls[0],
  presentacion: 'Compartiendo hábitos saludables y experiencias con mi comunidad.',
  propietario: id === 101,
  equipos: [
    {
      idUsuario: id,
      idEquipo: 301,
      nombreEquipo: 'Bienestar',
      logoEquipoUrl: null,
      rolEnEquipo: 'Colaborador',
      activo: true,
    },
  ],
});
let ownHeader = profileHeader(101);
const companions = Array.from({ length: 9 }, (_, i) => ({
  ...profileHeader(102 + i).autor,
  nombreCompleto: [
    'Ana Torres',
    'Diego Salas',
    'María Flores',
    'Luis Quispe',
    'Rosa Díaz',
    'Pedro López',
    'Sofía Castro',
    'Juan Ríos',
    'Elena Paz',
  ][i],
}));
const profilePosts = {
  ...fixtures.perfil.publicaciones,
  content:
    photoOptions.get('empty') === '1'
      ? []
      : Array.from({ length: 3 }, (_, i) => ({
          ...fixtures.perfil.publicaciones.content[0],
          idPost: i ? 9100 + i : fixtures.perfil.publicaciones.content[0].idPost,
          imagenes: galleryUrls.slice(i, i + 3),
          autor: ownHeader.autor,
          titulo: [
            'Una experiencia para compartir',
            'Creciendo con mi equipo',
            'Un día para cuidarnos',
          ][i],
        })),
  totalElements: photoOptions.get('empty') === '1' ? 0 : 3,
  totalPages: photoOptions.get('empty') === '1' ? 0 : 1,
};
fixtures.muro.pinned[0].totalReacciones = 3;
fixtures.muro.pinned[0].reaccionesPorTipo = { LIKE: 2, LOVE: 1 };
const reactions = new Map<string, typeof fixtures.detalle.reacciones>();
@Component({
  selector: 'app-root',
  imports: [TuiRoot, RouterOutlet],
  template: `<tui-root
    ><div
      style="padding:8px 16px;background:var(--sal-hover);color:var(--sal-ink-soft);text-align:center;font-size:12px"
    >
      Vista aislada de prueba · Fixtures de contratos · Sin conexión al backend
    </div>
    <router-outlet
  /></tui-root>`,
})
class Preview {}
bootstrapApplication(Preview, {
  providers: [
    provideZonelessChangeDetection(),
    provideTaiga(),
    provideEchartsCore({ echarts }),
    provideRouter([
      { path: 'social', children: SOCIAL_ROUTES },
      { path: '', pathMatch: 'full', redirectTo: 'social' },
    ]),
    { provide: AuthService, useValue: { tieneRol: () => true, tieneAlgunRol: () => true } },
    provideHttpClient(
      withInterceptors([
        (request, next) => {
          const path = new URL(request.url, location.origin).pathname;
          if (!path.includes('/api/v1/')) return next(request);
          let body: unknown;
          if (path.endsWith('/api/v1/mi-perfil')) body = { idUsuario: 101, idPersona: 1001 };
          else if (path.endsWith('/social/mi-perfil')) {
            if (request.method === 'PATCH')
              ownHeader = {
                ...ownHeader,
                presentacion: (request.body as { presentacion: string }).presentacion,
              };
            body = ownHeader;
          } else if (/\/social\/mi-perfil\/(foto|portada)$/.test(path)) body = ownHeader;
          else if (/\/usuarios\/\d+\/perfil$/.test(path)) {
            const id = Number(path.match(/usuarios\/(\d+)/)![1]);
            body = id === 101 ? ownHeader : profileHeader(id);
          } else if (/\/usuarios\/\d+\/companeros$/.test(path))
            body = {
              ...fixtures.comentarios,
              content: companions,
              totalElements: 9,
              totalPages: 1,
              number: 0,
              size: 9,
            };
          else if (path.endsWith('/muro') || /\/muro\/(equipos|actividades)\/\d+$/.test(path))
            body = fixtures.muro;
          else if (path.endsWith('/hashtags/tendencia')) body = fixtures.metricas.hashtagsTendencia;
          else if (/\/equipos\/\d+\/perfil$/.test(path)) body = fixtures.muro.pinned[0].equipo;
          else if (path.endsWith('/equipos/mi-equipo'))
            body = { id: 301, nombre: 'Bienestar', activo: true };
          else if (/\/posts\/\d+\/comentarios\/\d+$/.test(path))
            body = fixtures.comentarios.content[0];
          else if (/\/posts\/\d+\/comentarios$/.test(path)) body = fixtures.comentarios;
          else if (/\/posts\/\d+$/.test(path)) body = fixtures.detalleAutor;
          else if (path.endsWith('/mi-contenido'))
            body = { ...fixtures.guardados, publicaciones: profilePosts };
          else if (/\/usuarios\/\d+\/actividad$/.test(path))
            body = {
              ...fixtures.perfil,
              autor: profileHeader(Number(path.match(/usuarios\/(\d+)/)![1])).autor,
              publicaciones: profilePosts,
            };
          else if (path.endsWith('/buscar') && !path.endsWith('/menciones/buscar'))
            body = {
              ...fixtures.busquedaComentarios,
              tipo: request.params.get('tipo') ?? 'COMENTARIO',
            };
          else if (path.endsWith('/admin/reportes')) body = fixtures.reportes;
          else if (path.endsWith('/admin/metricas')) body = fixtures.metricas;
          else if (path.endsWith('/menciones/buscar')) {
            const logoUrl = new URL('/img/logo-icon.svg', location.origin).href;
            body =
              request.params.get('tipo') === 'ACTIVIDAD'
                ? [
                    {
                      idActividad: 401,
                      nombre: 'Caminata saludable',
                      estado: 'PROGRAMADA',
                      fechaActividad: '2026-10-30',
                      logoUrl,
                    },
                  ]
                : request.params.get('tipo') === 'EQUIPO'
                  ? [{ idEquipo: 301, nombre: 'Bienestar', logoUrl }]
                  : [
                      {
                        idUsuario: 102,
                        username: 'ana.torres',
                        nombreCompleto: 'Ana Torres',
                        fotoUrl: logoUrl,
                      },
                    ];
          } else if (/\/reacciones\/usuarios$/.test(path)) {
            const isComment = path.includes('/comentarios/');
            const content = isComment
              ? []
              : [
                  {
                    idUsuario: 102,
                    autor: companions[0],
                    tipo: 'LIKE',
                    fechaReaccion: '2026-10-29T10:00:00',
                  },
                  {
                    idUsuario: 103,
                    autor: companions[1],
                    tipo: 'LIKE',
                    fechaReaccion: '2026-10-29T10:05:00',
                  },
                  {
                    idUsuario: 104,
                    autor: companions[2],
                    tipo: 'LOVE',
                    fechaReaccion: '2026-10-29T10:10:00',
                  },
                ].filter(
                  (r) => !request.params.get('tipo') || request.params.get('tipo') === r.tipo,
                );
            body = {
              ...fixtures.comentarios,
              content,
              totalElements: content.length,
              totalPages: 1,
              number: 0,
            };
          } else if (/\/reacciones$/.test(path)) {
            const match = path.match(/\/(posts|comentarios)\/(\d+)\/reacciones$/)!;
            const key = match[1] + ':' + match[2];
            let value = reactions.get(key);
            if (!value) {
              const post = fixtures.muro.pinned
                .concat(fixtures.muro.posts.content)
                .find((x) => x.idPost === Number(match[2]));
              value = post
                ? {
                    total: post.totalReacciones,
                    porTipo: { ...post.reaccionesPorTipo },
                    reaccionDelUsuario: post.reaccionDelUsuario,
                  }
                : { total: 0, porTipo: {}, reaccionDelUsuario: null };
            }
            if (request.method === 'POST' || request.method === 'DELETE') {
              const old = value.reaccionDelUsuario;
              const next =
                request.method === 'DELETE' ? null : (request.body as { tipo: typeof old }).tipo;
              const counts = { ...value.porTipo };
              if (old) counts[old] = Math.max(0, (counts[old] || 0) - 1);
              if (next) counts[next] = (counts[next] || 0) + 1;
              value = {
                total: Object.values(counts).reduce((n, x) => n + (x || 0), 0),
                porTipo: counts,
                reaccionDelUsuario: next,
              };
              reactions.set(key, value);
              for (const post of fixtures.muro.pinned.concat(fixtures.muro.posts.content))
                if (post.idPost === Number(match[2]) && match[1] === 'posts') {
                  post.totalReacciones = value.total;
                  post.reaccionesPorTipo = { ...value.porTipo };
                  post.reaccionDelUsuario = value.reaccionDelUsuario;
                }
              return of(new HttpResponse({ status: 204, body: null }));
            }
            body = value;
          } else
            return throwError(
              () =>
                new HttpErrorResponse({
                  status: 404,
                  error: { message: 'Operación fuera del preview de solo lectura' },
                }),
            );
          return of(new HttpResponse({ status: 200, body }));
        },
      ]),
    ),
  ],
}).catch((error) => console.error(error));
