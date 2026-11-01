import { Routes } from '@angular/router';
import { cambiosClinicosGuard } from './features/admin/alertaclinica/etiquetas/cambios-pendientes';
import { AuthGuard } from './core/auth/auth-guard';
import { RoleGuard } from './core/auth/role-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./core/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    loadComponent: () => import('./core/registro/registro').then((m) => m.Registro),
  },
  {
    path: '',
    canActivate: [AuthGuard],   // solo login; sin restricciones por rol por ahora
    loadComponent: () => import('./core/layout/layout').then((m) => m.Layout),
    children: [
      {
        path: 'social',
        loadChildren: () => import('./features/social/social.routes').then((m) => m.SOCIAL_ROUTES),
      },
      {
        path: 'reportes',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        loadComponent: () => import('./core/inicio/inicio').then((m) => m.Inicio),
      },
      {
        path: 'no-autorizado',
        loadComponent: () =>
          import('./core/auth/no-autorizado').then((m) => m.NoAutorizado),
      },
      {
        path: 'admin',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/home/admin-home').then((m) => m.AdminHome),
      },
      {
        path: 'admin/panel',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/panel/admin-panel').then((m) => m.AdminPanel),
      },
      {
        path: 'admin/modulo/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/home/admin-modulo/admin-modulo').then(
            (m) => m.AdminModulo
          ),
      },
      {
        path: 'admin/personas',
        redirectTo: '/personal/personas',
        pathMatch: 'full',
      },
      {
        path: 'admin/equipos',
        redirectTo: '/teams',
        pathMatch: 'full',
      },
      {
        path: 'admin/usuarios',
        redirectTo: '/seguridad/usuarios',
        pathMatch: 'full',
      },
      {
        path: 'admin/evaluaciones',
        redirectTo: '/nutricional/evaluaciones',
        pathMatch: 'full',
      },
      {
        path: 'admin/alertas',
        redirectTo: '/alertaclinica/alertas',
        pathMatch: 'full',
      },
      {
        path: 'admin/asistencias',
        redirectTo: '/asistencias/registro',
        pathMatch: 'full',
      },
      {
        path: 'admin/metas',
        redirectTo: '/metas',
        pathMatch: 'full',
      },
      {
        path: 'admin/noticias',
        redirectTo: '/noticias',
        pathMatch: 'full',
      },
      {
        path: 'colaborador',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/inicio/col-inicio').then((m) => m.ColInicio),
      },
      {
        // La ficha vive en /mi-perfil (única, con tabs). Esta ruta solo redirige.
        path: 'colaborador/mi-perfil',
        redirectTo: '/mi-perfil',
        pathMatch: 'full',
      },
      {
        path: 'colaborador/mis-evaluaciones',
        canActivate: [RoleGuard], data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/mis-evaluaciones/col-mis-evaluaciones').then(
            (m) => m.ColMisEvaluaciones
          ),
      },
      {
        path: 'colaborador/mis-actividades',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/mis-actividades/col-mis-actividades').then(
            (m) => m.ColMisActividades
          ),
      },
      {
        path: 'colaborador/mis-actividades/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/mis-actividades/col-mis-actividades-detalle').then(
            (m) => m.ColMisActividadesDetalle
          ),
      },
      {
        path: 'colaborador/mis-alertas',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/alerta/alerta-list').then(
            (m) => m.ColAlertaList
          ),
      },
      {
        path: 'colaborador/mis-alertas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/alerta/alerta-detail').then(
            (m) => m.ColAlertaDetail
          ),
      },
      {
        path: 'colaborador/mis-notificaciones',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/notificaciones/mis-notificaciones').then(
            (m) => m.MisNotificaciones
          ),
      },
      {
        path: 'colaborador/mis-metas',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/metas/meta-list').then(
            (m) => m.ColMetaList
          ),
      },
      {
        path: 'colaborador/mis-metas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/metas/meta-form').then(
            (m) => m.ColMetaForm
          ),
      },
      {
        path: 'colaborador/mis-metas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/metas/meta-detail').then(
            (m) => m.ColMetaDetail
          ),
      },
      {
        path: 'colaborador/mis-asistencias',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/asistencias/asistencia-list').then(
            (m) => m.ColAsistenciaList
          ),
      },
      {
        path: 'colaborador/evaluaciones/nutricional/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/evaluaciones/evaluacion-detail').then(
            (m) => m.ColEvaluacionDetail
          ),
      },
      {
        path: 'colaborador/evaluaciones/aptitud/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/evaluaciones/evaluacion-detail').then(
            (m) => m.ColEvaluacionDetail
          ),
      },
      {
        path: 'colaborador/noticias',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/noticias/noticia-feed').then(
            (m) => m.ColNoticiaFeed
          ),
      },
      {
        path: 'colaborador/noticias/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Colaborador', 'Administrador', 'Coordinador', 'Evaluador'] },
        loadComponent: () =>
          import('./features/colaborador/noticias/noticia-detail').then(
            (m) => m.ColNoticiaDetail
          ),
      },
      // ============================================================
      // VISTA EVALUADOR — rol Evaluador (y Administrador como supervisor)
      // Solo crear/registrar y atender; sin acciones destructivas.
      // ============================================================
      {
        path: 'evaluador',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/inicio/eval-inicio').then((m) => m.EvalInicio),
      },
      {
        path: 'evaluador/colaboradores',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/colaboradores/eval-colaboradores').then(
            (m) => m.EvalColaboradores
          ),
      },
      {
        path: 'evaluador/colaboradores/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/colaboradores/eval-colaborador-detalle').then(
            (m) => m.EvalColaboradorDetalle
          ),
      },
      {
        path: 'evaluador/nutricional',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/nutricional/eval-nutricional-list').then(
            (m) => m.EvalNutricionalList
          ),
      },
      {
        path: 'evaluador/nutricional/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/nutricional/eval-nutricional-form').then(
            (m) => m.EvalNutricionalForm
          ),
      },
      {
        path: 'evaluador/nutricional/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/nutricional/eval-nutricional-detalle').then(
            (m) => m.EvalNutricionalDetalle
          ),
      },
      {
        path: 'evaluador/aptitud',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/aptitud/eval-aptitud-list').then(
            (m) => m.EvalAptitudList
          ),
      },
      {
        path: 'evaluador/aptitud/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/aptitud/eval-aptitud-form').then(
            (m) => m.EvalAptitudForm
          ),
      },
      {
        path: 'evaluador/aptitud/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/aptitud/eval-aptitud-detalle').then(
            (m) => m.EvalAptitudDetalle
          ),
      },
      {
        path: 'evaluador/alertas',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/alertas/eval-alertas-list').then(
            (m) => m.EvalAlertasList
          ),
      },
      {
        path: 'evaluador/alertas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/alertas/eval-alerta-detalle').then(
            (m) => m.EvalAlertaDetalle
          ),
      },
      {
        path: 'evaluador/metas',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/metas/eval-metas-list').then((m) => m.EvalMetasList),
      },
      {
        path: 'evaluador/metas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/metas/eval-meta-form').then((m) => m.EvalMetaForm),
      },
      {
        path: 'evaluador/metas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/metas/eval-meta-detalle').then(
            (m) => m.EvalMetaDetalle
          ),
      },
      {
        path: 'evaluador/reportes',
        canActivate: [RoleGuard],
        data: { roles: ['Evaluador', 'Administrador'] },
        loadComponent: () =>
          import('./features/evaluador/reportes/eval-reportes').then((m) => m.EvalReportes),
      },
      // Noticias (estáticas antes que :id)
      {
        path: 'noticias',
        loadComponent: () =>
          import('./features/admin/noticias/feed/noticia-feed').then(
            (m) => m.NoticiaFeedComponent
          ),
      },
      {
        path: 'noticias/admin',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/noticias/noticia/noticia-list').then(
            (m) => m.NoticiaList
          ),
      },
      {
        path: 'noticias/admin/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/noticias/noticia/noticia-form').then(
            (m) => m.NoticiaForm
          ),
      },
      {
        path: 'noticias/admin/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/noticias/noticia/noticia-form').then(
            (m) => m.NoticiaForm
          ),
      },
      {
        path: 'noticias/admin/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/noticias/noticia/noticia-detail').then(
            (m) => m.NoticiaDetail
          ),
      },
      {
        path: 'noticias/categorias',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/noticias/categoria/categoria-list').then(
            (m) => m.CategoriaList
          ),
      },
      {
        path: 'noticias/:id',
        loadComponent: () =>
          import('./features/admin/noticias/noticia/noticia-detail').then(
            (m) => m.NoticiaDetail
          ),
      },
      // Seguridad / Usuarios — solo Administrador (vista de terceros)
      {
        path: 'seguridad/usuarios',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/seguridad/usuario/usuario-list').then(
            (m) => m.UsuarioList
          ),
      },
      {
        path: 'seguridad/usuarios/nuevo',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/seguridad/usuario/usuario-form').then(
            (m) => m.UsuarioForm
          ),
      },
      {
        path: 'seguridad/usuarios/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/seguridad/usuario/usuario-detail').then(
            (m) => m.UsuarioDetail
          ),
      },
      // Notificaciones
      {
        path: 'notificaciones/bandeja',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/notificaciones/notificacion/bandeja').then(
            (m) => m.NotificacionBandeja
          ),
      },
      {
        path: 'notificaciones',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/notificaciones/notificacion/notificacion-list').then(
            (m) => m.NotificacionList
          ),
      },
      {
        path: 'notificaciones/plantillas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/notificaciones/plantilla/plantilla-list').then(
            (m) => m.PlantillaList
          ),
      },
      {
        path: 'notificaciones/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/notificaciones/notificacion/notificacion-detail').then(
            (m) => m.NotificacionDetail
          ),
      },
      // Metas
      {
        path: 'metas',
        loadComponent: () =>
          import('./features/admin/metas/meta/meta-list').then((m) => m.MetaList),
      },
      {
        path: 'metas/nueva',
        loadComponent: () =>
          import('./features/admin/metas/meta/meta-form').then((m) => m.MetaForm),
      },
      {
        path: 'metas/:id',
        loadComponent: () =>
          import('./features/admin/metas/meta/meta-detail').then((m) => m.MetaDetail),
      },
      // Asistencias
      {
        path: 'asistencias/registro',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/registro/asistencia-list').then(
            (m) => m.AsistenciaList
          ),
      },
      {
        path: 'asistencias/registro/nuevo',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/registro/asistencia-form').then(
            (m) => m.AsistenciaForm
          ),
      },
      {
        path: 'asistencias/cuarentena',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/cuarentena/cuarentena-list').then(
            (m) => m.CuarentenaList
          ),
      },
      {
        path: 'asistencias/exoneraciones',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/exoneracion/exoneracion-list').then(
            (m) => m.ExoneracionList
          ),
      },
      {
        path: 'asistencias/exoneraciones/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/exoneracion/exoneracion-form').then(
            (m) => m.ExoneracionForm
          ),
      },
      {
        path: 'asistencias/sincronizacion',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/sincronizacion/sincronizacion').then(
            (m) => m.Sincronizacion
          ),
      },
      {
        path: 'asistencias/qr/:idActividad',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/qr/qr-viewer').then((m) => m.QrViewer),
      },
      {
        path: 'asistencias/dashboard',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/asistencias/dashboard/dashboard').then(
            (m) => m.AsistenciaDashboard
          ),
      },
      // Catálogos
      {
        path: 'catalogos/areas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/area/area-list').then((m) => m.AreaList),
      },
      {
        path: 'catalogos/areas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/area/area-form').then((m) => m.AreaForm),
      },
      {
        path: 'catalogos/areas/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/area/area-form').then((m) => m.AreaForm),
      },
      // Teams (módulo propio, consume /api/v1/equipos)
      {
        path: 'mi-equipo',
        loadComponent: () => import('./features/admin/teams/mi-equipo').then(m => m.MiEquipo),
      },
      {
        path: 'teams',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/teams/team-list').then((m) => m.TeamList),
      },
      {
        path: 'teams/nuevo',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/teams/team-form').then((m) => m.TeamForm),
      },
      {
        path: 'teams/:id/editar',
        loadComponent: () =>
          import('./features/admin/teams/team-form').then((m) => m.TeamForm),
      },
      // Compatibilidad: la antigua ruta de equipos redirige al módulo Teams
      {
        path: 'personal/equipos',
        redirectTo: '/teams',
        pathMatch: 'full',
      },
      {
        path: 'personal/equipos/nueva',
        redirectTo: '/teams/nuevo',
        pathMatch: 'full',
      },
      {
        path: 'personal/equipos/:id/editar',
        redirectTo: '/teams/:id/editar',
        pathMatch: 'full',
      },
      // Personal - Personas
      {
        path: 'personal/personas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/personal/persona/persona-list').then((m) => m.PersonaList),
      },
      {
        path: 'personal/personas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/personal/persona/persona-form').then((m) => m.PersonaForm),
      },
      {
        path: 'personal/personas/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/personal/persona/persona-form').then((m) => m.PersonaForm),
      },
      {
        path: 'personal/personas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/personal/persona/persona-detail').then((m) => m.PersonaDetail),
      },
      {
        path: 'personal/reportes',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/personal/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'catalogo/periodos',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/periodo/periodo-list').then((m) => m.PeriodoList),
      },
      {
        path: 'catalogo/periodos/nuevo',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/periodo/periodo-form').then((m) => m.PeriodoForm),
      },
      {
        path: 'catalogo/periodos/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/catalogo/periodo/periodo-form').then((m) => m.PeriodoForm),
      },

      // ── Jornadas Nutricionales ─────────────────────────────────────────────
      // Vista pública (colaboradores y admin pueden ver)
      {
        path: 'nutricional/jornadas',
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-list').then(
            (m) => m.JornadaList
          ),
      },
      {
        path: 'nutricional/jornadas/:id',
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-detail').then(
            (m) => m.JornadaDetail
          ),
      },
      {
        path: 'nutricional/mis-inscripciones',
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/mis-inscripciones').then(
            (m) => m.MisInscripciones
          ),
      },
      // Vista admin (solo Administrador)
      {
        path: 'admin/nutricional/jornadas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-admin-list').then(
            (m) => m.JornadaAdminList
          ),
      },
      {
        path: 'admin/nutricional/jornadas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-form').then(
            (m) => m.JornadaForm
          ),
      },
      {
        path: 'admin/nutricional/jornadas/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-form').then(
            (m) => m.JornadaForm
          ),
      },
      {
        path: 'admin/nutricional/jornadas/:id/inscritos',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/jornada/jornada-inscritos').then(
            (m) => m.JornadaInscritos
          ),
      },
      // ── Fin Jornadas Nutricionales ─────────────────────────────────────────

      {
        path: 'nutricional/evaluaciones',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/evaluacion/evaluacion-list').then((m) => m.EvaluacionList),
      },
      {
        path: 'nutricional/evaluaciones/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/evaluacion/evaluacion-form').then((m) => m.EvaluacionForm),
      },
      {
        path: 'nutricional/evaluaciones/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/evaluacion/evaluacion-detail').then((m) => m.EvaluacionDetail),
      },
      {
        path: 'nutricional/dashboard',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/dashboard/dashboard').then(
            (m) => m.Dashboard
          ),
      },
      {
        path: 'nutricional/evaluaciones/:id/antropometrico',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/antropometrico/antropometrico-form').then(
            (m) => m.AntropometricoForm
          ),
      },
      {
        path: 'nutricional/evaluaciones/:id/bioquimico',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/bioquimico/bioquimico-form').then(
            (m) => m.BioquimicoForm
          ),
      },
      {
        path: 'nutricional/importar-bioquimico',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/nutricional/bioquimico/bioquimico-import').then(
            (m) => m.BioquimicoImport
          ),
      },
      {
        path: 'aptitud/dashboard',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/aptitud/dashboard/dashboard')
            .then((m) => m.Dashboard),
      },
      {
        path: 'aptitud',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/aptitud/evaluacion/evaluacion-list')
            .then((m) => m.EvaluacionList),
      },
      {
        path: 'aptitud/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/aptitud/evaluacion/evaluacion-form')
            .then((m) => m.EvaluacionForm),
      },
      {
        path: 'aptitud/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/aptitud/evaluacion/evaluacion-detail')
            .then((m) => m.EvaluacionDetail),
      },
      {
        path: 'actividades',
        loadComponent: () =>
          import('./features/admin/actividades/agenda/actividad-list')
            .then((m) => m.ActividadList),
      },
      {
        path: 'actividades/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/actividades/agenda/actividad-form')
            .then((m) => m.ActividadForm),
      },
      {
        path: 'actividades/reportes',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/actividades/reporte/actividad-reporte')
            .then((m) => m.ActividadReporte),
      },
      {
        path: 'actividades/:id',
        loadComponent: () =>
          import('./features/admin/actividades/agenda/actividad-detail')
            .then((m) => m.ActividadDetail),
      },
      {
        path: 'actividades/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/actividades/agenda/actividad-form')
            .then((m) => m.ActividadForm),
      },
      {
        path: 'alertaclinica',
        redirectTo: 'alertaclinica/dashboard',
        pathMatch: 'full',
      },
      {
        path: 'alertaclinica/etiquetas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/etiqueta-list').then(m => m.EtiquetaList),
      },
      {
        path: 'alertaclinica/etiquetas/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        canDeactivate: [cambiosClinicosGuard],
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/etiqueta-form').then(m => m.EtiquetaForm),
      },
      {
        path: 'alertaclinica/etiquetas/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        canDeactivate: [cambiosClinicosGuard],
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/etiqueta-form').then(m => m.EtiquetaForm),
      },
      {
        path: 'alertaclinica/etiquetas/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        canDeactivate: [cambiosClinicosGuard],
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/etiqueta-detail').then(m => m.EtiquetaDetail),
      },
      {
        path: 'alertaclinica/parametros',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/parametro-list').then(m => m.ParametroList),
      },
      {
        path: 'alertaclinica/parametros/:id',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        canDeactivate: [cambiosClinicosGuard],
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/parametro-detail').then(m => m.ParametroDetail),
      },
      {
        path: 'alertaclinica/reportes/etiquetas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        
        loadComponent: () => import('./features/admin/alertaclinica/etiquetas/etiqueta-reporte').then(m => m.EtiquetaReporte),
      },
      {
        path: 'alertaclinica/dashboard',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'alertaclinica/alertas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/alerta/alerta-clinica-list').then(
            (m) => m.AlertaClinicaList,
          ),
      },
      {
        path: 'alertaclinica/alertas/:id',
        canDeactivate: [cambiosClinicosGuard],
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/alerta/alerta-clinica-detail').then(
            (m) => m.AlertaClinicaDetail,
          ),
      },
      {
        path: 'alertaclinica/reglas/perimetro-abdominal',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/perimetro-abdominal/regla-perimetro-abdominal-list')
            .then((m) => m.ReglaPerimetroAbdominalList),
      },
      {
        path: 'alertaclinica/reglas/perimetro-abdominal/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/perimetro-abdominal/regla-perimetro-abdominal-form')
            .then((m) => m.ReglaPerimetroAbdominalForm),
      },
      {
        path: 'alertaclinica/reglas/perimetro-abdominal/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/perimetro-abdominal/regla-perimetro-abdominal-form')
            .then((m) => m.ReglaPerimetroAbdominalForm),
      },

      // ===== Reglas — Porcentaje de grasa =====
      {
        path: 'alertaclinica/reglas/porcentaje-grasa',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/porcentaje-grasa/regla-porcentaje-grasa-list')
            .then((m) => m.ReglaPorcentajeGrasaList),
      },
      {
        path: 'alertaclinica/reglas/porcentaje-grasa/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/porcentaje-grasa/regla-porcentaje-grasa-form')
            .then((m) => m.ReglaPorcentajeGrasaForm),
      },
      {
        path: 'alertaclinica/reglas/porcentaje-grasa/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/porcentaje-grasa/regla-porcentaje-grasa-form')
            .then((m) => m.ReglaPorcentajeGrasaForm),
      },

      // ===== Reglas — Masa muscular =====
      {
        path: 'alertaclinica/reglas/masa-muscular',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/masa-muscular/regla-masa-muscular-list')
            .then((m) => m.ReglaMasaMuscularList),
      },
      {
        path: 'alertaclinica/reglas/masa-muscular/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/masa-muscular/regla-masa-muscular-form')
            .then((m) => m.ReglaMasaMuscularForm),
      },
      {
        path: 'alertaclinica/reglas/masa-muscular/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/masa-muscular/regla-masa-muscular-form')
            .then((m) => m.ReglaMasaMuscularForm),
      },
      {
        path: 'alertaclinica/reglas/imc',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/imc/regla-imc-list')
            .then((m) => m.ReglaImcList),
      },
      {
        path: 'alertaclinica/reglas/imc/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/imc/regla-imc-form')
            .then((m) => m.ReglaImcForm),
      },
      {
        path: 'alertaclinica/reglas/imc/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/imc/regla-imc-form')
            .then((m) => m.ReglaImcForm),
      },

            // Grasa visceral
      {
        path: 'alertaclinica/reglas/grasa-visceral',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/grasa-visceral/regla-grasa-visceral-list')
            .then((m) => m.ReglaGrasaVisceralList),
      },
      {
        path: 'alertaclinica/reglas/grasa-visceral/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/grasa-visceral/regla-grasa-visceral-form')
            .then((m) => m.ReglaGrasaVisceralForm),
      },
      {
        path: 'alertaclinica/reglas/grasa-visceral/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/grasa-visceral/regla-grasa-visceral-form')
            .then((m) => m.ReglaGrasaVisceralForm),
      },

      // Glucosa
      {
        path: 'alertaclinica/reglas/glucosa',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/glucosa/regla-glucosa-list')
            .then((m) => m.ReglaGlucosaList),
      },
      {
        path: 'alertaclinica/reglas/glucosa/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/glucosa/regla-glucosa-form')
            .then((m) => m.ReglaGlucosaForm),
      },
      {
        path: 'alertaclinica/reglas/glucosa/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/glucosa/regla-glucosa-form')
            .then((m) => m.ReglaGlucosaForm),
      },

      // Colesterol
      {
        path: 'alertaclinica/reglas/colesterol',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/colesterol/regla-colesterol-list')
            .then((m) => m.ReglaColesterolList),
      },
      {
        path: 'alertaclinica/reglas/colesterol/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/colesterol/regla-colesterol-form')
            .then((m) => m.ReglaColesterolForm),
      },
      {
        path: 'alertaclinica/reglas/colesterol/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/colesterol/regla-colesterol-form')
            .then((m) => m.ReglaColesterolForm),
      },

      // Trigliceridos
      {
        path: 'alertaclinica/reglas/trigliceridos',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/trigliceridos/regla-trigliceridos-list')
            .then((m) => m.ReglaTrigliceridosList),
      },
      {
        path: 'alertaclinica/reglas/trigliceridos/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/trigliceridos/regla-trigliceridos-form')
            .then((m) => m.ReglaTrigliceridosForm),
      },
      {
        path: 'alertaclinica/reglas/trigliceridos/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/trigliceridos/regla-trigliceridos-form')
            .then((m) => m.ReglaTrigliceridosForm),
      },
      {
        path: 'alertaclinica/reglas/presion-arterial',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/presion-arterial/regla-presion-arterial-list')
            .then((m) => m.ReglaPresionArterialList),
      },
      {
        path: 'alertaclinica/reglas/presion-arterial/nueva',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/presion-arterial/regla-presion-arterial-form')
            .then((m) => m.ReglaPresionArterialForm),
      },
      {
        path: 'alertaclinica/reglas/presion-arterial/:id/editar',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () =>
          import('./features/admin/alertaclinica/reglas/presion-arterial/regla-presion-arterial-form')
            .then((m) => m.ReglaPresionArterialForm),
      },
      {
        path: 'mi-perfil',
        loadComponent: () =>
          import('./features/admin/perfil/perfil').then((m) => m.Perfil),
      },
    ],
  },
];
