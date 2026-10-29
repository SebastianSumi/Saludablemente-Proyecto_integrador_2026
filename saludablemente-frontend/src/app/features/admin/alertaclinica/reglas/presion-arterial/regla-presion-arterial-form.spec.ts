import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPresionArterialForm } from './regla-presion-arterial-form';

describe('ReglaPresionArterialForm', () => {
  let component: ReglaPresionArterialForm;
  let fixture: ComponentFixture<ReglaPresionArterialForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPresionArterialForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPresionArterialForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
