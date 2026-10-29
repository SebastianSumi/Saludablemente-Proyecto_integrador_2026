import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPorcentajeGrasaList } from './regla-porcentaje-grasa-list';

describe('ReglaPorcentajeGrasaList', () => {
  let component: ReglaPorcentajeGrasaList;
  let fixture: ComponentFixture<ReglaPorcentajeGrasaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPorcentajeGrasaList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPorcentajeGrasaList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
