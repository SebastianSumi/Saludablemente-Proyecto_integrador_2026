import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiChevron,
  TuiDataListWrapper,
  TuiComboBox,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MetaService } from '../meta-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { EstadoMeta, Meta } from '../models';
import { Persona } from '../../personal/persona/persona.model';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-meta-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiBadge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiTable,
  ],
  templateUrl: './meta-list.html',
  styleUrl: './meta-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; label?: string } | null;
          return o?.nombre ?? o?.label ?? (typeof item === 'string' ? (item as string) : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? a ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? b ?? null)
      ),
    }),
  ],
})
export class MetaList implements OnInit {
  private readonly service = inject(MetaService);
  private readonly personasSrv = inject(PersonaService);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly items = signal<Meta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'ACTIVA', label: 'Activa' },
    { id: 'COMPLETADA', label: 'Completada' },
    { id: 'DESVIADA', label: 'Desviada' },
    { id: 'ABANDONADA', label: 'Abandonada' },
  ];

  protected readonly personaCtrl = new FormControl<Persona | null>(null);
  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);

  ngOnInit(): void {
    this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))).subscribe((p) => {
      this.personas.set(p);
    });
    this.personaCtrl.valueChanges.subscribe(() => this.cargar());
    this.estadoCtrl.valueChanges.subscribe(() => this.cargar());
  }

  cargar(): void {
    const persona = this.personaCtrl.value;
    if (!persona?.id) {
      this.items.set([]);
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service
      .listarPorPersona(
        persona.id,
        (this.estadoCtrl.value?.id || undefined) as EstadoMeta | undefined
      )
      .subscribe({
        next: (data) => {
          this.items.set(data);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las metas.'
          );
          this.loading.set(false);
        },
      });
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  protected iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }

  protected aparienciaEstado(e: EstadoMeta): string {
    switch (e) {
      case 'ACTIVA':
        return 'primary';
      case 'COMPLETADA':
        return 'positive';
      case 'DESVIADA':
        return 'warning';
      default:
        return 'neutral';
    }
  }

  limpiarFiltros(): void {
    this.personaCtrl.setValue(null);
    this.estadoCtrl.setValue(null);
    this.items.set([]);
  }
}
