import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AlertaClinicaDetail } from './alerta-clinica-detail';

describe('AlertaClinicaDetail', () => {
  let component: AlertaClinicaDetail;
  let fixture: ComponentFixture<AlertaClinicaDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertaClinicaDetail],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(AlertaClinicaDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
