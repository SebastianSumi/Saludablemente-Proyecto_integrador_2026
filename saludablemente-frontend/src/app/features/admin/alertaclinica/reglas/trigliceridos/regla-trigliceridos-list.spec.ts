import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaTrigliceridosList } from './regla-trigliceridos-list';

describe('ReglaTrigliceridosList', () => {
  let component: ReglaTrigliceridosList;
  let fixture: ComponentFixture<ReglaTrigliceridosList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaTrigliceridosList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaTrigliceridosList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
