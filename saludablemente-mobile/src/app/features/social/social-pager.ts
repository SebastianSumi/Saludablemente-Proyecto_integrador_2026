import { Component, input, output } from '@angular/core';
import { TuiButton } from '@taiga-ui/core';
@Component({
  selector: 'social-pager',
  imports: [TuiButton],
  styleUrl: './social.css',
  template: ` @if (total() > 1) {
    <nav class="pager" aria-label="Paginación">
      <button
        tuiButton
        size="s"
        appearance="secondary"
        type="button"
        [disabled]="busy() || page() === 0"
        (click)="change.emit(page() - 1)"
      >
        Anterior</button
      ><span aria-live="polite">Página {{ page() + 1 }} de {{ total() }}</span
      ><button
        tuiButton
        size="s"
        appearance="secondary"
        type="button"
        [disabled]="busy() || page() + 1 >= total()"
        (click)="change.emit(page() + 1)"
      >
        Siguiente
      </button>
    </nav>
  }`,
})
export class SocialPager {
  readonly page = input(0);
  readonly total = input(0);
  readonly busy = input(false);
  readonly change = output<number>();
}
