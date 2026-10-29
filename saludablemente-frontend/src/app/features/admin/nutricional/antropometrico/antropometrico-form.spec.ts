import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AntropometricoForm } from './antropometrico-form';

describe('AntropometricoForm', () => {
  let component: AntropometricoForm;
  let fixture: ComponentFixture<AntropometricoForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AntropometricoForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(AntropometricoForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
