import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { UsoCardAlertaService } from '../../alertaclinica/uso-card-alerta-service';
import { AdminColor, AdminHijo, buscarModulo } from '../admin-modulos';

const COLOR_VAR: Record<AdminColor, string> = {
  primary: 'var(--sal-primary)',
  secondary: 'var(--sal-secondary)',
  success: 'var(--sal-success)',
  warning: 'var(--sal-warning)',
  error: 'var(--sal-danger)',
  violet: 'var(--sal-violet)',
};

type UsoModulo = Record<string, number>;

@Component({
  selector: 'app-admin-modulo',
  imports: [RouterLink, FormsModule, TuiButton, TuiIcon],
  templateUrl: './admin-modulo.html',
  styleUrl: './admin-modulo.css',
})
export class AdminModulo implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly usoService = inject(UsoCardAlertaService);

  protected readonly filtro = signal('');
  /** Frecuencia de uso por ruta (reordena los cards por preferencia). */
  protected readonly uso = signal<UsoModulo>({});

  protected get modulo() {
    return buscarModulo(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit(): void {
    if (!this.tieneDestacadas()) return;
    this.usoService.obtener().subscribe({
      next: (uso) => this.uso.set(uso ?? {}),
      error: () => this.uso.set({}),
    });
  }

  protected readonly hijas = computed(() => {
    const q = this.filtro().trim().toLowerCase();
    const todas = this.modulo?.hijos ?? [];
    if (!q) return todas;
    return todas.filter(
      (h) =>
        h.titulo.toLowerCase().includes(q) ||
        h.descripcion.toLowerCase().includes(q),
    );
  });

  /** El módulo define destacadas (layout cards). Independiente del filtro. */
  protected readonly tieneDestacadas = computed(() =>
    (this.modulo?.hijos ?? []).some((h) => h.destacado),
  );

  /** ¿Hay una preferencia guardada distinta del orden por defecto? */
  protected readonly hayUso = computed(() =>
    Object.values(this.uso()).some((n) => n > 0),
  );

  /** Hijos destacados bajo el filtro (cards grandes), ordenados por uso. */
  protected readonly destacadas = computed(() =>
    this.ordenarPorUso(this.hijas().filter((h) => h.destacado)),
  );

  /** Resto de hijos bajo el filtro (cards tamaño /admin), ordenados por uso. */
  protected readonly restantes = computed(() =>
    this.ordenarPorUso(this.hijas().filter((h) => !h.destacado)),
  );

  /** Ruta del card más usado dentro de un grupo (o null si nadie tiene uso). */
  protected masUsadoDe(lista: AdminHijo[]): string | null {
    const uso = this.uso();
    let mejor: string | null = null;
    let max = 0;
    for (const h of lista) {
      const n = uso[h.ruta] ?? 0;
      if (n > max) {
        max = n;
        mejor = h.ruta;
      }
    }
    return max > 0 ? mejor : null;
  }

  protected registrarUso(h: AdminHijo): void {
    const actual = this.uso();
    this.uso.set({ ...actual, [h.ruta]: (actual[h.ruta] ?? 0) + 1 });
    this.usoService.registrar(h.ruta).subscribe({ error: () => undefined });
  }

  protected restablecerOrden(): void {
    this.uso.set({});
    this.usoService.reiniciar().subscribe({ error: () => undefined });
  }

  protected sinImagen(event: Event): void {
    (event.target as HTMLImageElement | null)?.remove();
  }

  /** Color CSS del módulo, para la variable --modulo-color y las animaciones. */
  protected colorDe(color: AdminColor): string {
    return COLOR_VAR[color] ?? 'var(--sal-primary)';
  }

  /** Último segmento de la ruta, para el view-transition-name del icono. */
  protected slugDe(ruta: string): string {
    const partes = ruta.split('/').filter(Boolean);
    return partes.length ? partes[partes.length - 1] : '';
  }

  /** Ordena por uso descendente; empata por el orden original del módulo. */
  private ordenarPorUso(lista: AdminHijo[]): AdminHijo[] {
    const uso = this.uso();
    const indice = new Map(
      (this.modulo?.hijos ?? []).map((h, i) => [h.ruta, i] as const),
    );
    return [...lista].sort((a, b) => {
      const dif = (uso[b.ruta] ?? 0) - (uso[a.ruta] ?? 0);
      if (dif !== 0) return dif;
      return (indice.get(a.ruta) ?? 0) - (indice.get(b.ruta) ?? 0);
    });
  }
}
