import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TeamService } from './team-service';
import { Team } from './team.model';

@Component({
  selector: 'app-mi-equipo',
  imports: [RouterLink, FormsModule, TuiButton, TuiIcon],
  templateUrl: './mi-equipo.html',
  styleUrl: './mi-equipo.css',
})
export class MiEquipo {
  private readonly service = inject(TeamService);
  private readonly destroy = inject(DestroyRef);
  protected readonly equipo = signal<Team | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);
  protected readonly puedeEditar = signal(false);
  protected readonly miembros = signal<{ idUsuario: number; nombre: string }[]>([]);
  protected readonly errorMiembros = signal(false);
  protected readonly cargandoMiembros = signal(false);
  protected readonly imagenFallida = signal(false);
  protected readonly busqueda = signal('');
  protected readonly color = computed(() => /^#[\da-f]{6}$/i.test(this.equipo()?.color || '') ? this.equipo()!.color! : '#2563eb');
  protected readonly filtrados = computed(() => {
    const q = this.normalizar(this.busqueda());
    return this.miembros().filter(m => this.normalizar(m.nombre).includes(q))
      .sort((a, b) => Number(b.idUsuario === this.equipo()?.idUsuarioLider) - Number(a.idUsuario === this.equipo()?.idUsuarioLider) || a.nombre.localeCompare(b.nombre, 'es'));
  });
  constructor() { this.cargar(); }
  protected iniciales(nombre: string): string {
    return nombre.trim().split(/\s+/).slice(0, 2).map(p => p[0]).join('').toUpperCase() || '?';
  }
  private normalizar(texto: string): string {
    return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }
  protected cargar(): void {
    if (this.cargando()) return;
    this.cargando.set(true);
    this.error.set(null);
    this.puedeEditar.set(false);
    this.imagenFallida.set(false);
    this.service.miEquipo().pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: team => {
        this.equipo.set(team);
        this.cargando.set(false);
        this.service.puedeEditar(team.id).pipe(takeUntilDestroyed(this.destroy)).subscribe({
          next: permiso => this.puedeEditar.set(permiso), error: () => this.puedeEditar.set(false),
        });
        this.cargarMiembros();
      },
      error: () => {
        this.cargando.set(false);
        this.error.set('No pudimos consultar tu equipo. Verifica que tengas un equipo activo asignado o vuelve a intentarlo.');
      },
    });
  }
  protected cargarMiembros(): void {
    if (this.cargandoMiembros()) return;
    this.cargandoMiembros.set(true);
    this.errorMiembros.set(false);
    this.service.miembrosMiEquipo().pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: miembros => { this.miembros.set(miembros); this.cargandoMiembros.set(false); },
      error: () => { this.errorMiembros.set(true); this.cargandoMiembros.set(false); },
    });
  }
}
