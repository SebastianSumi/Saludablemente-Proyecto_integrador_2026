import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AlertaClinicaList } from './alerta-clinica-list';

describe('AlertaClinicaList', () => {
  let component: AlertaClinicaList;
  let fixture: ComponentFixture<AlertaClinicaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertaClinicaList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(AlertaClinicaList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
