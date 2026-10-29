import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TeamService } from './team-service';
import { Team } from './team.model';

@Component({
  selector: 'app-mi-equipo',
  imports: [RouterLink],
  template: `
    <section class="card">
      <h2>Mi equipo</h2>
      @if (error()) { <p role="alert">{{ error() }}</p> }
      @if (equipo(); as team) {
        <div [style.border-left]="'6px solid ' + (team.color || '#2563EB')" style="padding:1rem">
          @if (team.imagenUrl) { <img [src]="team.imagenUrl" alt="Imagen del equipo" width="128" height="128" style="object-fit:contain" /> }
          <h3>{{ team.nombre }}</h3>
          <p>{{ team.slogan }}</p><p>{{ team.descripcion }}</p>
          <blockquote>{{ team.textoVersiculo }} <cite>{{ team.referenciaBiblica }}</cite></blockquote>
          <h4>Integrantes con cuenta activa</h4>
          @if (errorMiembros()) { <p role="alert">No se pudieron cargar los integrantes.</p> }
          <ul>
            @for (miembro of miembros(); track miembro.idUsuario) {
              <li>{{ miembro.nombre }} @if (miembro.idUsuario === team.idUsuarioLider) { <strong> · Líder</strong> }</li>
            }
          </ul>
          @if (!team.idUsuarioLider) { <p>Sin líder asignado.</p> }
          @if (puedeEditar()) { <a [routerLink]="['/teams', team.id, 'editar']">Editar perfil</a> }
        </div>
      } @else if (!error()) { <p>Cargando equipo…</p> }
    </section>
  `,
})
export class MiEquipo {
  protected readonly equipo = signal<Team | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly puedeEditar = signal(false);
  protected readonly miembros = signal<{ idUsuario: number; nombre: string }[]>([]);
  protected readonly errorMiembros = signal(false);
  constructor() {
    const service = inject(TeamService);
    service.miEquipo().subscribe({
      next: team => {
        this.equipo.set(team);
        service.puedeEditar(team.id).subscribe({ next: permiso => this.puedeEditar.set(permiso), error: () => this.puedeEditar.set(false) });
        service.miembrosMiEquipo().subscribe({ next: miembros => this.miembros.set(miembros), error: () => this.errorMiembros.set(true) });
      },
      error: () => this.error.set('No se pudo consultar tu equipo. Verifica que tengas uno activo asignado.'),
    });
  }
}
