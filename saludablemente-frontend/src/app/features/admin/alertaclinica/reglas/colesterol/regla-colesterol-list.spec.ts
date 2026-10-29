import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaColesterolList } from './regla-colesterol-list';

describe('ReglaColesterolList', () => {
  let component: ReglaColesterolList;
  let fixture: ComponentFixture<ReglaColesterolList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaColesterolList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaColesterolList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
