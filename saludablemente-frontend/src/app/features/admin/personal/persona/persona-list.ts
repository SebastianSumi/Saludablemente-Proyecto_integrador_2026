import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiDataListWrapper, TuiComboBox } from '@taiga-ui/kit';
import { Avatar, urlAvatarFoto } from '../../../../shared/components/avatar/avatar';
import { TuiTable } from '@taiga-ui/addon-table';
import { PersonaService } from './persona-service';
import { TeamService } from '../../teams/team-service';
import { AreaService } from '../../catalogo/area/area-service';
import { Team } from '../../teams/team.model';
import { Area } from '../../catalogo/area/area.model';
import { Persona, PersonaFiltros, Sexo } from './persona.model';
import { ConfirmService } from '../../../../core/services/confirm-service';

interface FiltroOpcion {
  id: string;
  label: string;
}

interface FiltroEntidad {
  id: number | null;
  label: string;
}

@Component({
  selector: 'app-persona-list',
  imports: [
    Avatar,
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiBadge,
    TuiChevron,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiIcon,
    TuiComboBox,
    TuiTable,
    TuiInput,
    TuiTextfield,
  ],
  templateUrl: './persona-list.html',
  styleUrl: './persona-list.css',
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
export class PersonaList implements OnInit {
  private readonly personaService = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly equipoService = inject(TeamService);
  private readonly areaService = inject(AreaService);
  private readonly route = inject(ActivatedRoute);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly areas = signal<Area[]>([]);
  protected readonly equipos = signal<Team[]>([]);

  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly filtroEstado = signal<boolean | null>(null);
  protected readonly filtroSexo = signal<Sexo | null>(null);
  protected readonly filtroAreaId = signal<number | null>(null);
  protected readonly filtroEquipoId = signal<number | null>(null);
  protected readonly filtroTexto = signal<string>('');

  protected readonly estadoOpts: FiltroOpcion[] = [
    { id: 'todos', label: 'Todos' },
    { id: 'activos', label: 'Solo activos' },
    { id: 'inactivos', label: 'Solo inactivos' },
  ];

  protected readonly sexoOpts: FiltroOpcion[] = [
    { id: 'todos', label: 'Todos' },
    { id: 'M', label: 'Masculino' },
    { id: 'F', label: 'Femenino' },
  ];

  protected readonly textoCtrl = new FormControl<string>('', { nonNullable: true });
  protected readonly estadoCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly sexoCtrl = new FormControl<FiltroOpcion | null>(null);
  protected readonly areaCtrl = new FormControl<FiltroEntidad | null>(null);
  protected readonly equipoCtrl = new FormControl<FiltroEntidad | null>(null);

  protected readonly areaOpts = computed<FiltroEntidad[]>(() => [
    { id: null, label: 'Todas' },
    ...this.areas().map((a) => ({ id: a.id, label: a.nombre })),
  ]);

  protected readonly equipoOpts = computed<FiltroEntidad[]>(() => [
    { id: null, label: 'Todos' },
    ...this.equipos().map((e) => ({ id: e.id, label: e.nombre })),
  ]);

  protected readonly stringifyFiltro = (o: FiltroOpcion | null): string => o?.label ?? '';
  protected readonly matcherFiltro = (
    a: FiltroOpcion | null,
    b: FiltroOpcion | null
  ): boolean => (a?.id ?? null) === (b?.id ?? null);
  protected readonly stringifyEntidad = (o: FiltroEntidad | null): string =>
    o?.label ?? '';
  protected readonly matcherEntidad = (
    a: FiltroEntidad | null,
    b: FiltroEntidad | null
  ): boolean => (a?.id ?? null) === (b?.id ?? null);

  ngOnInit(): void {
    this.estadoCtrl.valueChanges.subscribe((o) => {
      const id = o?.id ?? 'todos';
      this.filtroEstado.set(id === 'todos' ? null : id === 'activos');
      this.cargar();
    });
    this.sexoCtrl.valueChanges.subscribe((o) => {
      const id = o?.id ?? 'todos';
      this.filtroSexo.set(id === 'todos' ? null : (id as Sexo));
      this.cargar();
    });
    this.areaCtrl.valueChanges.subscribe((o) => {
      this.filtroAreaId.set(o?.id ?? null);
      this.cargar();
    });
    this.equipoCtrl.valueChanges.subscribe((o) => {
      this.filtroEquipoId.set(o?.id ?? null);
      this.cargar();
    });

    this.textoCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((valor) => {
        this.filtroTexto.set(valor ?? '');
        this.cargar();
      });

    const areaParam = this.route.snapshot.queryParamMap.get('area');
    if (areaParam) {
      this.filtroAreaId.set(Number(areaParam));
    }

    const equipoParam = this.route.snapshot.queryParamMap.get('equipo');
    if (equipoParam) {
      this.filtroEquipoId.set(Number(equipoParam));
    }

    this.cargarCatalogos();
    this.sincronizarControles();
    this.cargar();
  }

  private cargarCatalogos(): void {
    this.areaService.listar(true).subscribe({
      next: (data) => {
        this.areas.set(data);
        this.sincronizarControles();
      },
      error: () => { /* silencioso */ },
    });

    this.equipoService.listar(true).subscribe({
      next: (data) => {
        this.equipos.set(data);
        this.sincronizarControles();
      },
      error: () => { /* silencioso */ },
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const texto = this.filtroTexto().trim();
    const filtros: PersonaFiltros = {
      estado: this.filtroEstado() ?? undefined,
      sexo: this.filtroSexo() ?? undefined,
      areaId: this.filtroAreaId() ?? undefined,
      equipoId: this.filtroEquipoId() ?? undefined,
      texto: texto || undefined,
      ordenarPor: 'apellidoPaterno',
      direccion: 'ASC',
    };

    this.personaService.listar(filtros).subscribe({
      next: (data) => this.personas.set(data),
      error: (err: HttpErrorResponse) => {
        if (err.status === 0) {
          this.error.set('No se pudo conectar con el servidor. Verifica que el backend esté corriendo.');
        } else {
          this.error.set('No se pudo cargar la lista de personas.');
        }
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  // -------------------------------------------------------------------
  // Cambio de filtros (vía tuiSelect)
  // -------------------------------------------------------------------

  private sincronizarControles(): void {
    const estado = this.filtroEstado();
    this.estadoCtrl.setValue(
      this.estadoOpts.find((o) =>
        estado === null ? o.id === 'todos' : o.id === (estado ? 'activos' : 'inactivos')
      ) ?? null,
      { emitEvent: false }
    );
    const sexo = this.filtroSexo();
    this.sexoCtrl.setValue(
      this.sexoOpts.find((o) => (sexo === null ? o.id === 'todos' : o.id === sexo)) ?? null,
      { emitEvent: false }
    );
    const areaId = this.filtroAreaId();
    this.areaCtrl.setValue(
      areaId === null
        ? { id: null, label: 'Todas' }
        : this.areaOpts().find((o) => o.id === areaId) ?? { id: null, label: 'Todas' },
      { emitEvent: false }
    );
    const equipoId = this.filtroEquipoId();
    this.equipoCtrl.setValue(
      equipoId === null
        ? { id: null, label: 'Todos' }
        : this.equipoOpts().find((o) => o.id === equipoId) ?? { id: null, label: 'Todos' },
      { emitEvent: false }
    );
  }

  limpiarFiltros(): void {
    this.filtroEstado.set(null);
    this.filtroSexo.set(null);
    this.filtroAreaId.set(null);
    this.filtroEquipoId.set(null);
    this.filtroTexto.set('');
    this.textoCtrl.setValue('', { emitEvent: false });
    this.sincronizarControles();
    this.cargar();
  }

  aplicarTexto(): void {
    this.filtroTexto.set(this.textoCtrl.value);
    this.cargar();
  }

  // -------------------------------------------------------------------
  // Acciones
  // -------------------------------------------------------------------

  inactivar(persona: Persona): void {
    const nombre = this.nombreCompleto(persona);
    this.confirmar
      .confirmar(`¿Inactivar a ${nombre}? Su credencial QR será anulada.`, 'Inactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.personaService.inactivar(persona.id).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            if (err.status === 409) {
              this.error.set(err.error?.message ?? 'No se puede inactivar la persona.');
            } else {
              this.error.set('No se pudo inactivar la persona.');
            }
          },
        });
      });
  }

  reactivar(persona: Persona): void {
    const nombre = this.nombreCompleto(persona);
    this.confirmar
      .confirmar(`¿Reactivar a ${nombre}? Se emitirá una nueva credencial QR.`, 'Reactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.personaService.reactivar(persona.id).subscribe({
          next: () => this.cargar(),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo reactivar la persona.');
          },
        });
      });
  }

  // -------------------------------------------------------------------
  // Helpers de plantilla
  // -------------------------------------------------------------------

  protected nombreCompleto(persona: Persona): string {
    const partes = [
      persona.apellidoPaterno,
      persona.apellidoMaterno,
      persona.nombres,
    ].filter((p) => !!p);
    return partes.join(' ');
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

  protected nombreEquipo(persona: Persona): string {
    return persona.equipo?.nombre ?? 'Sin equipo';
  }

  protected nombreArea(persona: Persona): string {
    return persona.areaTrabajo?.nombre ?? 'Sin área';
  }
}
