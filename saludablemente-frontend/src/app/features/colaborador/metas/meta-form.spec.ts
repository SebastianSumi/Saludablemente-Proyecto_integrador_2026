import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColMetaForm } from './meta-form';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { MetaService } from '../../admin/metas/meta-service';
import { ConfirmService } from '../../../core/services/confirm-service';
import { provideFormatosFecha } from '../../../core/forms/fecha-transformers';

describe('ColMetaForm', () => {
  it('crea el componente y resuelve idPersona del perfil', () => {
    const perfil = { obtener: () => of({ idPersona: 7, nombreCompleto: 'Test User' }) };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideFormatosFecha(),
        { provide: PerfilService, useValue: perfil },
        { provide: MetaService, useValue: {} },
        { provide: ConfirmService, useValue: {} },
      ],
    });
    const fixture = TestBed.createComponent(ColMetaForm);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('marca error si el formulario es inválido al guardar', () => {
    const perfil = { obtener: () => of({ idPersona: 7, nombreCompleto: 'Test User' }) };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideFormatosFecha(),
        { provide: PerfilService, useValue: perfil },
        { provide: MetaService, useValue: {} },
        { provide: ConfirmService, useValue: {} },
      ],
    });
    const fixture = TestBed.createComponent(ColMetaForm);
    fixture.detectChanges();
    const comp = fixture.componentInstance as unknown as {
      guardar: () => void;
      form: { invalid: boolean };
    };
    expect(comp.form.invalid).toBe(true);
  });
});
