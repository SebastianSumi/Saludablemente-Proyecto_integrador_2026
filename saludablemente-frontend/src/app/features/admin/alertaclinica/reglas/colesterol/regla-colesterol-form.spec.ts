import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaColesterolForm } from './regla-colesterol-form';

describe('ReglaColesterolForm', () => {
  let component: ReglaColesterolForm;
  let fixture: ComponentFixture<ReglaColesterolForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaColesterolForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaColesterolForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
