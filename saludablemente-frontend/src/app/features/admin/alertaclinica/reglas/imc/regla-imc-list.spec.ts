import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaImcList } from './regla-imc-list';

describe('ReglaImcList', () => {
  let component: ReglaImcList;
  let fixture: ComponentFixture<ReglaImcList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaImcList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaImcList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
