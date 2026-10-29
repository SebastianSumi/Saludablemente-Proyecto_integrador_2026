import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaMasaMuscularList } from './regla-masa-muscular-list';

describe('ReglaMasaMuscularList', () => {
  let component: ReglaMasaMuscularList;
  let fixture: ComponentFixture<ReglaMasaMuscularList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaMasaMuscularList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaMasaMuscularList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
