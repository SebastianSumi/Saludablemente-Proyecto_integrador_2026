import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPerimetroAbdominalForm } from './regla-perimetro-abdominal-form';

describe('ReglaPerimetroAbdominalForm', () => {
  let component: ReglaPerimetroAbdominalForm;
  let fixture: ComponentFixture<ReglaPerimetroAbdominalForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPerimetroAbdominalForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPerimetroAbdominalForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
