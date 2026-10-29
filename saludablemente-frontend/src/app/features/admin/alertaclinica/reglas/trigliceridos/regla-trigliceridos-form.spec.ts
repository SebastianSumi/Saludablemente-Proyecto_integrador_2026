import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaTrigliceridosForm } from './regla-trigliceridos-form';

describe('ReglaTrigliceridosForm', () => {
  let component: ReglaTrigliceridosForm;
  let fixture: ComponentFixture<ReglaTrigliceridosForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaTrigliceridosForm],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaTrigliceridosForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
