import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { POLYMORPHEUS_CONTEXT } from '@taiga-ui/polymorpheus';
import { RolesModal } from './roles-modal';
import { RolService } from '../rol-service';

@Component({
  template: '<tui-root><app-roles-modal /></tui-root>',
  imports: [TuiRoot, RolesModal],
})
class Host {}

describe('RolesModal selección', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({
        matches: false,
        addEventListener: () => undefined,
        removeEventListener: () => undefined,
      }),
    });
    (document as unknown as { execCommand: () => boolean }).execCommand = () => false;
    await TestBed.configureTestingModule({
      imports: [RolesModal],
      providers: [
        provideTaiga(),
        {
          provide: POLYMORPHEUS_CONTEXT,
          useValue: { data: { idUsuario: 1, rolesIds: [] }, completeWith: () => undefined },
        },
        {
          provide: RolService,
          useValue: {
            listar: () => of([{ id: 1, nombre: 'Administrador' }, { id: 2, nombre: 'Colaborador' }]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('clic en checkbox alterna la selección', async () => {
    const boxes = Array.from(
      fixture.nativeElement.querySelectorAll('input[type="checkbox"]')
    ) as HTMLInputElement[];
    expect(boxes.length).toBe(2);

    boxes[0].click();
    fixture.detectChanges();
    await fixture.whenStable();

    const comp = fixture.debugElement.query(By.directive(RolesModal))
      .componentInstance as unknown as {
      elegidos: () => number[];
    };
    // eslint-disable-next-line no-console
    console.log('elegidos tras clic:', JSON.stringify(comp.elegidos()));
    expect(comp.elegidos()).toEqual([1]);

    boxes[0].click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(comp.elegidos()).toEqual([]);
  });
});