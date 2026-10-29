import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaImcForm } from './regla-imc-form';

describe('ReglaImcForm', () => {
  let component: ReglaImcForm;
  let fixture: ComponentFixture<ReglaImcForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaImcForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaImcForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
