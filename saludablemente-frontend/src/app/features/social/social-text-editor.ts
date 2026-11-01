import { SocialIcon } from './social-icon';
import {
  Component,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialModal } from './social-modal';
import {
  editMentionDraft,
  encodeMentions,
  insertMention,
  mentionDraft,
} from './social-mention-draft';
import { FormsModule } from '@angular/forms';
import { TuiButton } from '@taiga-ui/core';
import { Subject, catchError, of, switchMap, tap, timer } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Mencion, Sugerencia, TipoMencion } from './social.model';
@Component({
  selector: 'social-text-editor',
  imports: [SocialIcon, SocialModal, Avatar, FormsModule, TuiButton],
  styleUrls: ['./social.css', './social-text-editor.css'],
  template: ` <div class="text-box" [class.compact]="compact()">
      <textarea
        #area
        [attr.aria-label]="label()"
        placeholder="Comparte una idea con tu comunidad…"
        [value]="draft().text"
        [rows]="compact() ? 2 : 4"
        (input)="onInput(area)"
        (keyup)="remember(area)"
        (click)="remember(area)"
        (select)="remember(area)"
        [disabled]="disabled()"
      ></textarea>
      <ng-content select="[editor-submit]" />
    </div>
    <div class="actions editor-toolbar" [class.compact-toolbar]="compact()">
      <small>{{ value().length }} / {{ max() }}</small
      ><button
        tuiButton
        type="button"
        appearance="flat"
        size="s"
        [disabled]="disabled()"
        (click)="remember(area); open.set(true)"
      >
        <social-icon name="at-sign" />Mencionar
      </button>
      <button
        tuiButton
        type="button"
        appearance="flat"
        size="s"
        [disabled]="disabled()"
        (click)="insertHashtag(area)"
      >
        <social-icon name="hash" />Hashtag
      </button>
      <ng-content select="[editor-tools]" />
    </div>
    @if (!compact()) {
      <small class="editor-help"
        >Usa #Salud para agrupar temas o menciona a una persona de tu comunidad.</small
      >
    }
    @if (limitError()) {
      <small class="error" role="alert"
        >La mención supera el límite de {{ max() }} caracteres.</small
      >
    }
    @if (open()) {
      <social-modal title="Mencionar" (closed)="closePicker()">
        <section class="mention-picker fields">
          <nav class="tabs mention-types" aria-label="Tipo de mención">
            @for (t of types; track t.value) {
              <button
                type="button"
                [attr.aria-pressed]="tipo === t.value"
                [class.active]="tipo === t.value"
                (click)="tipo = t.value; search(query)"
              >
                <social-icon [name]="t.icon" />{{ t.label }}
              </button>
            }
          </nav>
          <label class="mention-search"
            >Buscar por nombre<input
              type="search"
              autocomplete="off"
              aria-label="Buscar mención por nombre"
              [ngModelOptions]="{ standalone: true }"
              [ngModel]="query"
              (ngModelChange)="search($event)"
              placeholder="Escribe al menos 2 caracteres"
          /></label>
          <div class="mention-result-heading">
            <small>Selecciona para mencionar</small>
            <div role="group" aria-label="Vista de resultados">
              <button
                tuiButton
                appearance="flat"
                type="button"
                size="s"
                aria-label="Ver en lista"
                [attr.aria-pressed]="view() === 'list'"
                (click)="view.set('list')"
              >
                <social-icon name="list" />
              </button>
              <button
                tuiButton
                appearance="flat"
                type="button"
                size="s"
                aria-label="Ver en tarjetas"
                [attr.aria-pressed]="view() === 'cards'"
                (click)="view.set('cards')"
              >
                <social-icon name="grid-2x2" />
              </button>
            </div>
          </div>
          @if (loading()) {
            <small role="status">Buscando…</small>
          }
          @if (error()) {
            <p role="alert">No se pudieron consultar las menciones.</p>
          }
          @if (limitError()) {
            <p class="error" role="alert">La mención supera el límite de {{ max() }} caracteres.</p>
          }
          <div class="mention-results" [class.cards]="view() === 'cards'">
            @for (item of suggestions(); track item.tipo + item.id) {
              <button class="mention-result" type="button" (click)="insert(item, area)">
                <app-avatar
                  [foto]="avatar(item.imagenUrl)"
                  [iniciales]="item.nombre.slice(0, 1)"
                  [shape]="item.tipo === 'USUARIO' ? 'circle' : 'square'"
                  [fit]="item.tipo === 'USUARIO' ? 'cover' : 'contain'"
                  size="l"
                />
                <span class="mention-copy"
                  ><strong>{{ item.nombre }}</strong
                  ><small>{{ item.detalle || typeLabel(item.tipo) }}</small></span
                >
                <social-icon name="plus" />
              </button>
            }
          </div>
          @if (query.trim().length < 2) {
            <p class="empty">Busca una persona, actividad o equipo por su nombre.</p>
          }
          @if (!loading() && query.trim().length >= 2 && !error() && !suggestions().length) {
            <small>No se encontraron coincidencias.</small>
          }
        </section>
      </social-modal>
    }`,
})
export class SocialTextEditor {
  readonly mentions = input<Mencion[]>([]);
  protected readonly draft = signal(mentionDraft(''));
  protected readonly view = signal<'list' | 'cards'>('list');
  protected readonly avatar = urlAvatarFoto;
  protected readonly limitError = signal(false);
  protected readonly types = [
    { value: 'USUARIO' as const, label: 'Personas', icon: 'users' },
    { value: 'ACTIVIDAD' as const, label: 'Actividades', icon: 'calendar-days' },
    { value: 'EQUIPO' as const, label: 'Equipos', icon: 'flag' },
  ];
  protected typeLabel(type: TipoMencion) {
    return this.types.find((t) => t.value === type)?.label;
  }
  readonly compact = input(false);
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
  private selectionEnd = 0;
  private readonly api = inject(SocialService);
  private readonly queries = new Subject<{ tipo: TipoMencion; q: string }>();
  constructor() {
    effect(() => {
      const raw = this.value(),
        mentions = this.mentions();
      if (raw !== encodeMentions(untracked(this.draft)))
        this.draft.set(mentionDraft(raw, mentions));
    });
    this.queries
      .pipe(
        tap(() => {
          this.suggestions.set([]);
          this.error.set(false);
          this.loading.set(true);
        }),
        switchMap((x) =>
          x.q.trim().length < 2
            ? of([])
            : timer(250).pipe(
                switchMap(() => this.api.sugerir(x.tipo, x.q.trim())),
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
    const next = insertMention(
      this.draft(),
      this.cursor,
      this.selectionEnd,
      item.nombre,
      item.token,
    );
    if (encodeMentions(next).length > this.max()) {
      this.limitError.set(true);
      return;
    }
    this.draft.set(next);
    this.valueChange.emit(encodeMentions(next));
    this.cursor = next.ranges.at(-1)!.end + 1;
    this.selectionEnd = this.cursor;
    this.closePicker();
    area.focus();
    queueMicrotask(() => area.setSelectionRange(this.cursor, this.cursor));
  }
  protected insertHashtag(area: HTMLTextAreaElement) {
    this.remember(area);
    const at = this.cursor;
    const token = at && !/\s/.test(this.draft().text[at - 1]) ? ' #' : '#';
    const next = editMentionDraft(
      this.draft(),
      this.draft().text.slice(0, at) + token + this.draft().text.slice(this.selectionEnd),
    );
    if (encodeMentions(next).length > this.max()) return;
    this.draft.set(next);
    this.valueChange.emit(encodeMentions(next));
    this.cursor = at + token.length;
    area.focus();
    queueMicrotask(() => area.setSelectionRange(this.cursor, this.cursor));
  }
  protected remember(area: HTMLTextAreaElement) {
    this.cursor = area.selectionStart;
    this.selectionEnd = area.selectionEnd;
  }
  protected onInput(area: HTMLTextAreaElement) {
    const next = editMentionDraft(this.draft(), area.value);
    if (encodeMentions(next).length > this.max()) {
      area.value = this.draft().text;
      return;
    }
    this.limitError.set(false);
    this.draft.set(next);
    this.remember(area);
    this.valueChange.emit(encodeMentions(next));
  }
  protected closePicker() {
    this.open.set(false);
    this.search('');
    this.limitError.set(false);
  }
}
