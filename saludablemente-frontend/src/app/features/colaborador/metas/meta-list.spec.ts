import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColMetaList } from './meta-list';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { MetaService } from '../../admin/metas/meta-service';

describe('ColMetaList', () => {
  it('crea el componente y lista metas propias', () => {
    const perfil = { obtener: () => of({ idPersona: 7 }) };
    const metas = { listarPorPersona: () => of([]) };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: perfil },
        { provide: MetaService, useValue: metas },
      ],
    });
    const fixture = TestBed.createComponent(ColMetaList);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });
});
