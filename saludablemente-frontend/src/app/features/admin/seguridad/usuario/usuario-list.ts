import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, TuiInput, TuiLabel, TuiTextfield, tuiItemsHandlersProvider } from '@taiga-ui/core';
import {
  TuiAvatar,
  TuiBadge,
  TuiChevron,
  TuiComboBox,
  TuiDataListWrapper,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { UsuarioService } from '../usuario-service';
import { Usuario } from '../models';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-usuario-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiAvatar,
    TuiButton,
    TuiBadge,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './usuario-list.html',
  styleUrl: './usuario-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => (item as { label?: string } | null)?.label ?? ''
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null)
      ),
    }),
  ],
})
export class UsuarioList implements OnInit {
  private readonly service = inject(UsuarioService);

  protected readonly items = signal<Usuario[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly emailFiltro = signal('');

  protected readonly estadoOpts: Opcion[] = [
    { id: 'todos', label: 'Todos' },
    { id: 'activos', label: 'Solo activos' },
    { id: 'inactivos', label: 'Solo inactivos' },
  ];

  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);
  protected readonly emailCtrl = new FormControl<Opcion | null>(null);

  protected readonly emailsSugeridos = computed<Opcion[]>(() => [
    { id: '', label: 'Todos' },
    ...[...new Set(this.items().map((u) => u.email))].map((email) => ({
      id: email,
      label: email,
    })),
  ]);

  ngOnInit(): void {
    this.estadoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });
    // Solo filtra la tabla al ELEGIR una opción (escribir solo angosta el desplegable).
    this.emailCtrl.valueChanges.subscribe((o) => {
      this.emailFiltro.set(o?.id ?? '');
      this.page.set(0);
      this.cargar();
    });
    this.cargar();
  }

  /** Enter con texto libre: filtra por ese texto aunque no se haya elegido opción. */
  aplicarEmailTexto(texto: string): void {
    const valor = (texto ?? '').trim();
    if (valor === '') {
      this.emailCtrl.setValue(null, { emitEvent: false });
      this.emailFiltro.set('');
      this.page.set(0);
      this.cargar();
      return;
    }
    const exacto = this.emailsSugeridos().find(
      (o) => o.label.toLowerCase() === valor.toLowerCase()
    );
    if (exacto && this.emailCtrl.value?.id !== exacto.id) {
      this.emailCtrl.setValue(exacto);
      return;
    }
    if (!exacto) {
      this.emailFiltro.set(valor);
      this.page.set(0);
      this.cargar();
    }
  }

  limpiarFiltros(): void {
    this.emailFiltro.set('');
    this.estadoCtrl.setValue(null, { emitEvent: false });
    this.emailCtrl.setValue(null, { emitEvent: false });
    this.page.set(0);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    const id = this.estadoCtrl.value?.id ?? 'todos';
    this.service
      .buscar({
        email: this.emailFiltro() || undefined,
        activo: id === 'todos' ? undefined : id === 'activos',
        page: this.page(),
        size: 20,
      })
      .subscribe({
        next: (p) => {
          this.items.set(p.content);
          this.total.set(p.totalElements);
          this.totalPages.set(Math.max(1, p.totalPages));
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar los usuarios.'
          );
          this.loading.set(false);
        },
      });
  }

  paginaAnterior(): void {
    if (this.page() > 0) {
      this.page.update((v) => v - 1);
      this.cargar();
    }
  }

  paginaSiguiente(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((v) => v + 1);
      this.cargar();
    }
  }

  protected nombreUsuario(u: Usuario): string {
    const nombre = [u.nombres, u.apellidoPaterno, u.apellidoMaterno]
      .filter((x) => !!x)
      .join(' ');
    return nombre || u.email;
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
