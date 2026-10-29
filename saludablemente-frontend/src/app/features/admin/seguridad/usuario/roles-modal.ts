import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiCheckbox, TuiIcon, TuiLabel } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { UsuarioService } from '../usuario-service';
import { RolService } from '../rol-service';
import { Rol, Usuario } from '../models';

export interface RolesModalDatos {
  idUsuario: number;
  rolesIds: number[];
}

@Component({
  selector: 'app-roles-modal',
  imports: [FormsModule, ReactiveFormsModule, TuiBadge, TuiButton, TuiButtonLoading, TuiCheckbox, TuiIcon, TuiLabel],
  templateUrl: './roles-modal.html',
})
export class RolesModal implements OnInit {
  private readonly usuarios = inject(UsuarioService);
  private readonly rolesSrv = inject(RolService);
  private readonly context =
    injectContext<TuiDialogContext<Usuario | null, RolesModalDatos>>();

  protected readonly roles = signal<Rol[]>([]);
  protected readonly elegidos = signal<number[]>([...(this.context.data?.rolesIds ?? [])]);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.rolesSrv.listar().subscribe({
      next: (r) => this.roles.set(r),
      error: () => this.error.set('No se pudo cargar el catálogo de roles.'),
    });
  }

  protected toggle(id: number, marcado: boolean): void {
    this.elegidos.update((ids) =>
      marcado ? [...new Set([...ids, id])] : ids.filter((x) => x !== id)
    );
  }

  protected cancelar(): void {
    this.context.completeWith(null);
  }

  protected guardar(): void {
    if (this.elegidos().length === 0) {
      this.error.set('Selecciona al menos un rol.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.usuarios
      .actualizarRoles(this.context.data.idUsuario, { rolesIds: this.elegidos() })
      .subscribe({
        next: (u) => this.context.completeWith(u),
        error: (err: HttpErrorResponse) => {
          this.error.set(err.error?.message ?? 'No se pudieron actualizar los roles.');
          this.guardando.set(false);
        },
      });
  }
}
