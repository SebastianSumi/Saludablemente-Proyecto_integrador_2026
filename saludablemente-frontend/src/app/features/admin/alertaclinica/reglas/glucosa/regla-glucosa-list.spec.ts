import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaGlucosaList } from './regla-glucosa-list';

describe('ReglaGlucosaList', () => {
  let component: ReglaGlucosaList;
  let fixture: ComponentFixture<ReglaGlucosaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaGlucosaList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaGlucosaList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
