import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaGlucosaForm } from './regla-glucosa-form';

describe('ReglaGlucosaForm', () => {
  let component: ReglaGlucosaForm;
  let fixture: ComponentFixture<ReglaGlucosaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaGlucosaForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaGlucosaForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
