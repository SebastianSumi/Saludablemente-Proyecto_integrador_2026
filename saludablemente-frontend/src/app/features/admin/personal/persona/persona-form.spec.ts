import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { PersonaForm } from './persona-form';

describe('PersonaForm', () => {
  let component: PersonaForm;
  let fixture: ComponentFixture<PersonaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(PersonaForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
