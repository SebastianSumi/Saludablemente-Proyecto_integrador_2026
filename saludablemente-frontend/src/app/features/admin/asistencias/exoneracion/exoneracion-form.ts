import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiSelect } from '@taiga-ui/kit';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ExoneracionService } from '../exoneracion-service';
import { ActividadService } from '../reporte-qr-actividad-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { Actividad } from '../models';
import { Persona } from '../../personal/persona/persona.model';

@Component({
  selector: 'app-exoneracion-form',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './exoneracion-form.html',
  styleUrl: './exoneracion-form.css',
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
export class ExoneracionForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ExoneracionService);
  private readonly actividadesSrv = inject(ActividadService);
  private readonly personasSrv = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly router = inject(Router);

  protected readonly actividades = signal<Actividad[]>([]);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    actividad: [null as Actividad | null, [Validators.required]],
    persona: [null as Persona | null, [Validators.required]],
    motivo: ['', [Validators.required, Validators.maxLength(300)]],
    urlDocumento: ['', [Validators.maxLength(250)]],
  });

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({
      acts: this.actividadesSrv.listar().pipe(catchError(() => of([]))),
      pers: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
    }).subscribe(({ acts, pers }) => {
      this.actividades.set(acts);
      this.personas.set(pers);
      this.loading.set(false);
    });
  }

  protected nombreActividad(a: Actividad): string {
    return a.nombre;
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa actividad, persona y motivo.');
      return;
    }
    const v = this.form.getRawValue();
    this.confirmar
      .confirmar(`¿Solicitar exoneración para ${this.nombrePersona(v.persona as Persona)}?`, 'Solicitar')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        this.service
          .solicitar({
            idActividad: (v.actividad as Actividad).idActividad,
            idPersona: (v.persona as Persona).id,
            motivo: (v.motivo ?? '').trim(),
            urlDocumento: v.urlDocumento.trim() || undefined,
          })
          .subscribe({
            next: () => this.router.navigate(['/asistencias/exoneraciones']),
            error: (err: HttpErrorResponse) => {
              if (err.status === 409) {
                this.error.set(err.error?.message ?? 'Ya existe una solicitud.');
              } else if (err.status === 400) {
                this.error.set(err.error?.message ?? 'Datos inválidos.');
              } else {
                this.error.set('No se pudo registrar la solicitud.');
              }
              this.guardando.set(false);
            },
          });
      });
  }

  cancelar(): void {
    this.router.navigate(['/asistencias/exoneraciones']);
  }
}