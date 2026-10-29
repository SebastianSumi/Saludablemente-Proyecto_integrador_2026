import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaGrasaVisceralList } from './regla-grasa-visceral-list';

describe('ReglaGrasaVisceralList', () => {
  let component: ReglaGrasaVisceralList;
  let fixture: ComponentFixture<ReglaGrasaVisceralList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaGrasaVisceralList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaGrasaVisceralList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
