import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { Avatar, urlAvatarFoto } from '../../../shared/components/avatar/avatar';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import { AreaService } from '../../admin/catalogo/area/area-service';
import { Area } from '../../admin/catalogo/area/area.model';
import { Persona, PersonaFiltros } from '../../admin/personal/persona/persona.model';

interface FiltroEntidad {
  id: number | null;
  label: string;
}

@Component({
  selector: 'app-eval-colaboradores',
  standalone: true,
  imports: [
    Avatar,
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './eval-colaboradores.html',
  styleUrls: ['../evaluador-comun.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string =>
          (item as { label?: string } | null | undefined)?.label ?? ''
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class EvalColaboradores implements OnInit {
  private readonly personaService = inject(PersonaService);
  private readonly areaService = inject(AreaService);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly areas = signal<Area[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly filtroAreaId = signal<number | null>(null);
  protected readonly filtroTexto = signal<string>('');

  protected readonly textoCtrl = new FormControl<string>('', { nonNullable: true });
  protected readonly areaCtrl = new FormControl<FiltroEntidad | null>(null);

  protected readonly areaOpts = computed<FiltroEntidad[]>(() => [
    { id: null, label: 'Todas las áreas' },
    ...this.areas().map((a) => ({ id: a.id, label: a.nombre })),
  ]);

  ngOnInit(): void {
    this.textoCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((valor) => {
        this.filtroTexto.set(valor ?? '');
        this.cargar();
      });

    this.areaCtrl.valueChanges.subscribe((o) => {
      this.filtroAreaId.set(o?.id ?? null);
      this.cargar();
    });

    this.areaService.listar(true).subscribe({
      next: (data) => {
        this.areas.set(data);
        this.areaCtrl.setValue({ id: null, label: 'Todas las áreas' }, { emitEvent: false });
      },
      error: () => undefined,
    });

    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const texto = this.filtroTexto().trim();
    const filtros: PersonaFiltros = {
      estado: true,
      areaId: this.filtroAreaId() ?? undefined,
      texto: texto || undefined,
      ordenarPor: 'apellidoPaterno',
      direccion: 'ASC',
    };

    this.personaService.listar(filtros).subscribe({
      next: (data) => this.personas.set(data),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar la lista de colaboradores.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  limpiarFiltros(): void {
    this.filtroTexto.set('');
    this.filtroAreaId.set(null);
    this.textoCtrl.setValue('', { emitEvent: false });
    this.areaCtrl.setValue({ id: null, label: 'Todas las áreas' }, { emitEvent: false });
    this.cargar();
  }

  protected nombreCompleto(persona: Persona): string {
    return [persona.apellidoPaterno, persona.apellidoMaterno, persona.nombres]
      .filter((p) => !!p)
      .join(' ');
  }

  protected iniciales(persona: Persona): string {
    return this.nombreCompleto(persona)
      .split(' ')
      .filter((p) => p.length > 0)
      .slice(0, 2)
      .map((p) => p.charAt(0).toUpperCase())
      .join('');
  }

  protected fotoDe(persona: Persona): string | undefined {
    return urlAvatarFoto(persona.avatarUrl);
  }
}
