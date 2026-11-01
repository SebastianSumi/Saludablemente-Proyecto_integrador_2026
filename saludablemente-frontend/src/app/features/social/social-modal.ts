import {
  Component,
  ViewEncapsulation,
  computed,
  HostListener,
  inject,
  input,
  output,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { toObservable } from '@angular/core/rxjs-interop';
import { TuiDialog } from '@taiga-ui/core';
let nextModalId = 0;

/** Un único portal Taiga: foco contenido, fondo bloqueado y retorno al disparador. */
@Component({
  selector: 'social-modal',
  imports: [TuiDialog],
  encapsulation: ViewEncapsulation.None,
  styleUrl: './social-modal.css',
  template: `
    <ng-template
      [tuiDialog]="true"
      [tuiDialogOptions]="{
        label: title(),
        size: wide() ? 'l' : 'm',
        appearance: wide() ? 'taiga social-comments-dialog' : 'taiga',
        closable: canClose,
        dismissible: canClose,
      }"
      (tuiDialogChange)="onOpenChange($event)"
    >
      <div class="social-modal-body" [id]="bodyId"><ng-content /></div>
    </ng-template>
  `,
})
export class SocialModal {
  private readonly document = inject(DOCUMENT);
  protected readonly bodyId = `social-modal-${++nextModalId}`;
  readonly title = input.required<string>();
  readonly busy = input(false);
  readonly wide = input(false);
  readonly closed = output<void>();
  protected readonly canClose = toObservable(computed(() => !this.busy()));
  protected onOpenChange(open: boolean) {
    if (!open && !this.busy()) this.closed.emit();
  }
  @HostListener('document:keydown', ['$event'])
  protected keepTabInside(event: KeyboardEvent) {
    if (event.key !== 'Tab') return;
    const modal = this.document.getElementById(this.bodyId)?.closest('tui-modal');
    if (!modal || !modal.contains(this.document.activeElement)) return;
    const controls = [
      ...modal.querySelectorAll<HTMLElement>(
        'button, input, textarea, select, a[href], [tabindex]',
      ),
    ].filter(
      (el) =>
        el.tabIndex >= 0 &&
        !el.matches(':disabled, input[type=hidden]') &&
        el.getClientRects().length > 0,
    );
    const first = controls[0],
      last = controls.at(-1);
    if (!first || !last) return;
    if (event.shiftKey && this.document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && this.document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }
}
