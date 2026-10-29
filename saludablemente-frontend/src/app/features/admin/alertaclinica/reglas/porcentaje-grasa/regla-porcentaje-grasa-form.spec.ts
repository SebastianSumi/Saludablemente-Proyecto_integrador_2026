import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPorcentajeGrasaForm } from './regla-porcentaje-grasa-form';

describe('ReglaPorcentajeGrasaForm', () => {
  let component: ReglaPorcentajeGrasaForm;
  let fixture: ComponentFixture<ReglaPorcentajeGrasaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPorcentajeGrasaForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPorcentajeGrasaForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
