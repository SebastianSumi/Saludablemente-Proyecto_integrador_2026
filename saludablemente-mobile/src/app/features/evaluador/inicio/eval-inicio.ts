import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { UsuarioPerfil } from '../../admin/perfil/perfil.model';
import { EvaluacionService as EvalNutricionalService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { AptitudService } from '../../admin/aptitud/aptitud-service';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import { PeriodoService } from '../../admin/catalogo/periodo/periodo-service';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import type { AlertaClinica, SeveridadAlerta } from '../../admin/alertaclinica/alerta/alerta-clinica.model';
import type {
  EvaluacionAgregado,
  EvaluacionResumen as NutResumen,
} from '../../admin/nutricional/evaluacion/evaluacion.model';
import type { EvaluacionResumen as AptResumen } from '../../admin/aptitud/models';
import { RevealDirective } from '../../../shared/animations/reveal.directive';

export interface EvalKpis {
  nutricionales: number;
  aptitud: number;
  alertasPendientes: number;
  alertasCriticas: number;
  colaboradores: number;
}

export interface EvalModulo {
  titulo: string;
  descripcion: string;
  icono: string;
  ruta: string;
  color: string;
}

const MODULOS: EvalModulo[] = [
  {
    titulo: 'Colaboradores',
    descripcion: 'Busca personas y revisa su historial',
    icono: '@tui.users',
    ruta: '/evaluador/colaboradores',
    color: 'primary',
  },
  {
    titulo: 'Evaluación nutricional',
    descripcion: 'Antropometría, bioquímica y diagnóstico',
    icono: '@tui.apple',
    ruta: '/evaluador/nutricional',
    color: 'success',
  },
  {
    titulo: 'Aptitud física',
    descripcion: 'Fuerza, cardio y dictamen global',
    icono: '@tui.activity',
    ruta: '/evaluador/aptitud',
    color: 'secondary',
  },
  {
    titulo: 'Alertas clínicas',
    descripcion: 'Revisa y atiende alertas pendientes',
    icono: '@tui.bell',
    ruta: '/evaluador/alertas',
    color: 'error',
  },
  {
    titulo: 'Metas clínicas',
    descripcion: 'Prescribe objetivos y sigue su avance',
    icono: '@tui.target',
    ruta: '/evaluador/metas',
    color: 'violet',
  },
  {
    titulo: 'Reportes',
    descripcion: 'Fichas clínicas PDF y consolidados',
    icono: '@tui.file-text',
    ruta: '/evaluador/reportes',
    color: 'warning',
  },
];

@Component({
  selector: 'app-eval-inicio',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, DatePipe, RevealDirective],
  templateUrl: './eval-inicio.html',
  styleUrls: ['./eval-inicio.css'],
})
export class EvalInicio implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly nutSrv = inject(EvalNutricionalService);
  private readonly aptSrv = inject(AptitudService);
  private readonly alertaSrv = inject(AlertaClinicaService);
  private readonly periodoSrv = inject(PeriodoService);
  private readonly personaSrv = inject(PersonaService);

  protected readonly modulos = MODULOS;

  protected readonly perfil = signal<UsuarioPerfil | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly periodoCodigo = signal<string>('');

  protected readonly kpis = signal<EvalKpis>({
    nutricionales: 0,
    aptitud: 0,
    alertasPendientes: 0,
    alertasCriticas: 0,
    colaboradores: 0,
  });

  protected readonly alertas = signal<AlertaClinica[]>([]);
  protected readonly nutricionalesPendientes = signal<NutResumen[]>([]);
  protected readonly aptitudPendientes = signal<AptResumen[]>([]);

  ngOnInit(): void {
    this.perfilSrv.obtener().subscribe({
      next: (p) => this.perfil.set(p),
      error: () => undefined,
    });
    this.cargar();
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
      ? 'tile tile-circle tile-error'
      : sev === 'MODERADA'
        ? 'tile tile-circle tile-warning'
        : 'tile tile-circle tile-secondary';
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      periodo: this.periodoSrv
        .obtenerVigente()
        .pipe(catchError(() => of(null))),
      nut: this.nutSrv
        .agregados()
        .pipe(catchError(() => of(null as EvaluacionAgregado | null))),
      apt: this.aptSrv
        .buscar({ page: 0, size: 1 })
        .pipe(catchError(() => of(null))),
      alertas: this.alertaSrv
        .listar({ estado: 'PENDIENTE' })
        .pipe(catchError(() => of([] as AlertaClinica[]))),
      personas: this.personaSrv
        .listar({ estado: true })
        .pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ periodo, nut, apt, alertas, personas }) => {
        this.periodoCodigo.set(periodo?.codigo ?? '');

        this.kpis.set({
          nutricionales: nut?.total ?? 0,
          aptitud: apt?.totalElements ?? 0,
          alertasPendientes: alertas.length,
          alertasCriticas: alertas.filter((a) => a.nivelSeveridad === 'CRITICA').length,
          colaboradores: personas.length,
        });

        const pendientes = alertas
          .slice()
          .sort((a, b) => b.fechaGeneracion.localeCompare(a.fechaGeneracion));
        this.alertas.set(pendientes.slice(0, 5));

        this.cargarPendientes(periodo?.codigo ?? '');
      },
      error: () => {
        this.error.set('No se pudo cargar el panel del evaluador.');
        this.loading.set(false);
      },
    });
  }

  private cargarPendientes(codigoPeriodo: string): void {
    forkJoin({
      nut: codigoPeriodo
        ? this.nutSrv
            .incompletas(codigoPeriodo)
            .pipe(catchError(() => of([] as NutResumen[])))
        : of([] as NutResumen[]),
      apt: this.aptSrv
        .buscar({ estado: 'PENDIENTE', page: 0, size: 5 })
        .pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ nut, apt }) => {
        this.nutricionalesPendientes.set(nut.slice(0, 5));
        this.aptitudPendientes.set(apt?.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
