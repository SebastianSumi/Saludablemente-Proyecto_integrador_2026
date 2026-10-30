import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton } from '@taiga-ui/core';
import { Subject, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Sugerencia, TipoMencion } from './social.model';
@Component({
  selector: 'social-text-editor',
  imports: [FormsModule, TuiButton],
  styleUrl: './social.css',
  template: ` <textarea
      #area
      [attr.aria-label]="label()"
      [value]="value()"
      [maxLength]="max()"
      rows="4"
      (input)="valueChange.emit(area.value)"
      (keyup)="cursor = area.selectionStart"
      (click)="cursor = area.selectionStart"
      [disabled]="disabled()"
    ></textarea>
    <div class="actions">
      <small>{{ value().length }} / {{ max() }}</small
      ><button
        tuiButton
        type="button"
        appearance="flat"
        size="s"
        [disabled]="disabled()"
        (click)="open.set(!open())"
      >
        Mencionar
      </button>
    </div>
    @if (open()) {
      <div class="panel fields">
        <div class="two">
          <label
            >Tipo de mención<select [(ngModel)]="tipo" (ngModelChange)="search(query)">
              <option value="USUARIO">Persona</option>
              <option value="ACTIVIDAD">Actividad</option>
              <option value="EQUIPO">Equipo</option>
            </select></label
          ><label
            >Buscar por nombre<input
              [ngModel]="query"
              (ngModelChange)="search($event)"
              placeholder="Escribe al menos 2 caracteres"
          /></label>
        </div>
        @if (loading()) {
          <small role="status">Buscando…</small>
        }
        @if (error()) {
          <p role="alert">No se pudieron consultar las menciones.</p>
        }
        @for (item of suggestions(); track item.id) {
          <button tuiButton type="button" appearance="flat" size="s" (click)="insert(item, area)">
            {{ item.nombre }} · {{ item.token }}
          </button>
        }
        @if (!loading() && query.trim().length >= 2 && !error() && !suggestions().length) {
          <small>No se encontraron coincidencias.</small>
        }
      </div>
    }`,
})
export class SocialTextEditor {
  readonly value = input('');
  readonly valueChange = output<string>();
  readonly max = input(1000);
  readonly label = input('Contenido');
  readonly disabled = input(false);
  protected readonly open = signal(false);
  protected readonly suggestions = signal<Sugerencia[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);
  protected tipo: TipoMencion = 'USUARIO';
  protected query = '';
  protected cursor = 0;
  private readonly api = inject(SocialService);
  private readonly queries = new Subject<{ tipo: TipoMencion; q: string }>();
  constructor() {
    this.queries
      .pipe(
        tap(() => {
          this.suggestions.set([]);
          this.error.set(false);
          this.loading.set(true);
        }),
        debounceTime(250),
        switchMap((x) =>
          x.q.trim().length < 2
            ? of([])
            : this.api.sugerir(x.tipo, x.q.trim()).pipe(
                catchError(() => {
                  this.error.set(true);
                  return of([]);
                }),
              ),
        ),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((x) => {
        this.suggestions.set(x);
        this.loading.set(false);
      });
  }
  protected search(q: string) {
    this.query = q;
    this.queries.next({ tipo: this.tipo, q });
  }
  protected insert(item: Sugerencia, area: HTMLTextAreaElement) {
    const at = Math.min(this.cursor, this.value().length);
    const token = `${at && !/\s/.test(this.value()[at - 1]) ? ' ' : ''}${item.token} `;
    const next = this.value().slice(0, at) + token + this.value().slice(at);
    if (next.length > this.max()) return;
    this.valueChange.emit(next);
    this.cursor = at + token.length;
    this.open.set(false);
    area.focus();
    queueMicrotask(() => area.setSelectionRange(this.cursor, this.cursor));
  }
}
