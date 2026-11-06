import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PersonaService } from '../persona/persona-service';
import { TeamService } from '../../teams/team-service';
import { AreaService } from '../../catalogo/area/area-service';
import { Persona, PersonalAgregado } from '../persona/persona.model';
import { Team } from '../../teams/team.model';
import { Area } from '../../catalogo/area/area.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NgxEchartsDirective, TuiButton, TuiIcon],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class Dashboard implements OnInit {
  private readonly personaService = inject(PersonaService);
  private readonly equipoService = inject(TeamService);
  private readonly areaService = inject(AreaService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly resumen = signal<PersonalAgregado | null>(null);

  // Opciones de los 3 graficos
  protected readonly opcionesPorEquipo = signal<EChartsOption>({});
  protected readonly opcionesPorArea = signal<EChartsOption>({});
  protected readonly opcionesActivos = signal<EChartsOption>({});

  ngOnInit(): void {
    this.cargarTodo();
  }

  private cargarTodo(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      resumen: this.personaService.reporte(),
      equipos: this.equipoService.listar(true),
      areas: this.areaService.listar(true),
      personas: this.personaService.listar({
        estado: true,
        ordenarPor: 'apellidoPaterno',
        direccion: 'ASC',
      }),
    })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          if (err.status === 0) {
            this.error.set('No se pudo conectar con el servidor. Verifica que el backend esté corriendo.');
          } else {
            this.error.set('No se pudo cargar el dashboard.');
          }
          this.loading.set(false);
          return of(null);
        })
      )
      .subscribe({
        next: (data) => {
          if (!data) return;
          this.resumen.set(data.resumen);
          this.construirGraficoPorEquipo(data.equipos, data.personas);
          this.construirGraficoPorArea(data.areas, data.personas);
          this.construirGraficoActivos(data.resumen);
          this.loading.set(false);
        },
      });
  }

  // -------------------------------------------------------------------
  // Graficos
  // -------------------------------------------------------------------

  private construirGraficoPorEquipo(equipos: Team[], personas: Persona[]): void {
    const conteo = new Map<number, number>();
    for (const p of personas) {
      const idEquipo = p.equipo?.id;
      if (idEquipo) {
        conteo.set(idEquipo, (conteo.get(idEquipo) ?? 0) + 1);
      }
    }

    const nombres: string[] = [];
    const valores: number[] = [];
    for (const e of equipos) {
      nombres.push(e.nombre);
      valores.push(conteo.get(e.id) ?? 0);
    }

    this.opcionesPorEquipo.set({
      title: {
        text: 'Personas activas por equipo',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold', color: '#111c2d' },
      },
      tooltip: { trigger: 'axis' },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '20%' },
      xAxis: {
        type: 'category',
        data: nombres,
        axisLabel: { rotate: nombres.some((n) => n.length > 10) ? 20 : 0 },
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          type: 'bar',
          data: valores,
          itemStyle: {
            color: '#00a1ff',
            borderRadius: [6, 6, 0, 0],
          },
          label: { show: true, position: 'top' },
        },
      ],
    });
  }

  private construirGraficoPorArea(areas: Area[], personas: Persona[]): void {
    const conteo = new Map<number, number>();
    for (const p of personas) {
      const idArea = p.areaTrabajo?.id;
      if (idArea) {
        conteo.set(idArea, (conteo.get(idArea) ?? 0) + 1);
      }
    }

    const datos = areas
      .map((a) => ({ nombre: a.nombre, valor: conteo.get(a.id) ?? 0 }))
      .filter((d) => d.valor > 0)
      .sort((a, b) => b.valor - a.valor);

    this.opcionesPorArea.set({
      title: {
        text: 'Distribución por área de trabajo',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold', color: '#111c2d' },
      },
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [
        {
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          label: { show: true, formatter: '{b}\n{c}' },
          data: datos.map((d) => ({ name: d.nombre, value: d.valor })),
        },
      ],
    });
  }

  private construirGraficoActivos(resumen: PersonalAgregado): void {
    const activos = resumen.totalActivos;
    const inactivos = resumen.totalPersonal - resumen.totalActivos;

    this.opcionesActivos.set({
      title: {
        text: 'Estado del personal',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold', color: '#111c2d' },
      },
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          label: { show: true, formatter: '{b}\n{c}' },
          data: [
            { name: 'Activos', value: activos, itemStyle: { color: '#16a34a' } },
            { name: 'Inactivos', value: inactivos, itemStyle: { color: '#94a3b8' } },
          ],
        },
      ],
    });
  }
}
