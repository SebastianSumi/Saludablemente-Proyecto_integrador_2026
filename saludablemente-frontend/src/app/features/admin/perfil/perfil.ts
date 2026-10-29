import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge, TuiTabs } from '@taiga-ui/kit';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth-service';
import { ConfirmService } from '../../../core/services/confirm-service';
import { Avatar, urlAvatarFoto } from '../../../shared/components/avatar/avatar';
import { PerfilService } from './perfil-service';
import { UsuarioPerfil } from './perfil.model';
import { PersonaService } from '../personal/persona/persona-service';
import type {
  Persona,
  PreferenciaResponse,
} from '../personal/persona/persona.model';

@Component({
  selector: 'app-perfil',
  imports: [Avatar, DatePipe, RouterLink, TuiBadge, TuiButton, TuiIcon, TuiTabs],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  private readonly service = inject(PerfilService);
  private readonly personas = inject(PersonaService);
  private readonly confirmar = inject(ConfirmService);
  protected readonly auth = inject(AuthService);

  protected readonly perfil = signal<UsuarioPerfil | null>(null);
  protected readonly personalVivo = signal<Persona | null>(null);
  protected readonly preferencias = signal<PreferenciaResponse | null>(null);
  protected readonly fotoUrl = signal<string | undefined>(undefined);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly tabActivo = signal(0);
  protected readonly copiado = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.personalVivo.set(null);
    this.preferencias.set(null);

    this.service.obtener().subscribe({
      next: (data) => {
        this.perfil.set(data);
        this.componerPersonal(data.idPersona);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : err.error?.message ?? 'No se pudo cargar el perfil.'
        );
        this.loading.set(false);
      },
    });
  }

  private componerPersonal(idPersona: number): void {
    forkJoin({
      persona: this.personas.obtener(idPersona).pipe(catchError(() => of(null))),
      prefs: this.personas.obtenerPreferencias(idPersona).pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ persona, prefs }) => {
        this.personalVivo.set(persona);
        this.preferencias.set(prefs);
        this.fotoUrl.set(urlAvatarFoto(persona?.avatarUrl));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected iniciales(): string {
    const p = this.perfil();
    if (!p) return '';
    return p.nombreCompleto
      .split(' ')
      .filter((n) => n.length > 0)
      .slice(0, 2)
      .map((n) => n.charAt(0).toUpperCase())
      .join('');
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

  protected cerrarSesion(): void {
    this.auth.cerrarSesion();
  }

  protected cambiarFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    input.value = '';
    const persona = this.personalVivo();
    if (!archivo || !persona) return;
    this.confirmar
      .confirmar(`¿Subir "${archivo.name}" como tu foto de perfil?`, 'Subir foto')
      .subscribe((ok) => {
        if (!ok) return;
        this.loading.set(true);
        this.personas.subirAvatar(persona.id, archivo).subscribe({
          next: (p) => {
            this.personalVivo.set(p);
            this.fotoUrl.set(urlAvatarFoto(p.avatarUrl));
            this.loading.set(false);
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo subir la foto.');
            this.loading.set(false);
          },
        });
      });
  }

  protected quitarFoto(): void {
    const persona = this.personalVivo();
    if (!persona?.avatarUrl) return;
    this.confirmar
      .confirmar('¿Quitar tu foto de perfil?', 'Quitar')
      .subscribe((ok) => {
        if (!ok) return;
        this.loading.set(true);
        this.personas.eliminarAvatar(persona.id).subscribe({
          next: (p) => {
            this.personalVivo.set(p);
            this.fotoUrl.set(urlAvatarFoto(p.avatarUrl));
            this.loading.set(false);
          },
          error: () => {
            this.error.set('No se pudo quitar la foto.');
            this.loading.set(false);
          },
        });
      });
  }
}
