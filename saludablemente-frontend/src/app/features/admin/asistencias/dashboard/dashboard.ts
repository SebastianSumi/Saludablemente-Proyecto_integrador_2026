import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiSelect } from '@taiga-ui/kit';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ActividadService, ReporteAsistenciaService } from '../reporte-qr-actividad-service';
import { ExoneracionService } from '../exoneracion-service';
import { Actividad, Ausentismo, Participacion } from '../models';

@Component({
  selector: 'app-asistencia-dashboard',
  imports: [
    ReactiveFormsModule,
    NgxEchartsDirective,
    TuiBadge,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiIcon,
    TuiSelect,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; idActividad?: unknown } | null;
          return o?.nombre ?? (o?.idActividad !== undefined && o?.idActividad !== null ? `Actividad #${String(o.idActividad)}` : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { idActividad?: unknown } | null)?.idActividad ?? null) ===
          ((b as { idActividad?: unknown } | null)?.idActividad ?? null)
      ),
    }),
  ],
})
export class AsistenciaDashboard implements OnInit {
  private readonly actividadesSrv = inject(ActividadService);
  private readonly reportes = inject(ReporteAsistenciaService);
  private readonly exoneraciones = inject(ExoneracionService);

  protected readonly actividades = signal<Actividad[]>([]);
  protected readonly participacion = signal<Participacion | null>(null);
  protected readonly ausentismo = signal<Ausentismo | null>(null);
  protected readonly gamificados = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly actividadCtrl = new FormControl<Actividad | null>(null);
  protected readonly opcionesEstado = signal<EChartsOption>({});
  protected readonly opcionesMetodo = signal<EChartsOption>({});

  ngOnInit(): void {
    this.actividadesSrv.listar().pipe(catchError(() => of([]))).subscribe((acts) => {
      this.actividades.set(acts);
      if (acts.length > 0) {
        this.actividadCtrl.setValue(acts[0], { emitEvent: false });
        this.cargar();
      }
    });
    this.actividadCtrl.valueChanges.subscribe(() => this.cargar());
    this.exoneraciones.detectarAusentismo(3).pipe(catchError(() => of(null))).subscribe((a) => {
      if (a) this.ausentismo.set(a);
    });
  }

  cargar(): void {
    const act = this.actividadCtrl.value;
    if (!act?.idActividad) return;
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      part: this.reportes.participacion(act.idActividad),
      gam: this.reportes.gamificacion(act.idActividad).pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ part, gam }) => {
        this.participacion.set(part);
        this.gamificados.set(gam.length);
        this.construirGraficos(part);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar el dashboard.'
        );
        this.loading.set(false);
      },
    });
  }

  private construirGraficos(p: Participacion): void {
    this.opcionesEstado.set({
      title: { text: 'Estado de asistencias', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'item', formatter: '{b}: {c}' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data: [
            { name: 'Asistentes', value: p.totalAsistentes, itemStyle: { color: '#3b82f6' } },
            { name: 'Ausentes', value: p.totalAusentes, itemStyle: { color: '#9ca3af' } },
            { name: 'Exonerados', value: p.totalExonerados, itemStyle: { color: '#d97706' } },
            { name: 'Incompletas', value: p.totalIncompletas, itemStyle: { color: '#8b5cf6' } },
          ],
        },
      ],
    });
    this.opcionesMetodo.set({
      title: { text: 'Resumen del evento', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '20%' },
      xAxis: {
        type: 'category',
        data: ['Esperados', 'Asistentes', 'Cuarentena'],
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          type: 'bar',
          data: [p.totalEsperados, p.totalAsistentes, p.totalCuarentenaPendiente],
          itemStyle: { color: '#3b82f6' },
          label: { show: true, position: 'top' },
        },
      ],
    });
  }
}
