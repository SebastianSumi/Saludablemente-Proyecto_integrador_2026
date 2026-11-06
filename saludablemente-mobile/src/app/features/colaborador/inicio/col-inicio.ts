import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { UsuarioPerfil } from '../../admin/perfil/perfil.model';
import { EvaluacionService as EvalNutricionalService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { EvaluacionService as EvalAptitudService } from '../../admin/aptitud/evaluacion/evaluacion-service';
import type { EvaluacionResumen as EvalNutricionalResumen } from '../../admin/nutricional/evaluacion/evaluacion.model';
import type { EvaluacionResumen as EvalAptitudResumen } from '../../admin/aptitud/evaluacion/evaluacion.model';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import type {
  AlertaClinica,
  SeveridadAlerta,
} from '../../admin/alertaclinica/alerta/alerta-clinica.model';
import { MetaService } from '../../admin/metas/meta-service';
import type { Meta } from '../../admin/metas/models';
import { AsistenciaService } from '../../admin/asistencias/asistencia-service';
import { AgendaService } from '../../admin/actividades/agenda-service';
import type { Actividad } from '../../admin/actividades/models';
import { NotificacionService } from '../notificaciones/notificacion-service';
import { NoticiaPublicaService } from '../../admin/noticias/noticia-service';
import type { NoticiaFeed } from '../../admin/noticias/models';
import { RevealDirective } from '../../../shared/animations/reveal.directive';

export interface ColKpis {
  evaluaciones: number;
  alertasPendientes: number;
  alertasCriticas: number;
  metasActivas: number;
  asistencias: number;
  noLeidas: number;
}

export interface EvaluacionItem {
  key: string;
  tipo: 'NUTRICIONAL' | 'APTITUD';
  fecha: string;
  estado: string;
  detalle: string;
  ruta: (string | number)[];
}

export interface MetaItem {
  id: number;
  descripcion: string;
  porcentaje: number;
}

export interface ColModulo {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
  color: string;
}

export const COL_MODULOS: ColModulo[] = [
  {
    titulo: 'Mis evaluaciones',
    descripcion: 'Historial nutricional y de aptitud física',
    icono: '@tui.activity',
    ruta: '/colaborador/mis-evaluaciones',
    color: 'primary',
  },
  {
    titulo: 'Mis alertas',
    descripcion: 'Alertas de salud pendientes y atendidas',
    icono: '@tui.bell',
    ruta: '/colaborador/mis-alertas',
    color: 'error',
  },
  {
    titulo: 'Mis metas',
    descripcion: 'Progreso, check-ins y objetivos',
    icono: '@tui.target',
    ruta: '/colaborador/mis-metas',
    color: 'success',
  },
  {
    titulo: 'Mis asistencias',
    descripcion: 'Tu historial de marcaciones',
    icono: '@tui.clipboard-check',
    ruta: '/colaborador/mis-asistencias',
    color: 'warning',
  },
  {
    titulo: 'Notificaciones',
    descripcion: 'Avisos del sistema para ti',
    icono: '@tui.inbox',
    ruta: '/colaborador/mis-notificaciones',
    color: 'violet',
  },
  {
    titulo: 'Noticias',
    descripcion: 'Comunicados institucionales',
    icono: '@tui.newspaper',
    ruta: '/colaborador/noticias',
    color: 'secondary',
  },
  {
    titulo: 'Mi perfil',
    descripcion: 'Tus datos, área y roles',
    icono: '@tui.user',
    ruta: '/colaborador/mi-perfil',
    color: 'secondary',
  },
];

/** Avance de una meta sobre su rango inicial→objetivo, acotado a [0, 100]. */
export function calcularPorcentajeMeta(meta: {
  valorInicial: number;
  valorActual: number;
  valorObjetivo: number;
}): number {
  const rango = meta.valorObjetivo - meta.valorInicial;
  if (rango === 0) return meta.valorActual >= meta.valorObjetivo ? 100 : 0;
  const pct = ((meta.valorActual - meta.valorInicial) / rango) * 100;
  return Math.max(0, Math.min(100, Math.round(pct)));
}

function dictamenTexto(dictamen: string): string {
  switch (dictamen) {
    case 'APTO':
      return 'Apto';
    case 'APTO_CON_RESTRICCIONES':
      return 'Apto con restricciones';
    case 'NO_APTO':
      return 'No apto';
    default:
      return dictamen;
  }
}

@Component({
  selector: 'app-col-inicio',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, DatePipe, RevealDirective],
  templateUrl: './col-inicio.html',
  styleUrls: ['./col-inicio.css'],
})
export class ColInicio implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly nutSrv = inject(EvalNutricionalService);
  private readonly aptSrv = inject(EvalAptitudService);
  private readonly alertaSrv = inject(AlertaClinicaService);
  private readonly metaSrv = inject(MetaService);
  private readonly asisSrv = inject(AsistenciaService);
  private readonly agendaSrv = inject(AgendaService);
  private readonly notiSrv = inject(NotificacionService);
  private readonly feedSrv = inject(NoticiaPublicaService);

  protected readonly modulos = COL_MODULOS;

  protected readonly perfil = signal<UsuarioPerfil | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly kpis = signal<ColKpis>({
    evaluaciones: 0,
    alertasPendientes: 0,
    alertasCriticas: 0,
    metasActivas: 0,
    asistencias: 0,
    noLeidas: 0,
  });

  protected readonly evaluaciones = signal<EvaluacionItem[]>([]);
  protected readonly alertas = signal<AlertaClinica[]>([]);
  protected readonly metas = signal<MetaItem[]>([]);
  protected readonly actividadesHoy = signal<Actividad[]>([]);
  protected readonly noticias = signal<NoticiaFeed[]>([]);

  ngOnInit(): void {
    this.perfilSrv.obtener().subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.cargar(p.idPersona);
      },
      error: () => {
        this.error.set('No se pudo cargar tu perfil.');
        this.loading.set(false);
      },
    });
  }

  protected iniciales(nombre?: string): string {
    if (!nombre) return '?';
    return nombre
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected fechaHoy(): string {
    return new Date().toLocaleDateString('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    });
  }

  protected pillSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-secondary';
  }

  protected tileSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'tile tile-circle tile-soft-error'
      : sev === 'MODERADA'
        ? 'tile tile-circle tile-soft-warning'
        : 'tile tile-circle tile-soft-secondary';
  }

  protected tileEvaluacion(tipo: EvaluacionItem['tipo']): string {
    return tipo === 'NUTRICIONAL'
      ? 'tile tile-circle tile-soft-primary'
      : 'tile tile-circle tile-soft-secondary';
  }

  protected iconoEvaluacion(tipo: EvaluacionItem['tipo']): string {
    return tipo === 'NUTRICIONAL' ? '@tui.heart-pulse' : '@tui.activity';
  }

  protected etiquetaEvaluacion(tipo: EvaluacionItem['tipo']): string {
    return tipo === 'NUTRICIONAL' ? 'Nutricional' : 'Aptitud física';
  }

  protected estadoEvaluacionTexto(estado: string): string {
    switch (estado) {
      case 'COMPLETA':
        return 'Completa';
      case 'PENDIENTE':
        return 'Pendiente';
      case 'ANULADA':
        return 'Anulada';
      default:
        return estado;
    }
  }

  protected pillEstadoEvaluacion(estado: string): string {
    switch (estado) {
      case 'COMPLETA':
        return 'pill pill-success';
      case 'PENDIENTE':
        return 'pill pill-warning';
      default:
        return 'pill pill-neutral';
    }
  }

  private cargar(idPersona: number): void {
    forkJoin({
      nut: this.nutSrv
        .historialPorPersona(idPersona)
        .pipe(catchError(() => of([] as EvalNutricionalResumen[]))),
      apt: this.aptSrv
        .historial(idPersona)
        .pipe(catchError(() => of([] as EvalAptitudResumen[]))),
      alertas: this.alertaSrv
        .listar({ personaId: idPersona })
        .pipe(catchError(() => of([] as AlertaClinica[]))),
      metas: this.metaSrv
        .listarPorPersona(idPersona)
        .pipe(catchError(() => of([] as Meta[]))),
      asis: this.asisSrv.historialPorPersona(idPersona).pipe(catchError(() => of([]))),
      hoy: this.agendaSrv.deHoy().pipe(catchError(() => of([] as Actividad[]))),
      noLeidas: this.notiSrv.contadorNoLeidas(idPersona).pipe(catchError(() => of(0))),
      feed: this.feedSrv
        .feed({ idPersona, size: 5 })
        .pipe(catchError(() => of({ content: [] as NoticiaFeed[] }))),
    }).subscribe(({ nut, apt, alertas, metas, asis, hoy, noLeidas, feed }) => {
      const pendientes = alertas
        .filter((a) => a.estado === 'PENDIENTE')
        .sort((a, b) => b.fechaGeneracion.localeCompare(a.fechaGeneracion));

      const metasActivas = metas.filter((m) => m.estado === 'ACTIVA');

      this.kpis.set({
        evaluaciones: nut.length + apt.length,
        alertasPendientes: pendientes.length,
        alertasCriticas: pendientes.filter((a) => a.nivelSeveridad === 'CRITICA').length,
        metasActivas: metasActivas.length,
        asistencias: asis.length,
        noLeidas: noLeidas ?? 0,
      });

      this.evaluaciones.set(this.unirEvaluaciones(nut, apt));
      this.alertas.set(pendientes.slice(0, 5));
      this.metas.set(
        metasActivas.slice(0, 4).map((m) => ({
          id: m.id,
          descripcion: m.descripcion,
          porcentaje: calcularPorcentajeMeta(m),
        })),
      );
      this.actividadesHoy.set(hoy);
      this.noticias.set(feed.content ?? []);
      this.loading.set(false);
    });
  }

  private unirEvaluaciones(
    nut: EvalNutricionalResumen[],
    apt: EvalAptitudResumen[],
  ): EvaluacionItem[] {
    const items: EvaluacionItem[] = [
      ...nut.map((n) => ({
        key: `NUTRICIONAL-${n.idEvaluacion}`,
        tipo: 'NUTRICIONAL' as const,
        fecha: n.fechaEvaluacion,
        estado: n.estadoEvaluacion,
        detalle: n.areaTrabajoAlMomento || 'Control nutricional',
        ruta: ['/colaborador/evaluaciones/nutricional', n.idEvaluacion],
      })),
      ...apt.map((a) => ({
        key: `APTITUD-${a.id}`,
        tipo: 'APTITUD' as const,
        fecha: a.fechaEvaluacion,
        estado: a.estadoEvaluacion ?? 'COMPLETA',
        detalle: a.dictamenGlobal
          ? dictamenTexto(a.dictamenGlobal)
          : a.areaTrabajoAlMomento || 'Aptitud física',
        ruta: ['/colaborador/evaluaciones/aptitud', a.id],
      })),
    ];
    return items
      .sort((a, b) => b.fecha.localeCompare(a.fecha))
      .slice(0, 5);
  }
}
