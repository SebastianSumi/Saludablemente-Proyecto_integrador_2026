import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { PersonaDetail } from './persona-detail';

@Component({ template: '', standalone: true })
class Dummy {}

describe('PersonaDetail', () => {
  let component: PersonaDetail;
  let fixture: ComponentFixture<PersonaDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaDetail],
      providers: [
        provideRouter([
          { path: 'personal/personas', component: Dummy },
          { path: 'personal/personas/:id', component: Dummy },
        ]),
        provideHttpClient(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PersonaDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
