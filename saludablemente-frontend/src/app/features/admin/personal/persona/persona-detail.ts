import { Component, DestroyRef, inject, signal, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield, TuiDialogService } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { Avatar, urlAvatarFoto } from '../../../../shared/components/avatar/avatar';
import { PersonaService } from './persona-service';
import { CredencialResponse, Persona, PreferenciaResponse } from './persona.model';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PreferenciaModal } from './preferencia-modal';

@Component({
  selector: 'app-persona-detail',
  imports: [Avatar, ReactiveFormsModule, RouterLink, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './persona-detail.html',
  styleUrl: './persona-detail.css',
})
export class PersonaDetail implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly personaService = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly id = signal<number>(0);
  protected readonly persona = signal<Persona | null>(null);
  protected readonly fotoUrl = signal<string | undefined>(undefined);
  protected readonly credencial = signal<CredencialResponse | null>(null);
  protected readonly preferencias = signal<PreferenciaResponse | null>(null);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly guardando = signal(false);
  protected readonly copiado = signal<string | null>(null);

  // Edición inline de celular
  protected readonly editandoCelular = signal(false);
  protected readonly celularForm = this.fb.nonNullable.group({
    celular: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{9,15}$/)]],
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/personal/personas']);
        return;
      }
      this.id.set(Number(idParam));
      this.persona.set(null);
      this.credencial.set(null);
      this.preferencias.set(null);
      this.editandoCelular.set(false);
      this.cargar();
      this.cargarPreferencias();
    });
  }

  // -------------------------------------------------------------------
  // Carga inicial
  // -------------------------------------------------------------------

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.personaService.obtener(this.id()).subscribe({
      next: (p) => {
        this.persona.set(p);
        this.fotoUrl.set(urlAvatarFoto(p.avatarUrl));
        this.celularForm.setValue({ celular: p.celular });
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.error.set('Persona no encontrada.');
        } else {
          this.error.set('No se pudo cargar la persona.');
        }
        this.loading.set(false);
      },
    });
  }

  private cargarPreferencias(): void {
    this.personaService.obtenerPreferencias(this.id()).subscribe({
      next: (p) => {
        this.preferencias.set(p);
      },
      error: () => {
        // 404 esperado si no hay preferencias. No es un error visible.
        this.preferencias.set(null);
      },
    });
  }

  configurarPreferencias(): void {
    const actual = this.preferencias();
    this.dialogs
      .open<PreferenciaResponse | null>(
        new PolymorpheusComponent(PreferenciaModal),
        {
          label: actual ? 'Editar preferencias' : 'Nueva preferencia',
          data: {
            personaId: this.id(),
            canalPreferido: actual?.canalPreferido ?? 'WHATSAPP',
            horarioContactoInicio: actual?.horarioContactoInicio ?? '09:00',
            horarioContactoFin: actual?.horarioContactoFin ?? '18:00',
            aceptaRecordatorios: actual?.aceptaRecordatorios ?? true,
          },
        }
      )
      .subscribe({
        next: (guardada) => {
          if (guardada) {
            this.preferencias.set(guardada);
          }
        },
        // Al cerrar el modal se recarga del backend: lo mostrado siempre
        // refleja lo guardado sin necesidad de recargar la página.
        complete: () => this.cargarPreferencias(),
      });
  }

  // -------------------------------------------------------------------
  // Cambio de celular
  // -------------------------------------------------------------------

  iniciarEdicionCelular(): void {
    this.editandoCelular.set(true);
  }

  cancelarEdicionCelular(): void {
    const p = this.persona();
    if (p) {
      this.celularForm.setValue({ celular: p.celular });
    }
    this.editandoCelular.set(false);
  }

  guardarCelular(): void {
    const ctrl = this.celularForm.controls.celular;
    ctrl.setValue(ctrl.value.trim());
    if (this.celularForm.invalid) {
      this.celularForm.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    this.personaService.cambiarCelular(this.id(), this.celularForm.getRawValue()).subscribe({
      next: (p) => {
        this.persona.set(p);
        this.editandoCelular.set(false);
        this.guardando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.error.set(err.error?.message ?? 'Celular duplicado.');
        } else {
          this.error.set('No se pudo actualizar el celular.');
        }
        this.guardando.set(false);
      },
    });
  }

  // -------------------------------------------------------------------
  // Credencial QR
  // -------------------------------------------------------------------

  generarCredencial(): void {
    const p = this.persona();
    if (!p) return;

    const esRotacion = this.credencial() !== null;
    const mensaje = esRotacion
      ? `¿Rotar la credencial de ${this.nombreCompleto(p)}? La credencial anterior quedará anulada.`
      : `¿Generar credencial QR para ${this.nombreCompleto(p)}?`;

    this.confirmar.confirmar(mensaje, esRotacion ? 'Rotar' : 'Generar').subscribe((ok) => {
      if (!ok) return;
      this.guardando.set(true);
      this.error.set(null);

      this.personaService.generarCredencial(this.id(), true).subscribe({
        next: (c) => {
          this.credencial.set(c);
          this.guardando.set(false);
        },
        error: () => {
          this.error.set('No se pudo generar la credencial.');
          this.guardando.set(false);
        },
      });
    });
  }

  // -------------------------------------------------------------------
  // Inactivar / Reactivar
  // -------------------------------------------------------------------

  inactivar(): void {
    const p = this.persona();
    if (!p) return;

    this.confirmar
      .confirmar(`¿Inactivar a ${this.nombreCompleto(p)}? Su credencial QR será anulada.`, 'Inactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.personaService.inactivar(this.id()).subscribe({
          next: () => this.router.navigate(['/personal/personas']),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo inactivar la persona.');
            this.guardando.set(false);
          },
        });
      });
  }

  reactivar(): void {
    const p = this.persona();
    if (!p) return;

    this.confirmar
      .confirmar(`¿Reactivar a ${this.nombreCompleto(p)}? Se emitirá una nueva credencial QR.`, 'Reactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.personaService.reactivar(this.id()).subscribe({
          next: () => this.router.navigate(['/personal/personas']),
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo reactivar la persona.');
            this.guardando.set(false);
          },
        });
      });
  }

  // -------------------------------------------------------------------
  // Foto de perfil
  // -------------------------------------------------------------------

  cambiarFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    input.value = '';
    const p = this.persona();
    if (!archivo || !p) return;
    this.confirmar
      .confirmar(`¿Subir "${archivo.name}" como foto de ${this.nombreCompleto(p)}?`, 'Subir foto')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        this.personaService.subirAvatar(p.id, archivo).subscribe({
          next: (actualizada) => {
            this.persona.set(actualizada);
            this.fotoUrl.set(urlAvatarFoto(actualizada.avatarUrl));
            this.guardando.set(false);
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo subir la foto.');
            this.guardando.set(false);
          },
        });
      });
  }

  quitarFoto(): void {
    const p = this.persona();
    if (!p || (!p.avatarUrl && !this.fotoUrl())) return;
    this.confirmar
      .confirmar(`¿Quitar la foto de ${this.nombreCompleto(p)}?`, 'Quitar')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.personaService.eliminarAvatar(p.id).subscribe({
          next: (actualizada) => {
            this.persona.set(actualizada);
            this.fotoUrl.set(urlAvatarFoto(actualizada.avatarUrl));
            this.guardando.set(false);
          },
          error: () => {
            this.error.set('No se pudo quitar la foto.');
            this.guardando.set(false);
          },
        });
      });
  }

  // -------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------

  protected reintentar(): void {
    this.cargar();
    this.cargarPreferencias();
  }

  protected celularTelLink(p: Persona): string {
    return `tel:${p.celular.replace(/\s/g, '')}`;
  }

  protected copiar(texto: string, clave: string): void {
    if (!texto) return;
    const done = () => {
      this.copiado.set(clave);
      window.setTimeout(() => {
        if (this.copiado() === clave) this.copiado.set(null);
      }, 1600);
    };
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(texto).then(done).catch(done);
    } else {
      done();
    }
  }

  protected descargarQR(): void {
    const c = this.credencial();
    const p = this.persona();
    if (!c?.qrBase64) return;
    const a = document.createElement('a');
    a.href = 'data:image/png;base64,' + c.qrBase64;
    a.download = `credencial-${p?.documentoIdentidad ?? c.personaId}.png`;
    document.body.appendChild(a);
    a.click();
    a.remove();
  }

  protected iniciales(p: Persona): string {
    const n = (p.nombres || '').trim()[0] || '';
    const a = (p.apellidoPaterno || '').trim()[0] || '';
    return (n + a).toUpperCase() || 'P';
  }

  protected nombreCompleto(p: Persona): string {
    return [p.apellidoPaterno, p.apellidoMaterno, p.nombres]
      .filter((x) => !!x)
      .join(' ');
  }

  protected fechaFormateada(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  protected fechaHoraFormateada(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleString('es-PE', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
