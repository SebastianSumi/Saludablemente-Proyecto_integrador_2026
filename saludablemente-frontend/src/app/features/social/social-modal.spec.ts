import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { SocialModal } from './social-modal';

@Component({
  imports: [TuiRoot, SocialModal],
  template: `<tui-root
    ><button (click)="open.set(true)">Abrir acción</button>
    @if (open()) {
      <social-modal title="Acción de Social" [busy]="busy()" (closed)="open.set(false)">
        <label>Texto<input aria-label="Texto de prueba" /></label>
        <button (click)="open.set(false)" [disabled]="busy()">Cancelar</button>
      </social-modal>
    }
  </tui-root>`,
})
class ModalHost {
  readonly open = signal(false);
  readonly busy = signal(false);
}

describe('Social · portal modal Taiga', () => {
  beforeEach(async () => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({ matches: false, addEventListener: () => {}, removeEventListener: () => {} }),
    });
    await TestBed.configureTestingModule({
      imports: [ModalHost],
      providers: [provideTaiga()],
    }).compileComponents();
  });
  it('monta una sola acción en un diálogo y la retira al cancelar', async () => {
    const fixture = TestBed.createComponent(ModalHost);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('button').click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(document.querySelectorAll('tui-dialog').length).toBe(1);
    expect(document.querySelector('tui-dialog')?.textContent).toContain('Acción de Social');
    const cancel = [...document.querySelectorAll('tui-dialog button')].find(
      (x) => x.textContent?.trim() === 'Cancelar',
    ) as HTMLButtonElement;
    cancel.click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.componentInstance.open()).toBe(false);
  });
  it('deshabilita el cierre de la acción mientras se guarda', async () => {
    const fixture = TestBed.createComponent(ModalHost);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.componentInstance.open.set(true);
    fixture.componentInstance.busy.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const cancel = [...document.querySelectorAll('tui-dialog button')].find(
      (x) => x.textContent?.trim() === 'Cancelar',
    ) as HTMLButtonElement;
    expect(cancel.disabled).toBe(true);
    cancel.click();
    expect(fixture.componentInstance.open()).toBe(true);
  });
});
