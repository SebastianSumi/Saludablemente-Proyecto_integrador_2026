import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiChevron, TuiComboBox, TuiDataListWrapper } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { catchError, of } from 'rxjs';
import { MetaService } from '../../admin/metas/meta-service';
import { PersonaService } from '../../admin/personal/persona/persona-service';
import { EstadoMeta, Meta } from '../../admin/metas/models';
import { Persona } from '../../admin/personal/persona/persona.model';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-eval-metas-list',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './eval-metas-list.html',
  styleUrls: ['../evaluador-comun.css', '../evaluador-tablas-detalle.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as
            | { label?: string; nombres?: string; documentoIdentidad?: string; apellidoPaterno?: string }
            | null;
          if (o?.nombres) {
            return `${o.documentoIdentidad ?? ''} ${o.apellidoPaterno ?? ''} ${o.nombres}`.trim();
          }
          return o?.label ?? '';
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class EvalMetasList implements OnInit {
  private readonly service = inject(MetaService);
  private readonly personasSrv = inject(PersonaService);
  private readonly route = inject(ActivatedRoute);

  protected readonly personas = signal<Persona[]>([]);
  protected readonly items = signal<Meta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos los estados' },
    { id: 'ACTIVA', label: 'Activa' },
    { id: 'COMPLETADA', label: 'Completada' },
    { id: 'DESVIADA', label: 'Desviada' },
    { id: 'ABANDONADA', label: 'Abandonada' },
  ];

  protected readonly personaCtrl = new FormControl<Persona | null>(null);
  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);

  ngOnInit(): void {
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });

    this.personasSrv
      .listar({ estado: true })
      .pipe(catchError(() => of([])))
      .subscribe((p) => {
        this.personas.set(p);
        const personaId = Number(this.route.snapshot.queryParamMap.get('persona'));
        if (personaId) {
          const elegida = p.find((x) => x.id === personaId);
          if (elegida) {
            this.personaCtrl.setValue(elegida);
            return;
          }
        }
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
      .listarPorPersona(persona.id, (this.estadoCtrl.value?.id || undefined) as EstadoMeta | undefined)
      .subscribe({
        next: (data) => {
          this.items.set(data);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0
              ? 'No se pudo conectar con el servidor.'
              : 'No se pudieron cargar las metas.'
          );
          this.loading.set(false);
        },
      });
  }

  limpiar(): void {
    this.personaCtrl.setValue(null);
    this.estadoCtrl.setValue(this.estadoOpts[0], { emitEvent: false });
    this.items.set([]);
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  protected pillEstado(estado: EstadoMeta): string {
    switch (estado) {
      case 'ACTIVA':
        return 'pill pill-primary';
      case 'COMPLETADA':
        return 'pill pill-success';
      case 'DESVIADA':
        return 'pill pill-warning';
      default:
        return 'pill pill-neutral';
    }
  }
}
