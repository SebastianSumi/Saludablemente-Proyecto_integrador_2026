import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PerfilService } from '../admin/perfil/perfil-service';
import { SocialService } from './social-service';
import { SocialCurrentAuthor } from './social-current-author';
import { BACKEND } from './testing/backend-fixtures';

describe('Social · identidad del editor', () => {
  it('consulta el idUsuario autenticado y comparte la lectura de la foto', () => {
    const perfil = { obtener: vi.fn(() => of({ idUsuario: 7, idPersona: 900 })) };
    const social = { perfil: vi.fn(() => of(BACKEND.perfil)) };
    TestBed.configureTestingModule({
      providers: [
        { provide: PerfilService, useValue: perfil },
        { provide: SocialService, useValue: social },
      ],
    });
    const service = TestBed.inject(SocialCurrentAuthor);
    const values: unknown[] = [];
    service.autor$.subscribe((a) => values.push(a));
    service.autor$.subscribe((a) => values.push(a));
    expect(social.perfil).toHaveBeenCalledExactlyOnceWith(7);
    expect(values).toEqual([BACKEND.perfil.autor, BACKEND.perfil.autor]);
  });
  it('si falla el perfil permite comentar conservando el avatar de respaldo', () => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: PerfilService,
          useValue: { obtener: () => throwError(() => new Error('sin perfil')) },
        },
        { provide: SocialService, useValue: {} },
      ],
    });
    let value: unknown;
    TestBed.inject(SocialCurrentAuthor).autor$.subscribe((a) => (value = a));
    expect(value).toBeNull();
  });
});
