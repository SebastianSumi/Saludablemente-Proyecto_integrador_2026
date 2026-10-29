import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaGrasaVisceralForm } from './regla-grasa-visceral-form';

describe('ReglaGrasaVisceralForm', () => {
  let component: ReglaGrasaVisceralForm;
  let fixture: ComponentFixture<ReglaGrasaVisceralForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaGrasaVisceralForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaGrasaVisceralForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
