import { Component, DestroyRef, inject, signal, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiCalendar, TuiDropdown, TuiFilterByInputPipe, TuiError, TuiIcon, TuiInput, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiButtonLoading,
  TuiChevron,
  TuiDataListWrapper,
  TuiComboBox,
  TuiInputDate,
} from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PersonaService } from './persona-service';
import { TeamService } from '../../teams/team-service';
import { AreaService } from '../../catalogo/area/area-service';
import { Team } from '../../teams/team.model';
import { Area } from '../../catalogo/area/area.model';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-persona-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiCalendar,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './persona-form.html',
  styleUrl: './persona-form.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => {
        if (item === null || item === undefined) return '';
        if (typeof item === 'string') return item;
        const o = item as { label?: string; nombre?: string };
        return o.label ?? o.nombre ?? '';
      }),
      identityMatcher: signal((a: unknown, b: unknown): boolean => {
        if (a === b) return true;
        if (a === null || a === undefined || b === null || b === undefined) return false;
        if (typeof a === 'string' || typeof b === 'string') return a === b;
        const x = a as { id?: unknown; nombre?: string };
        const y = b as { id?: unknown; nombre?: string };
        return (x.id ?? x.nombre) === (y.id ?? y.nombre);
      }),
    }),
  ],
})
export class PersonaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly personaService = inject(PersonaService);
  private readonly equipoService = inject(TeamService);
  private readonly areaService = inject(AreaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly guardando = signal(false);

  protected readonly equipos = signal<Team[]>([]);
  protected readonly areas = signal<Area[]>([]);

  protected readonly sexos: Opcion[] = [
    { id: 'M', label: 'Masculino' },
    { id: 'F', label: 'Femenino' },
  ];

  protected readonly estadosCiviles: string[] = [
    'Soltero',
    'Casado',
    'Divorciado',
    'Viudo',
    'Conviviente',
  ];

  protected readonly nivelesEducativos: string[] = [
    'Sin instrucción',
    'Primaria',
    'Secundaria',
    'Técnico',
    'Universitario',
    'Postgrado',
  ];

  protected readonly stringifyOpcion = (o: Opcion | null): string => o?.label ?? '';
  protected readonly matcherOpcion = (a: Opcion | null, b: Opcion | null): boolean =>
    (a?.id ?? null) === (b?.id ?? null);
  protected readonly stringifyArea = (a: Area | null): string => a?.nombre ?? '';
  protected readonly matcherArea = (a: Area | null, b: Area | null): boolean =>
    (a?.id ?? null) === (b?.id ?? null);
  protected readonly stringifyEquipo = (e: Team | null): string => e?.nombre ?? '';
  protected readonly matcherEquipo = (e: Team | null, b: Team | null): boolean =>
    (e?.id ?? null) === (b?.id ?? null);

  protected readonly form = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidoPaterno: ['', [Validators.required, Validators.maxLength(100)]],
    apellidoMaterno: ['', [Validators.maxLength(100)]],
    documentoIdentidad: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    celular: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{9,15}$/)]],
    fechaNacimiento: ['', [Validators.required]],
    sexo: [null as Opcion | null, [Validators.required]],
    area: [null as Area | null],
    equipo: [null as Team | null],
    tallaPolo: ['', [Validators.maxLength(5)]],
    estadoCivil: [''],
    nivelEducativo: [''],
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.cargarListasYEntidad();
    });
  }

  private cargarListasYEntidad(): void {
    this.loading.set(true);
    this.errorCarga.set(false);
    this.error.set(null);
    forkJoin({
      areas: this.areaService.listar(true).pipe(catchError(() => of([]))),
      equipos: this.equipoService.listar(true).pipe(catchError(() => of([]))),
    }).subscribe(({ areas, equipos }) => {
      this.areas.set(areas);
      this.equipos.set(equipos);

      const idParam = this.route.snapshot.paramMap.get('id');
      if (!idParam) {
        this.loading.set(false);
        return;
      }
      const id = Number(idParam);
      this.id.set(id);
      this.personaService.obtener(id).subscribe({
        next: (persona) => {
          this.form.patchValue({
            nombres: persona.nombres,
            apellidoPaterno: persona.apellidoPaterno,
            apellidoMaterno: persona.apellidoMaterno ?? '',
            documentoIdentidad: persona.documentoIdentidad,
            celular: persona.celular,
            fechaNacimiento: persona.fechaNacimiento,
            sexo: this.sexos.find((s) => s.id === persona.sexo) ?? null,
            area: areas.find((a) => a.id === persona.areaTrabajo?.id) ?? null,
            equipo: equipos.find((e) => e.id === persona.equipo?.id) ?? null,
            tallaPolo: persona.tallaPolo ?? '',
            estadoCivil: persona.estadoCivil ?? '',
            nivelEducativo: persona.nivelEducativo ?? '',
          });
          this.loading.set(false);
        },
        error: () => {
          this.errorCarga.set(true);
          this.error.set('No se pudo cargar la persona.');
          this.loading.set(false);
        },
      });
    });
  }

  guardar(): void {
    if (this.loading() || this.errorCarga() || this.guardando()) return;

    this.error.set(null);

    // Trim antes de validar
    const controles = ['nombres', 'apellidoPaterno', 'apellidoMaterno',
                       'documentoIdentidad', 'celular', 'tallaPolo'] as const;
    for (const nombre of controles) {
      const ctrl = this.form.controls[nombre];
      ctrl.setValue((ctrl.value ?? '').trim());
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const id = this.id();

    const payload = {
      nombres: valor.nombres ?? '',
      apellidoPaterno: valor.apellidoPaterno ?? '',
      apellidoMaterno: valor.apellidoMaterno || undefined,
      documentoIdentidad: valor.documentoIdentidad ?? '',
      celular: valor.celular ?? '',
      fechaNacimiento: valor.fechaNacimiento ?? '',
      sexo: valor.sexo?.id as 'M' | 'F',
      areaId: valor.area?.id ?? undefined,
      equipoId: valor.equipo?.id ?? undefined,
      tallaPolo: valor.tallaPolo || undefined,
      estadoCivil: valor.estadoCivil || undefined,
      nivelEducativo: valor.nivelEducativo || undefined,
    };

    const peticion = id
      ? this.personaService.actualizar(id, payload)
      : this.personaService.crear(payload);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => this.router.navigate(['/personal/personas']),
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Datos duplicados.');
        } else if (err.status === 404) {
          this.error.set(err.error?.message ?? 'Recurso no encontrado.');
        } else if (err.status === 400) {
          this.error.set(err.error?.message ?? 'Datos inválidos.');
        } else {
          this.error.set('No se pudo guardar la persona.');
        }
        this.guardando.set(false);
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/personal/personas']);
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.form.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.form.get(campo));
  }
}