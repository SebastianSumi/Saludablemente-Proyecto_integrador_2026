import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaMasaMuscularForm } from './regla-masa-muscular-form';

describe('ReglaMasaMuscularForm', () => {
  let component: ReglaMasaMuscularForm;
  let fixture: ComponentFixture<ReglaMasaMuscularForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaMasaMuscularForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaMasaMuscularForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
