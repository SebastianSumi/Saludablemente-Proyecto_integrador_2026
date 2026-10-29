import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { EvaluacionForm } from './evaluacion-form';

describe('EvaluacionForm', () => {
  let component: EvaluacionForm;
  let fixture: ComponentFixture<EvaluacionForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(EvaluacionForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
