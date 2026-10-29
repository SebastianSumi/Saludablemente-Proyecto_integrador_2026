import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPerimetroAbdominalList } from './regla-perimetro-abdominal-list';

describe('ReglaPerimetroAbdominalList', () => {
  let component: ReglaPerimetroAbdominalList;
  let fixture: ComponentFixture<ReglaPerimetroAbdominalList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPerimetroAbdominalList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPerimetroAbdominalList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
