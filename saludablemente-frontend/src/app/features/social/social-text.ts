import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Mencion } from './social.model';
import { rutaMencion } from './social.utils';
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
  template: ` <div class="text">
      @for (part of parts(); track $index) {
        @if (part.tag) {
          <a [routerLink]="['/social/hashtags', part.tag]">{{ part.text }}</a>
        } @else {
          {{ part.text }}
        }
      }
    </div>
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
  readonly text = input<string | null>('');
  readonly mentions = input<Mencion[]>([]);
  protected readonly ruta = rutaMencion;
  protected readonly parts = computed(() => {
    const value = this.text() ?? '';
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
