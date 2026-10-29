import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { EvaluacionDetail } from './evaluacion-detail';

describe('EvaluacionDetail', () => {
  let component: EvaluacionDetail;
  let fixture: ComponentFixture<EvaluacionDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluacionDetail],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(EvaluacionDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
