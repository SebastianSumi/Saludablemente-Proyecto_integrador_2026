import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiTabs } from '@taiga-ui/kit';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { EvaluacionService as NutSrv } from '../../admin/nutricional/evaluacion/evaluacion-service';
import type { EvaluacionResponse as NutDetail } from '../../admin/nutricional/evaluacion/evaluacion.model';
import { EvaluacionService as AptSrv } from '../../admin/aptitud/evaluacion/evaluacion-service';
import type { Evaluacion as AptDetail } from '../../admin/aptitud/evaluacion/evaluacion.model';
import { map, switchMap } from 'rxjs/operators';

export type NivelDx = 'ok' | 'warn' | 'danger' | 'neutro';

@Component({
  selector: 'app-col-evaluacion-detail',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiTabs, DatePipe],
  templateUrl: './evaluacion-detail.html',
  styleUrls: ['./evaluacion-detail.css'],
})
export class ColEvaluacionDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly perfilSrv = inject(PerfilService);
  private readonly nutSrv = inject(NutSrv);
  private readonly aptSrv = inject(AptSrv);

  protected readonly tipo = signal<'nutricional' | 'aptitud'>('nutricional');
  protected readonly tab = signal(0);
  protected readonly nut = signal<NutDetail | null>(null);
  protected readonly apt = signal<AptDetail | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const esApt = this.router.url.includes('/aptitud/');
    this.tipo.set(esApt ? 'aptitud' : 'nutricional');
    this.tab.set(0);
    if (!Number.isFinite(id) || id <= 0) {
      this.error.set('Identificador inválido.');
      return;
    }
    this.loading.set(true);
    let idPersonaPropia = 0;
    this.perfilSrv
      .obtener()
      .pipe(
        switchMap((p) => {
          idPersonaPropia = p.idPersona;
          return esApt ? this.aptSrv.obtener(id) : this.nutSrv.obtener(id);
        }),
        map((data: unknown) => {
          const idPersona = (data as { idPersona?: number }).idPersona;
          if (idPersona !== undefined && idPersona !== idPersonaPropia) {
            throw new Error('Esta evaluación no pertenece a tu persona.');
          }
          return data;
        }),
      )
      .subscribe({
        next: (data) => {
          if (esApt) this.apt.set(data as AptDetail);
          else this.nut.set(data as NutDetail);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse | Error) => {
          this.error.set(
            err instanceof Error && !(err instanceof HttpErrorResponse)
              ? err.message
              : (err as HttpErrorResponse).status === 0
                ? 'No se pudo conectar con el servidor.'
                : 'No se pudo cargar tu evaluación.',
          );
          this.loading.set(false);
        },
      });
  }

  // -------------------------------------------------------------------
  // Semáforo de diagnósticos
  // -------------------------------------------------------------------

  protected nivelDx(dx?: string | null): NivelDx {
    if (!dx) return 'neutro';
    const v = dx.toUpperCase();
    if (/(MUY_ALTO|RIESGO_ALTO|OBESIDAD|ALTERADO|NO_APTO|BAJO|MALO)/.test(v)) {
      return 'danger';
    }
    if (
      /(ALTO|RIESGO|SOBREPESO|REGULAR|PROMEDIO|RESTRICCIONES|DESVIADA)/.test(v) &&
      !/NORMAL/.test(v)
    ) {
      return 'warn';
    }
    if (/(NORMAL|BUENO|EXCELENTE|APTO|OPTIMO|ÓPTIMO)/.test(v)) {
      return 'ok';
    }
    return 'neutro';
  }

  protected claseDx(dx?: string | null): string {
    switch (this.nivelDx(dx)) {
      case 'ok':
        return 'pill-success';
      case 'warn':
        return 'pill-warning';
      case 'danger':
        return 'pill-error';
      default:
        return 'pill-neutral';
    }
  }

  protected etiquetaDx(dx?: string | null): string {
    if (!dx) return 'Sin dato';
    return dx
      .toLowerCase()
      .split(/[_\s]+/)
      .map((w) => (w ? w.charAt(0).toUpperCase() + w.slice(1) : w))
      .join(' ');
  }

  /** Peor nivel entre los diagnósticos nutricionales. */
  protected readonly semaforoNut = computed<NivelDx>(() => {
    const n = this.nut();
    const dxs = [
      n?.detalleAntropometrico?.dxImc,
      n?.detalleAntropometrico?.dxPerimetroAbdominal,
      n?.detalleAntropometrico?.dxGrasa,
      n?.detalleAntropometrico?.dxMasaMuscular,
      n?.detalleAntropometrico?.dxGrasaVisceral,
      n?.detalleBioquimico?.dxBioquimico,
    ];
    let peor: NivelDx = 'ok';
    for (const dx of dxs) {
      const nivel = this.nivelDx(dx);
      if (nivel === 'danger') return 'danger';
      if (nivel === 'warn') peor = 'warn';
    }
    return peor;
  });

  protected readonly conteoNut = computed(() => {
    const n = this.nut();
    const dxs = [
      n?.detalleAntropometrico?.dxImc,
      n?.detalleAntropometrico?.dxPerimetroAbdominal,
      n?.detalleAntropometrico?.dxGrasa,
      n?.detalleAntropometrico?.dxMasaMuscular,
      n?.detalleAntropometrico?.dxGrasaVisceral,
    ];
    let ok = 0;
    let warn = 0;
    let danger = 0;
    for (const dx of dxs) {
      const nivel = this.nivelDx(dx);
      if (nivel === 'ok') ok++;
      else if (nivel === 'warn') warn++;
      else if (nivel === 'danger') danger++;
    }
    const bio = this.nivelDx(n?.detalleBioquimico?.dxBioquimico);
    if (bio === 'ok') ok++;
    else if (bio === 'warn') warn++;
    else if (bio === 'danger') danger++;
    return { ok, warn, danger };
  });

  /** Analitos afectados según dxBioquimico ("ALTERADO: GLUCOSA, …"). */
  protected readonly bioAfectados = computed<Set<string>>(() => {
    const dx = this.nut()?.detalleBioquimico?.dxBioquimico ?? '';
    const m = /ALTERADO\s*:\s*(.+)/i.exec(dx);
    if (!m) return new Set();
    return new Set(
      m[1]
        .split(',')
        .map((s) =>
          s
            .trim()
            .toUpperCase()
            .replace(/[^A-Z_]/g, ''),
        )
        .filter((s) => !!s),
    );
  });

  protected bioEstado(clave: string): NivelDx {
    const dx = this.nut()?.detalleBioquimico?.dxBioquimico ?? '';
    if (!dx) return 'neutro';
    if (/NORMAL/.test(dx.toUpperCase())) return 'ok';
    return this.bioAfectados().has(clave) ? 'danger' : 'ok';
  }

  protected claseFitness(score?: number | null): string {
    if (score === undefined || score === null) return 'pill-neutral';
    if (score >= 70) return 'pill-success';
    if (score >= 50) return 'pill-primary';
    if (score >= 30) return 'pill-warning';
    return 'pill-error';
  }

  protected etiquetaFitness(score?: number | null): string {
    if (score === undefined || score === null) return 'Sin evaluar';
    if (score >= 85) return 'Excelente';
    if (score >= 70) return 'Muy bueno';
    if (score >= 50) return 'Bueno';
    if (score >= 30) return 'Regular';
    return 'Bajo';
  }

  protected claseDictamen(d?: string): string {
    switch (d) {
      case 'APTO':
        return 'pill-success';
      case 'APTO_CON_RESTRICCIONES':
        return 'pill-warning';
      case 'NO_APTO':
        return 'pill-error';
      default:
        return 'pill-neutral';
    }
  }

  protected etiquetaDictamen(d?: string): string {
    switch (d) {
      case 'APTO':
        return 'Apto';
      case 'APTO_CON_RESTRICCIONES':
        return 'Apto con restricciones';
      case 'NO_APTO':
        return 'No apto';
      default:
        return 'Sin dictamen';
    }
  }
}
