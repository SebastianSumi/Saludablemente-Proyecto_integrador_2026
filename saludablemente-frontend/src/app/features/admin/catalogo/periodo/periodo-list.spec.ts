import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { PeriodoList } from './periodo-list';

describe('PeriodoList', () => {
  let component: PeriodoList;
  let fixture: ComponentFixture<PeriodoList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PeriodoList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(PeriodoList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
