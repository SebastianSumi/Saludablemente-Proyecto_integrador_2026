import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Avatar, urlAvatarFoto } from '../../../shared/components/avatar/avatar';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import { EvaluacionService as EvalNutricionalService } from '../../admin/nutricional/evaluacion/evaluacion-service';
import { AptitudService } from '../../admin/aptitud/aptitud-service';
import { AlertaClinicaService } from '../../admin/alertaclinica/alerta/alerta-clinica-service';
import { MetaService } from '../../admin/metas/meta-service';
import type { Persona } from '../../admin/personal/persona/persona.model';
import type { EvaluacionResumen as NutResumen } from '../../admin/nutricional/evaluacion/evaluacion.model';
import type { EvaluacionResumen as AptResumen } from '../../admin/aptitud/models';
import type { AlertaClinica, SeveridadAlerta } from '../../admin/alertaclinica/alerta/alerta-clinica.model';
import type { Meta } from '../../admin/metas/models';
import { calcularPorcentajeMeta } from '../inicio/calcular-meta';

@Component({
  selector: 'app-eval-colaborador-detalle',
  standalone: true,
  imports: [DatePipe, RouterLink, TuiButton, TuiIcon, Avatar],
  templateUrl: './eval-colaborador-detalle.html',
  styleUrls: ['../evaluador-comun.css'],
})
export class EvalColaboradorDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly personaSrv = inject(PersonaService);
  private readonly nutSrv = inject(EvalNutricionalService);
  private readonly aptSrv = inject(AptitudService);
  private readonly alertaSrv = inject(AlertaClinicaService);
  private readonly metaSrv = inject(MetaService);

  protected readonly id = signal<number>(0);
  protected readonly persona = signal<Persona | null>(null);
  protected readonly evaluacionesNut = signal<NutResumen[]>([]);
  protected readonly evaluacionesApt = signal<AptResumen[]>([]);
  protected readonly alertas = signal<AlertaClinica[]>([]);
  protected readonly metas = signal<Meta[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const idParam = Number(this.route.snapshot.paramMap.get('id'));
    this.id.set(idParam);
    this.cargar(idParam);
  }

  protected nombreCompleto(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  protected iniciales(p: Persona): string {
    return this.nombreCompleto(p)
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected fotoDe(p: Persona): string | undefined {
    return urlAvatarFoto(p.avatarUrl);
  }

  protected porcentajeMeta(m: Meta): number {
    return calcularPorcentajeMeta(m);
  }

  protected pillSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-secondary';
  }

  private cargar(idPersona: number): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      persona: this.personaSrv.obtener(idPersona).pipe(catchError(() => of(null))),
      nut: this.nutSrv
        .historialPorPersona(idPersona)
        .pipe(catchError(() => of([] as NutResumen[]))),
      apt: this.aptSrv.historial(idPersona).pipe(catchError(() => of([] as AptResumen[]))),
      alertas: this.alertaSrv
        .listar({ personaId: idPersona })
        .pipe(catchError(() => of([] as AlertaClinica[]))),
      metas: this.metaSrv
        .listarPorPersona(idPersona)
        .pipe(catchError(() => of([] as Meta[]))),
    }).subscribe({
      next: ({ persona, nut, apt, alertas, metas }) => {
        if (!persona) {
          this.error.set('No se encontró el colaborador solicitado.');
          this.loading.set(false);
          return;
        }
        this.persona.set(persona);
        this.evaluacionesNut.set(nut);
        this.evaluacionesApt.set(apt);
        this.alertas.set(
          alertas
            .slice()
            .sort((a, b) => b.fechaGeneracion.localeCompare(a.fechaGeneracion))
        );
        this.metas.set(metas);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la ficha del colaborador.');
        this.loading.set(false);
      },
    });
  }
}
