import { Component, computed, input } from '@angular/core';
import { TuiIcon } from '@taiga-ui/core';

/** Iconos de interfaz: catálogo del sistema y dos glifos SVG locales de reacción. */
@Component({
  selector: 'social-icon',
  imports: [TuiIcon],
  host: { 'aria-hidden': 'true' },
  template: `@switch (name()) {
    @case ('surprised') {
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
      >
        <circle cx="12" cy="12" r="9" />
        <path d="M8 8h.01M16 8h.01" stroke-width="3" />
        <ellipse cx="12" cy="14.5" rx="2.1" ry="2.8" />
      </svg>
    }
    @case ('applause') {
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path
          d="m5 14-2-3a1.3 1.3 0 0 1 2-1.5l2 2-2-6a1.2 1.2 0 0 1 2.3-.7l2 5-1-7a1.2 1.2 0 0 1 2.4-.3l1 7 1-5a1.2 1.2 0 0 1 2.4.5l-1 7 2-2a1.3 1.3 0 0 1 2 1.7l-4 6a5 5 0 0 1-8-1Z"
        />
        <path d="m17 3 1-1m2 5h2m-3 4 2 1M7 21a7 7 0 0 0 11-3l2-4" />
      </svg>
    }
    @default {
      <tui-icon [icon]="icon()" />
    }
  }`,
  styles: [
    `
      :host {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 1.2rem;
        height: 1.2rem;
        flex-shrink: 0;
        vertical-align: middle;
      }
      svg,
      tui-icon {
        width: 100%;
        height: 100%;
        display: block;
      }
    `,
  ],
})
export class SocialIcon {
  readonly name = input.required<string>();
  readonly filled = input(false);
  protected readonly icon = computed(
    () =>
      `@tui.${this.name()}${this.filled() && ['thumbs-up', 'heart', 'bookmark'].includes(this.name()) ? '-filled' : ''}`,
  );
}
