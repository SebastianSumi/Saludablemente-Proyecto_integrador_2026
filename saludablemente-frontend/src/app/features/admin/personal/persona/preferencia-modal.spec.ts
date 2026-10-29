import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { POLYMORPHEUS_CONTEXT } from '@taiga-ui/polymorpheus';
import { PreferenciaModal } from './preferencia-modal';
import { PersonaService } from './persona-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { provideFormatosFecha } from '../../../../core/forms/fecha-transformers';

describe('PreferenciaModal', () => {
  let fixture: ComponentFixture<PreferenciaModal>;
  let completadoCon: unknown;
  let upsertLlamadoCon: { id: number; payload: unknown } | null;

  const GUARDADA = {
    id: 9,
    personaId: 3,
    canalPreferido: 'EMAIL',
    horarioContactoInicio: '08:00:00',
    horarioContactoFin: '17:00:00',
    aceptaRecordatorios: false,
  };

  beforeEach(async () => {
    completadoCon = undefined;
    upsertLlamadoCon = null;
    await TestBed.configureTestingModule({
      imports: [PreferenciaModal],
      providers: [
        ...provideFormatosFecha(),
        {
          provide: POLYMORPHEUS_CONTEXT,
          useValue: {
            data: { personaId: 3 },
            completeWith: (v: unknown) => {
              completadoCon = v;
            },
          },
        },
        {
          provide: PersonaService,
          useValue: {
            upsertPreferencias: (id: number, payload: unknown) => {
              upsertLlamadoCon = { id, payload };
              return of(GUARDADA);
            },
          },
        },
        { provide: ConfirmService, useValue: { confirmar: () => of(true) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PreferenciaModal);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('se crea', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('confirma, guarda y cierra con lo guardado', () => {
    const comp = fixture.componentInstance as unknown as {
      form: { controls: { canalPreferido: { setValue: (v: string) => void } } };
      guardar: () => void;
    };
    comp.form.controls.canalPreferido.setValue('EMAIL');
    comp.guardar();
    fixture.detectChanges();

    expect(upsertLlamadoCon).toBeTruthy();
    expect((upsertLlamadoCon as { id: number }).id).toBe(3);
    expect(completadoCon).toEqual(GUARDADA);
  });

  it('cancelar cierra con null sin guardar', () => {
    const comp = fixture.componentInstance;
    (comp as unknown as { cancelar: () => void }).cancelar();
    expect(completadoCon).toBeNull();
    expect(upsertLlamadoCon).toBeNull();
  });

  it('los horarios usan el input nativo de hora con sus valores', () => {
    const inicio = fixture.nativeElement.querySelector(
      'input[formControlName="horarioContactoInicio"]',
    ) as HTMLInputElement;
    const fin = fixture.nativeElement.querySelector(
      'input[formControlName="horarioContactoFin"]',
    ) as HTMLInputElement;
    expect(inicio?.type).toBe('time');
    expect(fin?.type).toBe('time');
    expect(inicio?.value).toBe('09:00');
    expect(fin?.value).toBe('18:00');
  });
});
