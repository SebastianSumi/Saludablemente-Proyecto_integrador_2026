import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { TuiAvatar, TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { UsuarioService } from '../usuario-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Usuario } from '../models';
import { RolesModal } from './roles-modal';
import { PasswordModal } from './password-modal';

@Component({
  selector: 'app-usuario-detail',
  imports: [RouterLink, TuiAvatar, TuiBadge, TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './usuario-detail.html',
  styleUrl: './usuario-detail.css',
})
export class UsuarioDetail implements OnInit {
  private readonly service = inject(UsuarioService);
  private readonly confirmar = inject(ConfirmService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly route = inject(ActivatedRoute);
  protected readonly router = inject(Router);

  protected readonly usuario = signal<Usuario | null>(null);
  protected readonly loading = signal(false);
  protected readonly procesando = signal(false);
  protected readonly error = signal<string | null>(null);

  private id = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/seguridad/usuarios']);
        return;
      }
      this.id = Number(idParam);
      this.usuario.set(null);
      this.cargar();
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.obtener(this.id).subscribe({
      next: (u) => {
        this.usuario.set(u);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.status === 404 ? 'Usuario no encontrado.' : 'No se pudo cargar el usuario.');
        this.loading.set(false);
      },
    });
  }

  editarRoles(): void {
    const u = this.usuario();
    if (!u) return;
    this.dialogs
      .open<Usuario | null>(new PolymorpheusComponent(RolesModal), {
        label: 'Editar roles',
        data: { idUsuario: u.id, rolesIds: u.roles.map((r) => r.id) },
      })
      .subscribe({
        next: (actualizado) => {
          if (actualizado) this.usuario.set(actualizado);
        },
        complete: () => this.cargar(),
      });
  }

  cambiarPassword(): void {
    const u = this.usuario();
    if (!u) return;
    this.dialogs
      .open<boolean>(new PolymorpheusComponent(PasswordModal), {
        label: 'Cambiar contraseña',
        data: { idUsuario: u.id, email: u.email },
      })
      .subscribe();
  }

  inactivar(): void {
    const u = this.usuario();
    if (!u) return;
    this.confirmar
      .confirmar(`¿Inactivar la cuenta ${u.email}?`, 'Inactivar')
      .subscribe((ok) => {
        if (!ok) return;
        this.procesando.set(true);
        this.service.inactivar(u.id).subscribe({
          next: () => {
            this.procesando.set(false);
            this.cargar();
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo inactivar.');
            this.procesando.set(false);
          },
        });
      });
  }

  reactivar(): void {
    const u = this.usuario();
    if (!u) return;
    this.procesando.set(true);
    this.service.reactivar(u.id).subscribe({
      next: () => {
        this.procesando.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo reactivar.');
        this.procesando.set(false);
      },
    });
  }

  protected nombreCompleto(u: Usuario): string {
    return (
      [u.nombres, u.apellidoPaterno, u.apellidoMaterno].filter((x) => !!x).join(' ') || u.email
    );
  }

  protected iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .filter((x) => x.length > 0)
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('');
  }
}
