import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiCalendar,
  TuiDropdown,
  TuiError,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiButtonLoading,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
  TuiInputDate,
  TuiTabs,
  TuiTextarea,
} from '@taiga-ui/kit';
import { fijarHoyEn, limpiarFechaEn } from '../../../../core/forms/fecha-transformers';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AgendaService, ActividadImagenService, ConfiguracionActividadService, InscripcionService, OperacionService, ReporteActividadService } from '../agenda-service';
import { QrActividadService } from '../../asistencias/reporte-qr-actividad-service';
import type { QrActividad } from '../../asistencias/models';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaService } from '../../personal/persona/persona-service';
import { UsuarioService } from '../../seguridad/usuario-service';
import { Actividad, ActividadImagen, InscritoDetalle, ReporteActividad, urlImagenActividad } from '../models';
import { Persona } from '../../personal/persona/persona.model';
import { Usuario } from '../../seguridad/models';

@Component({
  selector: 'app-actividad-detail',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    TuiAvatar,
    TuiBadge,
    TuiButton,
    TuiButtonLoading,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiCalendar,
    TuiError,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiInputDate,
    TuiLabel,
    TuiTable,
    TuiTabs,
    TuiTextarea,
    TuiTextfield,
  ],
  templateUrl: './actividad-detail.html',
  styleUrl: './actividad-detail.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; email?: string } | null;
          return o?.nombre ?? o?.email ?? (typeof item === 'string' ? (item as string) : '');
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
export class ActividadDetail implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly agenda = inject(AgendaService);
  private readonly operacion = inject(OperacionService);
  private readonly qrActividad = inject(QrActividadService);
  private readonly inscripcionesSrv = inject(InscripcionService);
  private readonly configSrv = inject(ConfiguracionActividadService);
  private readonly reportes = inject(ReporteActividadService);
  private readonly personasSrv = inject(PersonaService);
  private readonly usuariosSrv = inject(UsuarioService);
  private readonly imagenSrv = inject(ActividadImagenService);
  private readonly confirmar = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  protected readonly router = inject(Router);

  protected readonly tabActivo = signal(0);
  protected readonly actividad = signal<Actividad | null>(null);
  protected readonly inscritos = signal<InscritoDetalle[]>([]);
  protected readonly totalInscritos = signal(0);
  protected readonly token = signal<QrActividad | null>(null);
  protected readonly reporte = signal<ReporteActividad | null>(null);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly loading = signal(false);
  protected readonly procesando = signal(false);
  protected readonly error = signal<string | null>(null);

  // Estados multimedia (Logo y Galería)
  protected readonly logoCargando = signal(false);
  protected readonly subiendoImagen = signal(false);
  protected readonly descripcionFoto = signal('');
  protected readonly fotoSeleccionada = signal<ActividadImagen | null>(null);
  protected readonly errorMultimedia = signal<string | null>(null);
  protected readonly imagenPendienteArchivo = signal<File | null>(null);
  protected readonly imagenPendientePreview = signal<string | null>(null);
  protected readonly mostrarModalSubida = signal<boolean>(false);

  /** Solo EN_CURSO permite consultar/rotar QR. Si no, modal y sin llamada backend. */
  protected readonly puedeUsarQr = computed(() => this.actividad()?.estado === 'EN_CURSO');

  protected readonly reprogramarForm = this.fb.nonNullable.group({
    nuevaFecha: ['', [Validators.required]],
    nuevaHoraInicio: ['', [Validators.required]],
    nuevaHoraFin: ['', [Validators.required]],
  });
  protected readonly cancelarForm = this.fb.nonNullable.group({
    motivo: ['', [Validators.required, Validators.maxLength(200)]],
  });
  protected readonly transferirCtrl = this.fb.nonNullable.control<Usuario | null>(null);
  protected readonly inscribirCtrl = this.fb.nonNullable.control<Persona | null>(null);
  protected readonly puntajeCtrl = this.fb.nonNullable.control<number | null>(null);
  protected readonly materialesCtrl = this.fb.nonNullable.control('');

  private id = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/actividades']);
        return;
      }
      this.id = Number(idParam);
      this.actividad.set(null);
      this.token.set(null);
      this.reporte.set(null);
      this.cargarTodo();
    });
    forkJoin({
      personas: this.personasSrv.listar({ estado: true }).pipe(catchError(() => of([]))),
      usuarios: this.usuariosSrv.buscar({ activo: true, size: 100 }).pipe(catchError(() => of(null))),
    }).subscribe(({ personas, usuarios }) => {
      this.personas.set(personas);
      if (usuarios) this.usuarios.set(usuarios.content);
    });
  }

  cargarTodo(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      act: this.agenda.obtener(this.id),
      rep: this.reportes.porActividad(this.id).pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ act, rep }) => {
        const insc = act.inscritos ?? [];
        this.actividad.set(act);
        this.inscritos.set(insc);
        this.totalInscritos.set(act.totalInscritos ?? insc.length);
        if (rep) this.reporte.set(rep);
        this.loading.set(false);
        this.cargarToken();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 404 ? 'Actividad no encontrada.' : 'No se pudo cargar.');
        this.loading.set(false);
      },
    });
  }

  private cargarToken(): void {
    // QR único (sistema QrActividad). Sin llamada si no está EN_CURSO (evita 404).
    if (!this.puedeUsarQr()) {
      this.token.set(null);
      return;
    }
    this.qrActividad.actual(this.id).pipe(catchError(() => of(null))).subscribe((t) => {
      if (t) this.token.set(t);
    });
  }

  // ---- Operaciones ----

  cambiarEstado(accion: 'iniciar' | 'finalizar'): void {
    const verbo = accion === 'iniciar' ? 'iniciar' : 'finalizar';
    this.confirmar.confirmar(`¿Seguro que deseas ${verbo} la actividad?`, 'Aceptar').subscribe((ok) => {
      if (!ok) return;
      this.procesando.set(true);
      this.operacion[accion](this.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.cargarTodo();
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? `No se pudo ${verbo}.`);
          this.procesando.set(false);
        },
      });
    });
  }

  cancelarActividad(): void {
    if (this.cancelarForm.invalid) {
      this.cancelarForm.markAllAsTouched();
      return;
    }
    const motivo = (this.cancelarForm.controls.motivo.value ?? '').trim();
    this.procesando.set(true);
    this.operacion.cancelar(this.id, motivo).subscribe({
      next: () => {
        this.procesando.set(false);
        this.cancelarForm.reset();
        this.cargarTodo();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo cancelar.');
        this.procesando.set(false);
      },
    });
  }

  protected fijarHoy(campo: string): void {
    fijarHoyEn(this.reprogramarForm.get(campo));
  }

  protected limpiarCampo(campo: string): void {
    limpiarFechaEn(this.reprogramarForm.get(campo));
  }

  reprogramar(): void {
    if (this.reprogramarForm.invalid) {
      this.reprogramarForm.markAllAsTouched();
      return;
    }
    const v = this.reprogramarForm.getRawValue();
    if (v.nuevaHoraInicio >= v.nuevaHoraFin) {
      this.error.set('La hora de inicio debe ser anterior a la de fin.');
      return;
    }
    this.procesando.set(true);
    this.operacion
      .reprogramar(this.id, {
        nuevaFecha: v.nuevaFecha,
        nuevaHoraInicio: v.nuevaHoraInicio,
        nuevaHoraFin: v.nuevaHoraFin,
      })
      .subscribe({
        next: () => {
          this.procesando.set(false);
          this.cargarTodo();
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudo reprogramar (solo programadas).');
          this.procesando.set(false);
        },
      });
  }

  transferir(): void {
    const u = this.transferirCtrl.value;
    if (!u) {
      this.error.set('Selecciona el nuevo coordinador.');
      return;
    }
    this.procesando.set(true);
    this.operacion.transferir(this.id, u.id).subscribe({
      next: () => {
        this.procesando.set(false);
        this.transferirCtrl.setValue(null);
        this.cargarTodo();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo transferir.');
        this.procesando.set(false);
      },
    });
  }

  rotarToken(): void {
    // Restricción frontend: modal de advertencia y sin llamada backend si no EN_CURSO.
    if (!this.puedeUsarQr()) {
      this.confirmar.advertirQrNoDisponible(this.actividad()?.estado).subscribe();
      return;
    }
    this.confirmar.confirmar('¿Rotar el token QR? El anterior quedará inválido.', 'Rotar').subscribe((ok) => {
      if (!ok) return;
      this.procesando.set(true);
      this.qrActividad.rotar(this.id).subscribe({
        next: (t) => {
          this.token.set(t);
          this.procesando.set(false);
        },
        error: () => {
          this.error.set('No se pudo rotar el token.');
          this.procesando.set(false);
        },
      });
    });
  }

  // ---- Inscripciones ----

  inscribirTodos(): void {
    this.confirmar
      .confirmar('¿Inscribir a todas las personas activas? Reemplaza inscritos previos.', 'Inscribir')
      .subscribe((ok) => {
        if (!ok) return;
        this.procesando.set(true);
        this.inscripcionesSrv.inscribirTodos(this.id).subscribe({
          next: () => {
            this.procesando.set(false);
            this.recargarInscritos();
          },
          error: () => {
            this.error.set('No se pudo inscribir a todos.');
            this.procesando.set(false);
          },
        });
      });
  }

  inscribirPersona(): void {
    const p = this.inscribirCtrl.value;
    if (!p) {
      this.error.set('Selecciona la persona a inscribir.');
      return;
    }
    this.procesando.set(true);
    this.inscripcionesSrv.inscribirPersonas(this.id, [p.id]).subscribe({
      next: () => {
        this.procesando.set(false);
        this.inscribirCtrl.setValue(null);
        this.recargarInscritos();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo inscribir.');
        this.procesando.set(false);
      },
    });
  }

  quitarInscrito(idPersona: number): void {
    this.procesando.set(true);
    this.inscripcionesSrv.eliminar(this.id, idPersona).subscribe({
      next: () => {
        this.procesando.set(false);
        this.recargarInscritos();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 404 ? 'Inscripción no encontrada.' : 'No se pudo quitar.');
        this.procesando.set(false);
      },
    });
  }

  private recargarInscritos(): void {
    this.agenda
      .obtener(this.id)
      .pipe(catchError(() => of(null)))
      .subscribe((act) => {
        if (!act) return;
        this.actividad.set(act);
        const insc = act.inscritos ?? [];
        this.inscritos.set(insc);
        this.totalInscritos.set(act.totalInscritos ?? insc.length);
      });
  }

  // ---- Configuración ----

  guardarMateriales(): void {
    const lista = this.materialesCtrl.value
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
    this.procesando.set(true);
    this.configSrv.asignarMateriales(this.id, lista).subscribe({
      next: () => {
        this.procesando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron guardar los materiales.');
        this.procesando.set(false);
      },
    });
  }

  guardarPuntaje(): void {
    const puntos = Number(this.puntajeCtrl.value);
    if (!Number.isInteger(puntos) || puntos < 0) {
      this.error.set('El puntaje debe ser un entero mayor o igual a 0.');
      return;
    }
    this.procesando.set(true);
    this.configSrv.asignarPuntaje(this.id, puntos).subscribe({
      next: () => {
        this.procesando.set(false);
      },
      error: () => {
        this.error.set('No se pudo guardar el puntaje.');
        this.procesando.set(false);
      },
    });
  }

  // ---- Logo y Galería ----

  protected resolverUrl(url?: string | null): string | undefined {
    return urlImagenActividad(url);
  }

  protected formatearTamano(bytes?: number): string {
    if (!bytes) return '';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  onLogoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    if (!archivo.type.startsWith('image/')) {
      this.errorMultimedia.set('El archivo seleccionado debe ser una imagen (PNG, JPG, WEBP).');
      input.value = '';
      return;
    }
    if (archivo.size > 2 * 1024 * 1024) {
      this.errorMultimedia.set(`El logo supera el límite de 2 MB (${this.formatearTamano(archivo.size)}).`);
      input.value = '';
      return;
    }
    this.confirmar
      .confirmar(`¿Deseas establecer "${archivo.name}" (${this.formatearTamano(archivo.size)}) como logo oficial?`, 'Subir logo')
      .subscribe((ok) => {
        if (!ok) {
          input.value = '';
          return;
        }
        this.logoCargando.set(true);
        this.errorMultimedia.set(null);
        this.imagenSrv.subirLogo(this.id, archivo).subscribe({
          next: (res) => {
            const act = this.actividad();
            if (act) {
              this.actividad.set({ ...act, logoUrl: res.logoUrl });
            }
            this.logoCargando.set(false);
            input.value = '';
          },
          error: (err: HttpErrorResponse) => {
            let msg = 'No se pudo subir el logo.';
            if (err.status === 0) {
              msg = 'Error de conexión (ERR_CONNECTION_RESET). El archivo puede exceder el límite permitido.';
            } else if (err.error?.message) {
              msg = err.error.message;
            }
            this.errorMultimedia.set(msg);
            this.logoCargando.set(false);
            input.value = '';
          },
        });
      });
  }

  eliminarLogo(): void {
    this.confirmar.confirmar('¿Seguro que deseas eliminar el logo de la actividad?', 'Eliminar').subscribe((ok) => {
      if (!ok) return;
      this.logoCargando.set(true);
      this.errorMultimedia.set(null);
      this.imagenSrv.eliminarLogo(this.id).subscribe({
        next: () => {
          const act = this.actividad();
          if (act) {
            this.actividad.set({ ...act, logoUrl: undefined });
          }
          this.logoCargando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMultimedia.set(err.error?.message ?? 'No se pudo eliminar el logo.');
          this.logoCargando.set(false);
        },
      });
    });
  }

  onImagenSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    const actuales = this.actividad()?.imagenes?.length ?? 0;
    if (actuales >= 10) {
      this.errorMultimedia.set('La actividad ya tiene el límite máximo de 10 imágenes.');
      input.value = '';
      return;
    }
    if (!archivo.type.startsWith('image/')) {
      this.errorMultimedia.set('El archivo seleccionado debe ser una imagen (JPG, PNG, WEBP).');
      input.value = '';
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.errorMultimedia.set(`La imagen supera el límite de 5 MB (${this.formatearTamano(archivo.size)}).`);
      input.value = '';
      return;
    }

    this.errorMultimedia.set(null);
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imagenPendientePreview.set(e.target?.result as string);
      this.imagenPendienteArchivo.set(archivo);
      this.mostrarModalSubida.set(true);
    };
    reader.readAsDataURL(archivo);
    input.value = '';
  }

  cancelarSubidaModal(): void {
    this.mostrarModalSubida.set(false);
    this.imagenPendienteArchivo.set(null);
    this.imagenPendientePreview.set(null);
    this.descripcionFoto.set('');
  }

  ejecutarSubidaImagen(): void {
    const archivo = this.imagenPendienteArchivo();
    if (!archivo) return;
    this.subiendoImagen.set(true);
    this.errorMultimedia.set(null);
    const desc = this.descripcionFoto().trim();
    this.imagenSrv.subirImagen(this.id, archivo, desc || undefined).subscribe({
      next: () => {
        // Recargamos desde backend para obtener orden consecutivo correcto
        this.recargarImagenes();
        this.subiendoImagen.set(false);
        this.cancelarSubidaModal();
      },
      error: (err: HttpErrorResponse) => {
        let msg = 'No se pudo subir la imagen a la galería.';
        if (err.status === 0) {
          msg = 'Error de conexión con el servidor (ERR_CONNECTION_RESET). Verifica que el backend esté en ejecución y que el archivo no supere el tamaño permitido.';
        } else if (err.error?.message) {
          msg = err.error.message;
        }
        this.errorMultimedia.set(msg);
        this.subiendoImagen.set(false);
      },
    });
  }

  eliminarImagen(idImagen: number): void {
    this.confirmar.confirmar('¿Seguro que deseas eliminar esta imagen de la galería?', 'Eliminar').subscribe((ok) => {
      if (!ok) return;
      this.errorMultimedia.set(null);
      // Cerrar lightbox si la foto eliminada estaba abierta
      if (this.fotoSeleccionada()?.idImagen === idImagen) {
        this.fotoSeleccionada.set(null);
      }
      this.imagenSrv.eliminarImagen(this.id, idImagen).subscribe({
        next: () => {
          // Recargamos desde backend para que el orden sea consecutivo (1, 2, 3…)
          this.recargarImagenes();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMultimedia.set(err.error?.message ?? 'No se pudo eliminar la imagen.');
        },
      });
    });
  }

  /** Recarga solo la lista de imágenes desde el backend y actualiza el signal local. */
  private recargarImagenes(): void {
    this.imagenSrv.listarImagenes(this.id)
      .pipe(catchError(() => of([])))
      .subscribe((imgs) => {
        const act = this.actividad();
        if (act) {
          this.actividad.set({ ...act, imagenes: imgs });
        }
      });
  }

  abrirFoto(img: ActividadImagen): void {
    this.fotoSeleccionada.set(img);
  }

  cerrarFoto(): void {
    this.fotoSeleccionada.set(null);
  }

  protected nombrePersona(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres].filter((x) => !!x).join(' ');
  }

  protected aparienciaEstado(e?: string): string {
    switch (e) {
      case 'EN_CURSO':
        return 'primary';
      case 'FINALIZADA':
        return 'positive';
      case 'CANCELADA':
        return 'negative';
      default:
        return 'warning';
    }
  }
}
