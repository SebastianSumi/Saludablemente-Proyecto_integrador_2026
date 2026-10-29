import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';

interface Modulo {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
}

@Component({
  imports: [RouterLink, TuiButton, TuiIcon],
  selector: 'app-admin-panel',
  styleUrl: './admin-panel.css',
  templateUrl: './admin-panel.html',
})
export class AdminPanel {
  protected readonly secciones: { titulo: string; modulos: Modulo[] }[] = [
    {
      titulo: 'Personal',
      modulos: [
        {
          titulo: 'Personas',
          descripcion: 'CRUD completo, filtros, QR y preferencias.',
          icono: '@tui.users',
          ruta: '/personal/personas',
        },
        {
          titulo: 'Equipos',
          descripcion: 'Gestión de equipos de trabajo.',
          icono: '@tui.user-check',
          ruta: '/teams',
        },
        {
          titulo: 'Reportes de personal',
          descripcion: 'Resumen agregado del personal.',
          icono: '@tui.clipboard-list',
          ruta: '/personal/reportes',
        },
      ],
    },
    {
      titulo: 'Catálogos',
      modulos: [
        {
          titulo: 'Áreas de trabajo',
          descripcion: 'Catálogo de áreas.',
          icono: '@tui.grid',
          ruta: '/catalogos/areas',
        },
        {
          titulo: 'Periodos',
          descripcion: 'Periodos de evaluación.',
          icono: '@tui.calendar',
          ruta: '/catalogo/periodos',
        },
      ],
    },
    {
      titulo: 'Nutricional',
      modulos: [
        {
          titulo: 'Evaluaciones',
          descripcion: 'Evaluaciones, antropometría y bioquímica.',
          icono: '@tui.apple',
          ruta: '/nutricional/evaluaciones',
        },
        {
          titulo: 'Importar bioquímico',
          descripcion: 'Carga masiva desde Excel.',
          icono: '@tui.file-text',
          ruta: '/nutricional/importar-bioquimico',
        },
        {
          titulo: 'Dashboard nutricional',
          descripcion: 'Indicadores y gráficos nutricionales.',
          icono: '@tui.layout-dashboard',
          ruta: '/nutricional/dashboard',
        },
      ],
    },
    {
      titulo: 'Aptitud física',
      modulos: [
        {
          titulo: 'Evaluaciones',
          descripcion: 'Evaluaciones de aptitud física.',
          icono: '@tui.activity',
          ruta: '/aptitud',
        },
      ],
    },
    {
      titulo: 'Alertas clínicas',
      modulos: [
        {
          titulo: 'Alertas',
          descripcion: 'Alertas detectadas por las reglas.',
          icono: '@tui.bell',
          ruta: '/alertaclinica/alertas',
        },
        {
          titulo: 'Dashboard clínico',
          descripcion: 'Tablero de alertas clínicas.',
          icono: '@tui.heart-pulse',
          ruta: '/alertaclinica/dashboard',
        },
        {
          titulo: 'Reglas',
          descripcion: 'Reglas nutricionales y clínicas.',
          icono: '@tui.settings',
          ruta: '/alertaclinica/reglas/imc',
        },
      ],
    },
    {
      titulo: 'Asistencias',
      modulos: [
        {
          titulo: 'Registro',
          descripcion: 'Asistencias por actividad y cierre.',
          icono: '@tui.clipboard-list',
          ruta: '/asistencias/registro',
        },
        {
          titulo: 'Cuarentena',
          descripcion: 'Bandeja de registros retenidos.',
          icono: '@tui.shield-alert',
          ruta: '/asistencias/cuarentena',
        },
        {
          titulo: 'Exoneraciones',
          descripcion: 'Justificaciones y dictámenes.',
          icono: '@tui.file-check',
          ruta: '/asistencias/exoneraciones',
        },
        {
          titulo: 'Sincronización',
          descripcion: 'Lotes offline masivos.',
          icono: '@tui.send',
          ruta: '/asistencias/sincronizacion',
        },
        {
          titulo: 'Dashboard asistencias',
          descripcion: 'Participación y ausentismo.',
          icono: '@tui.layout-dashboard',
          ruta: '/asistencias/dashboard',
        },
      ],
    },
    {
      titulo: 'Actividades',
      modulos: [
        {
          titulo: 'Agenda',
          descripcion: 'Actividades, operaciones e inscripciones.',
          icono: '@tui.calendar',
          ruta: '/actividades',
        },
        {
          titulo: 'Nueva actividad',
          descripcion: 'Crear actividad programada.',
          icono: '@tui.plus',
          ruta: '/actividades/nueva',
        },
        {
          titulo: 'Reporte agenda',
          descripcion: 'Por periodo y fechas.',
          icono: '@tui.clipboard-list',
          ruta: '/actividades/reportes',
        },
      ],
    },
    {
      titulo: 'Seguridad',
      modulos: [
        {
          titulo: 'Usuarios',
          descripcion: 'Cuentas, roles y estados.',
          icono: '@tui.shield',
          ruta: '/seguridad/usuarios',
        },
        {
          titulo: 'Nuevo usuario',
          descripcion: 'Cuenta vinculada a RRHH.',
          icono: '@tui.user-check',
          ruta: '/seguridad/usuarios/nuevo',
        },
      ],
    },
    {
      titulo: 'Metas',
      modulos: [
        {
          titulo: 'Metas',
          descripcion: 'Metas por persona, progreso y check-ins.',
          icono: '@tui.home',
          ruta: '/metas',
        },
        {
          titulo: 'Nueva meta',
          descripcion: 'Personal, clínica o sugerida.',
          icono: '@tui.plus',
          ruta: '/metas/nueva',
        },
      ],
    },
    {
      titulo: 'Noticias',
      modulos: [
        {
          titulo: 'Feed',
          descripcion: 'Noticias publicadas.',
          icono: '@tui.file-text',
          ruta: '/noticias',
        },
        {
          titulo: 'Administrar',
          descripcion: 'Borradores, publicar y archivar.',
          icono: '@tui.pencil',
          ruta: '/noticias/admin',
        },
        {
          titulo: 'Categorías',
          descripcion: 'Categorías del feed.',
          icono: '@tui.grid',
          ruta: '/noticias/categorias',
        },
      ],
    },
    {
      titulo: 'Colaborador',
      modulos: [
        {
          titulo: 'Mi panel',
          descripcion: 'Vista del colaborador.',
          icono: '@tui.home',
          ruta: '/colaborador',
        },
        {
          titulo: 'Mis notificaciones',
          descripcion: 'Solo tus notificaciones.',
          icono: '@tui.bell',
          ruta: '/colaborador/mis-notificaciones',
        },
        {
          titulo: 'Mi perfil ligero',
          descripcion: 'Perfil sin datos sensibles.',
          icono: '@tui.user',
          ruta: '/colaborador/mi-perfil',
        },
        {
          titulo: 'Mis evaluaciones',
          descripcion: 'Solo evaluaciones propias.',
          icono: '@tui.stethoscope',
          ruta: '/colaborador/mis-evaluaciones',
        },
        {
          titulo: 'Mis alertas',
          descripcion: 'Solo alertas propias.',
          icono: '@tui.bell',
          ruta: '/colaborador/mis-alertas',
        },
      ],
    },
  ];
}
