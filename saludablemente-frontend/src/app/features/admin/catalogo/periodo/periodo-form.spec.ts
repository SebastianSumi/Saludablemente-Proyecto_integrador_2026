import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { TuiDay } from '@taiga-ui/cdk/date-time';
import { PeriodoForm } from './periodo-form';

describe('PeriodoForm', () => {
  let component: PeriodoForm;
  let fixture: ComponentFixture<PeriodoForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PeriodoForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(PeriodoForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('fijarHoy pone la fecha actual en formato ISO', () => {
    component['fijarHoy']('fechaInicio');
    expect(component['form'].controls.fechaInicio.value).toBe(
      TuiDay.currentLocal().toJSON()
    );
    expect(component['form'].controls.fechaInicio.touched).toBe(true);
  });

  it('limpiarCampo vacía la fecha', () => {
    component['fijarHoy']('fechaFin');
    component['limpiarCampo']('fechaFin');
    expect(component['form'].controls.fechaFin.value).toBe('');
  });
});
