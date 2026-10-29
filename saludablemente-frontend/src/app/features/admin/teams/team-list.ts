import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { TeamService } from './team-service';
import { Team } from './team.model';
import { ConfirmService } from '../../../core/services/confirm-service';

interface FiltroOpcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-team-list',
  imports: [RouterLink, ReactiveFormsModule, TuiButton, TuiBadge, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiLabel, TuiTextfield, TuiTable],
  templateUrl: './team-list.html',
  styleUrl: './team-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => (item as { label?: string } | null)?.label ?? ''
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class TeamList implements OnInit {

  private readonly teamService = inject(TeamService);
  private readonly confirmar = inject(ConfirmService);
  protected readonly equipos = signal<Team[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly filtroActivo = signal<boolean | null>(true);

  protected readonly columnas = ['nombre', 'descripcion', 'estado', 'acciones'];

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: 'activos', label: 'Solo activos' },
    { id: 'todos', label: 'Todos' },
    { id: 'inactivos', label: 'Solo inactivos' },
  ];

  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);

  ngOnInit(): void {
    this.sincronizarControl();
    this.estadoCtrl.valueChanges.subscribe((o) => {
      const id = o?.id ?? 'todos';
      if (id === 'todos') this.filtroActivo.set(null);
      else if (id === 'activos') this.filtroActivo.set(true);
      else this.filtroActivo.set(false);
      this.cargar();
    });
    this.cargar();
  }

  private sincronizarControl(): void {
    const activo = this.filtroActivo();
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) =>
        activo === null ? o.id === 'todos' : o.id === (activo ? 'activos' : 'inactivos')
      ) ?? null,
      { emitEvent: false }
    );
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const activo = this.filtroActivo();
    const filtro = activo === null ? undefined : activo;

    this.teamService.listar(filtro).subscribe({
      next: (data) => this.equipos.set(data),
      error: () => {
        this.error.set('No se pudo cargar la lista de equipos.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  cambiarFiltro(valor: string): void {
    if (valor === 'todos') {
      this.filtroActivo.set(null);
    } else if (valor === 'activos') {
      this.filtroActivo.set(true);
    } else if (valor === 'inactivos') {
      this.filtroActivo.set(false);
    }
    this.cargar();
  }

  inactivar(equipo: Team): void {
    this.confirmar
      .confirmar(`¿Inactivar el equipo "${equipo.nombre}"?`, 'Inactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.teamService.inactivar(equipo.id).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'No se puede inactivar el equipo.');
            } else {
              this.error.set('No se pudo inactivar el equipo.');
            }
          },
        });
      });
  }
}
