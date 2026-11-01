import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Mencion } from './social.model';
import { rutaMencion } from './social.utils';
import { mentionDraft } from './social-mention-draft';
@Component({
  selector: 'social-text',
  imports: [RouterLink],
  styles: [
    `
      .text {
        white-space: pre-wrap;
        overflow-wrap: anywhere;
        line-height: 1.7;
      }
      .collapsed {
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      .read-more {
        border: 0;
        background: none;
        padding: 4px 0;
        font: inherit;
        font-weight: 600;
        color: var(--sal-action);
        cursor: pointer;
      }
      .tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        margin-top: 8px;
      }
      a {
        color: var(--sal-action);
        text-decoration: none;
      }
      .mention {
        padding: 3px 8px;
        border: 1px solid var(--sal-line);
        border-radius: 8px;
        font-size: 12px;
      }
    `,
  ],
  template: ` <div class="text" [class.collapsed]="truncate() && !expanded() && isLong()">
      @for (part of parts(); track $index) {
        @if (part.tag) {
          <a [routerLink]="['/social/hashtags', part.tag]">{{ part.text }}</a>
        } @else {
          {{ part.text }}
        }
      }
    </div>
    @if (truncate() && !expanded() && isLong()) {
      <button class="read-more" type="button" (click)="expanded.set(true)" aria-expanded="false">
        Ver más
      </button>
    }
    @if (mentions().length) {
      <div class="tags" aria-label="Menciones">
        @for (m of mentions(); track $index) {
          <a class="mention" [routerLink]="ruta(m.tipoMencion, m.refId)">{{
            m.displayName || 'Contenido no disponible'
          }}</a>
        }
      </div>
    }`,
})
export class SocialText {
  protected readonly isLong = computed(() => {
    const value = mentionDraft(this.text() ?? '', this.mentions()).text;
    return value.length > 220 || value.split('\n').length > 3;
  });
  readonly truncate = input(false);
  protected readonly expanded = signal(false);
  readonly text = input<string | null>('');
  readonly mentions = input<Mencion[]>([]);
  protected readonly ruta = rutaMencion;
  protected readonly parts = computed(() => {
    const value = mentionDraft(this.text() ?? '', this.mentions()).text;
    const regex = /(?<![\p{L}\p{N}_])#([a-z0-9_áéíóúñ]{2,50})(?![\p{L}\p{N}_])/giu;
    const parts: { text: string; tag: string | null }[] = [];
    let last = 0;
    for (const match of value.matchAll(regex)) {
      const index = match.index;
      parts.push(
        { text: value.slice(last, index), tag: null },
        { text: match[0], tag: match[1].toLowerCase() },
      );
      last = index + match[0].length;
    }
    parts.push({ text: value.slice(last), tag: null });
    return parts;
  });
}
