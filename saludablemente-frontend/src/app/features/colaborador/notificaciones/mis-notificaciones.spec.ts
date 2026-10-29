import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { MisNotificaciones } from './mis-notificaciones';
import { NotificacionService } from './notificacion-service';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  template: '<tui-root><app-mis-notificaciones /></tui-root>',
  imports: [TuiRoot, MisNotificaciones],
})
class Host {}

describe('MisNotificaciones solo propias', () => {
  let fixture: ComponentFixture<Host>;
  let marcadas: number[] = [];
  let archivadas: number[] = [];

  beforeEach(async () => {
    marcadas = [];
    archivadas = [];
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: () => ({
        matches: false,
        addEventListener: () => undefined,
        removeEventListener: () => undefined,
      }),
    });
    (document as unknown as { execCommand: () => boolean }).execCommand = () => false;
    await TestBed.configureTestingModule({
      imports: [MisNotificaciones],
      providers: [
        provideTaiga(),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { tieneAlgunRol: () => false },
        },
        {
          provide: PerfilService,
          useValue: {
            obtener: () =>
              of({
                idUsuario: 1,
                idPersona: 7,
                nombreCompleto: 'Ana Colaboradora',
                roles: ['Colaborador'],
              }),
          },
        },
        {
          provide: NotificacionService,
          useValue: {
            misAlertas: (idPersona: number) => {
              expect(idPersona).toBe(7);
              return of([
                {
                  idNotificacion: 1,
                  titulo: 'Meta completada',
                  mensaje: 'Felicidades',
                  origenModulo: 'METAS',
                  canal: 'EMAIL',
                  leido: false,
                },
                {
                  idNotificacion: 2,
                  titulo: 'Recordatorio',
                  mensaje: 'Toma agua',
                  origenModulo: 'RECORDATORIOS',
                  canal: 'IN_APP',
                  leido: true,
                },
              ]);
            },
            marcarLeida: (id: number) => {
              marcadas.push(id);
              return of(undefined);
            },
            archivar: (id: number) => {
              archivadas.push(id);
              return of(undefined);
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('muestra solo las propias con contador de no leídas', () => {
    const texto = fixture.nativeElement.textContent as string;
    expect(texto).toContain('Meta completada');
    expect(texto).toContain('Recordatorio');
    expect(texto).toContain('Sin leer: 1');
    expect(texto).toContain('Leídas: 1');
  });

  it('marcar leída llama al servicio con el id', async () => {
    const botones = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const marcar = botones.find((b) => (b.textContent ?? '').includes('Marcar leída'));
    if (!marcar) throw new Error('Sin botón Marcar leída');
    marcar.click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(marcadas).toEqual([1]);
  });
});
