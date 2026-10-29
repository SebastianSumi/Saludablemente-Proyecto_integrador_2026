import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { TeamForm } from './team-form';
import { AuthService } from '../../../core/auth/auth-service';

describe('TeamForm', () => {
  let component: TeamForm;
  let fixture: ComponentFixture<TeamForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeamForm],
      providers: [provideRouter([]), provideHttpClient(), { provide: AuthService, useValue: { tieneRol: () => true } }],
    }).compileComponents();

    fixture = TestBed.createComponent(TeamForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
