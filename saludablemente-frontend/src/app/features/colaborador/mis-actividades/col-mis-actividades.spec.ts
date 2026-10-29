import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColMisActividades } from './col-mis-actividades';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { AgendaService } from '../../admin/actividades/agenda-service';
import { TuiDayRange, TuiDay } from '@taiga-ui/cdk/date-time';

describe('ColMisActividades', () => {
  let fixture: ComponentFixture<ColMisActividades>;
  let component: ColMisActividades;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColMisActividades],
      providers: [
        provideRouter([]),
        {
          provide: PerfilService,
          useValue: {
            obtener: () => of({ idPersona: 7 }),
          },
        },
        {
          provide: AgendaService,
          useValue: {
            listar: () =>
              of([
                {
                  idActividad: 1,
                  nombre: 'Actividad 1',
                  fechaActividad: '2026-10-01',
                  horaInicio: '08:00',
                  horaFin: '10:00',
                  lugar: 'Sala A',
                  estado: 'PROGRAMADA',
                  idUsuarioCoordinador: 7,
                },
                {
                  idActividad: 2,
                  nombre: 'Actividad 2',
                  fechaActividad: '2026-10-15',
                  horaInicio: '14:00',
                  horaFin: '16:00',
                  lugar: 'Sala B',
                  estado: 'EN_CURSO',
                  idUsuarioCoordinador: 999,
                },
              ]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ColMisActividades);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render activities assigned to the logged-in user', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Actividad 1');
    expect(el.textContent).not.toContain('Actividad 2');
  });

  it('should filter by date range', () => {
    const comp = fixture.componentInstance as ColMisActividades;
    comp.rangoCtrl.setValue(
      new TuiDayRange(
        TuiDay.fromLocalNativeDate(new Date('2026-09-30')),
        TuiDay.fromLocalNativeDate(new Date('2026-10-02'))
      )
    );
    comp.filtrarPorRango(comp.rangoCtrl.value);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Actividad 1');
    expect(el.textContent).not.toContain('Actividad 2');
  });
});
