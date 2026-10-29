import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { ReglaPresionArterialList } from './regla-presion-arterial-list';

describe('ReglaPresionArterialList', () => {
  let component: ReglaPresionArterialList;
  let fixture: ComponentFixture<ReglaPresionArterialList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglaPresionArterialList],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglaPresionArterialList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
