import { IMG_ALERTAS, IMG_DASHBOARD_CLINICO, imagenRegla } from '../alertaclinica/ilustracion-alerta';

export interface AdminHijo {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
  /** Si es true, se muestra como card grande resaltada en /admin/modulo/:id. */
  destacado?: boolean;
  /** Ilustración opcional (WebP en public/img). */
  imagen?: string;
}

export type AdminColor =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'warning'
  | 'error'
  | 'violet';

export interface AdminModuloDef {
  id: string;
  titulo: string;
  descripcion: string;
  icono: string;
  color: AdminColor;
  hijos: AdminHijo[];
  /** Ilustración opcional de portada del módulo. */
  imagen?: string;
}

export const ADMIN_MODULOS: readonly AdminModuloDef[] = [
  {
    id: 'social', titulo: 'Social', descripcion: 'Comunidad, publicaciones y conversaciones.',
    icono: '@tui.messages-square', color: 'primary',
    hijos: [
      { titulo: 'Muro social', descripcion: 'Actividades, noticias oficiales y publicaciones.', icono: '@tui.messages-square', ruta: '/social' },
      { titulo: 'Moderación', descripcion: 'Revisar reportes y cuidar la comunidad.', icono: '@tui.shield', ruta: '/social/admin/reportes' },
      { titulo: 'Métricas', descripcion: 'Participación e interacciones por periodo.', icono: '@tui.chart-column', ruta: '/social/admin/metricas' },
    ],
  },
  {
    id: 'personal',
    titulo: 'Personal',
    descripcion: 'Personas, equipos y reportes del talento.',
    icono: '@tui.users',
    color: 'primary',
    hijos: [
      { titulo: 'Personas', descripcion: 'Ficha 360°, filtros, QR y preferencias.', icono: '@tui.users', ruta: '/personal/personas' },
      { titulo: 'Reportes de personal', descripcion: 'Resumen agregado y ausentismo.', icono: '@tui.clipboard-list', ruta: '/personal/reportes' },
    ],
  },
  {
    id: 'teams',
    titulo: 'Teams',
    descripcion: 'Equipos de trabajo del sistema.',
    icono: '@tui.user-check',
    color: 'secondary',
    hijos: [
      { titulo: 'Equipos', descripcion: 'Equipos de trabajo y miembros.', icono: '@tui.user-check', ruta: '/teams' },
      { titulo: 'Nuevo equipo', descripcion: 'Crear un equipo de trabajo.', icono: '@tui.plus', ruta: '/teams/nuevo' },
    ],
  },
  {
    id: 'nutricional',
    titulo: 'Nutricional',
    descripcion: 'Evaluaciones, antropometría y bioquímica.',
    icono: '@tui.apple',
    color: 'success',
    hijos: [
      { titulo: 'Evaluaciones', descripcion: 'Evaluaciones nutricionales y detalle.', icono: '@tui.apple', ruta: '/nutricional/evaluaciones' },
      { titulo: 'Jornadas', descripcion: 'Administrar jornadas, cupos y turnos.', icono: '@tui.calendar-heart', ruta: '/admin/nutricional/jornadas' },
      { titulo: 'Nueva jornada', descripcion: 'Programar una jornada de evaluación.', icono: '@tui.calendar-plus', ruta: '/admin/nutricional/jornadas/nueva' },
      { titulo: 'Mis inscripciones', descripcion: 'Turnos reservados en jornadas.', icono: '@tui.ticket', ruta: '/nutricional/mis-inscripciones' },
      { titulo: 'Dashboard nutricional', descripcion: 'Indicadores y tendencias.', icono: '@tui.layout-dashboard', ruta: '/nutricional/dashboard' },
      { titulo: 'Importar bioquímico', descripcion: 'Carga masiva desde Excel.', icono: '@tui.file-up', ruta: '/nutricional/importar-bioquimico' },
    ],
  },
  {
    id: 'aptitud',
    titulo: 'Aptitud física',
    descripcion: 'Fuerza, cardio y pruebas físicas.',
    icono: '@tui.activity',
    color: 'violet',
    hijos: [
      { titulo: 'Evaluaciones', descripcion: 'Evaluaciones de aptitud física.', icono: '@tui.activity', ruta: '/aptitud' },
      { titulo: 'Dashboard de aptitud', descripcion: 'Métricas agregadas y resultados físicos.', icono: '@tui.layout-dashboard', ruta: '/aptitud/dashboard' },
    ],
  },
  {
    id: 'actividades',
    titulo: 'Actividades',
    descripcion: 'Agenda, operaciones, inscripciones y reportes.',
    icono: '@tui.calendar',
    color: 'primary',
    hijos: [
      { titulo: 'Agenda de actividades', descripcion: 'Listado y gestión de actividades.', icono: '@tui.calendar', ruta: '/actividades' },
      { titulo: 'Nueva actividad', descripcion: 'Programar nueva actividad institucional.', icono: '@tui.plus', ruta: '/actividades/nueva' },
      { titulo: 'Reporte por periodo', descripcion: 'Consolidado de actividades por fechas.', icono: '@tui.chart-column', ruta: '/actividades/reportes' },
    ],
  },
  {
    id: 'alertas',
    titulo: 'Alertas clínicas',
    descripcion: 'Bandeja, tablero y reglas clínicas.',
    icono: '@tui.bell',
    color: 'error',
    imagen: IMG_ALERTAS,
    hijos: [
      { titulo: 'Alertas', descripcion: 'Alertas detectadas por las reglas.', icono: '@tui.bell', ruta: '/alertaclinica/alertas', destacado: true, imagen: IMG_ALERTAS },
      { titulo: 'Dashboard clínico', descripcion: 'Tablero de riesgo clínico.', icono: '@tui.heart-pulse', ruta: '/alertaclinica/dashboard', destacado: true, imagen: IMG_DASHBOARD_CLINICO },
      { titulo: 'Etiquetas clínicas', descripcion: 'Clasificación, parámetros y alertas asociadas.', icono: '@tui.tags', ruta: '/alertaclinica/etiquetas', destacado: true },
      { titulo: 'Parámetros clínicos', descripcion: 'Consulta de parámetros y sus etiquetas.', icono: '@tui.activity', ruta: '/alertaclinica/parametros' },
      { titulo: 'Reporte de etiquetas', descripcion: 'Asignaciones, detalle y alertas únicas.', icono: '@tui.chart-column', ruta: '/alertaclinica/reportes/etiquetas' },
      { titulo: 'Regla: Perímetro abdominal', descripcion: 'Umbrales de perímetro abdominal.', icono: '@tui.ruler', ruta: '/alertaclinica/reglas/perimetro-abdominal', imagen: imagenRegla('perimetro-abdominal') },
      { titulo: 'Regla: Porcentaje de grasa', descripcion: 'Umbrales de grasa corporal.', icono: '@tui.percent', ruta: '/alertaclinica/reglas/porcentaje-grasa', imagen: imagenRegla('porcentaje-grasa') },
      { titulo: 'Regla: Masa muscular', descripcion: 'Umbrales de masa muscular.', icono: '@tui.biceps-flexed', ruta: '/alertaclinica/reglas/masa-muscular', imagen: imagenRegla('masa-muscular') },
      { titulo: 'Regla: IMC', descripcion: 'Umbrales de índice de masa corporal.', icono: '@tui.scale', ruta: '/alertaclinica/reglas/imc', imagen: imagenRegla('imc') },
      { titulo: 'Regla: Grasa visceral', descripcion: 'Umbrales de grasa visceral.', icono: '@tui.circle-dot', ruta: '/alertaclinica/reglas/grasa-visceral', imagen: imagenRegla('grasa-visceral') },
      { titulo: 'Regla: Glucosa', descripcion: 'Umbrales de glucosa.', icono: '@tui.droplet', ruta: '/alertaclinica/reglas/glucosa', imagen: imagenRegla('glucosa') },
      { titulo: 'Regla: Colesterol', descripcion: 'Umbrales de colesterol.', icono: '@tui.flask-conical', ruta: '/alertaclinica/reglas/colesterol', imagen: imagenRegla('colesterol') },
      { titulo: 'Regla: Triglicéridos', descripcion: 'Umbrales de triglicéridos.', icono: '@tui.test-tube', ruta: '/alertaclinica/reglas/trigliceridos', imagen: imagenRegla('trigliceridos') },
      { titulo: 'Regla: Presión arterial', descripcion: 'Umbrales de presión arterial.', icono: '@tui.heart-pulse', ruta: '/alertaclinica/reglas/presion-arterial', imagen: imagenRegla('presion-arterial') },
    ],
  },
  {
    id: 'asistencias',
    titulo: 'Asistencias',
    descripcion: 'Registro, cuarentena y justificaciones.',
    icono: '@tui.clipboard-check',
    color: 'warning',
    hijos: [
      { titulo: 'Registro', descripcion: 'Asistencias por actividad y cierre.', icono: '@tui.clipboard-check', ruta: '/asistencias/registro' },
      { titulo: 'Cuarentena', descripcion: 'Registros retenidos por revisar.', icono: '@tui.shield-alert', ruta: '/asistencias/cuarentena' },
      { titulo: 'Exoneraciones', descripcion: 'Justificaciones y dictámenes.', icono: '@tui.file-check', ruta: '/asistencias/exoneraciones' },
      { titulo: 'Sincronización', descripcion: 'Lotes offline masivos.', icono: '@tui.send', ruta: '/asistencias/sincronizacion' },
      { titulo: 'Dashboard asistencias', descripcion: 'Participación y ausentismo.', icono: '@tui.chart-column', ruta: '/asistencias/dashboard' },
    ],
  },
  {
    id: 'metas',
    titulo: 'Metas',
    descripcion: 'Metas personales, clínicas y check-ins.',
    icono: '@tui.target',
    color: 'secondary',
    hijos: [
      { titulo: 'Metas', descripcion: 'Metas por persona, progreso y check-ins.', icono: '@tui.target', ruta: '/metas' },
      { titulo: 'Nueva meta', descripcion: 'Personal, clínica o sugerida.', icono: '@tui.plus', ruta: '/metas/nueva' },
    ],
  },
  {
    id: 'noticias',
    titulo: 'Noticias',
    descripcion: 'Feed público y administración.',
    icono: '@tui.newspaper',
    color: 'primary',
    hijos: [
      { titulo: 'Feed', descripcion: 'Noticias publicadas.', icono: '@tui.newspaper', ruta: '/noticias' },
      { titulo: 'Administrar', descripcion: 'Borradores, publicar y archivar.', icono: '@tui.pencil', ruta: '/noticias/admin' },
      { titulo: 'Categorías', descripcion: 'Taxonomía del feed.', icono: '@tui.tags', ruta: '/noticias/categorias' },
    ],
  },
  {
    id: 'seguridad',
    titulo: 'Seguridad y cuenta',
    descripcion: 'Usuarios de terceros y tu propio perfil.',
    icono: '@tui.shield',
    color: 'warning',
    hijos: [
      { titulo: 'Usuarios', descripcion: 'Cuentas, roles y estados (terceros).', icono: '@tui.shield', ruta: '/seguridad/usuarios' },
      { titulo: 'Nuevo usuario', descripcion: 'Crear cuenta de acceso.', icono: '@tui.user-plus', ruta: '/seguridad/usuarios/nuevo' },
      { titulo: 'Mi perfil', descripcion: 'Tus datos personales (vista propia).', icono: '@tui.user', ruta: '/mi-perfil' },
    ],
  },
  {
    id: 'notificaciones',
    titulo: 'Notificaciones',
    descripcion: 'Bandeja, envíos y plantillas de mensajes.',
    icono: '@tui.bell',
    color: 'violet',
    hijos: [
      { titulo: 'Todas', descripcion: 'Listado completo de notificaciones enviadas.', icono: '@tui.bell', ruta: '/notificaciones' },
      { titulo: 'Bandeja', descripcion: 'Bandeja de revisión del administrador.', icono: '@tui.inbox', ruta: '/notificaciones/bandeja' },
      { titulo: 'Plantillas', descripcion: 'Plantillas de mensajes por evento y canal.', icono: '@tui.mail', ruta: '/notificaciones/plantillas' },
    ],
  },
  {
    id: 'catalogos',
    titulo: 'Catálogos',
    descripcion: 'Áreas de trabajo y periodos.',
    icono: '@tui.grid',
    color: 'success',
    hijos: [
      { titulo: 'Áreas', descripcion: 'Catálogo de áreas de trabajo.', icono: '@tui.grid', ruta: '/catalogos/areas' },
      { titulo: 'Periodos', descripcion: 'Periodos de evaluación.', icono: '@tui.calendar', ruta: '/catalogo/periodos' },
    ],
  },
];

export function buscarModulo(id: string | null): AdminModuloDef | undefined {
  if (!id) return undefined;
  return ADMIN_MODULOS.find((m) => m.id === id);
}
