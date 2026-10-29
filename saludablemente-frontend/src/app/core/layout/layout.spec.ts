import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Layout, construirMigas, haceCuanto } from './layout';
import { AuthService } from '../auth/auth-service';
import { ThemeService } from '../theme/theme-service';
import { PerfilService } from '../../features/admin/perfil/perfil-service';
import { NotificacionService } from '../../features/colaborador/notificaciones/notificacion-service';

describe('Layout', () => {
  let component: Layout;
  let fixture: ComponentFixture<Layout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Layout],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            estaAutenticado: () => true,
            obtenerNombre: () => 'Test User',
            obtenerRoles: () => ['Administrador'],
            tieneAlgunRol: () => true,
          },
        },
        {
          provide: ThemeService,
          useValue: { esOscuro: () => false, alternar: () => undefined },
        },
        {
          provide: PerfilService,
          useValue: {
            obtener: () =>
              of({
                idUsuario: 1,
                idPersona: 7,
                email: 'test@example.com',
                nombreCompleto: 'Test User',
                documentoIdentidad: '12345678',
                areaTrabajo: 'Limpieza',
                activo: true,
                fechaCreacion: '2024-01-15',
                roles: ['Administrador'],
              }),
          },
        },
        {
          provide: NotificacionService,
          useValue: {
            contadorNoLeidas: () => of(3),
            misAlertas: () =>
              of([
                { idNotificacion: 1, titulo: 'A', mensaje: 'a', leido: false },
                { idNotificacion: 2, titulo: 'B', mensaje: 'b', leido: true },
              ]),
            marcarLeida: () => of(undefined),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Layout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load notification count', () => {
    expect(component['noLeidas']()).toBe(3);
  });

  it('should open the bell dropdown with the latest notifications', () => {
    component['alternarCampana']();
    expect(component['campanaAbierta']()).toBe(true);
    expect(component['ultimas']().length).toBe(2);
  });
});

describe('haceCuanto', () => {
  const ahora = new Date('2026-10-09T12:00:00').getTime();

  it('ahora mismo, minutos, horas y días', () => {
    expect(haceCuanto('2026-10-09T12:00:00', ahora)).toBe('ahora mismo');
    expect(haceCuanto('2026-10-09T11:57:00', ahora)).toBe('hace 3 min');
    expect(haceCuanto('2026-10-09T10:00:00', ahora)).toBe('hace 2 h');
    expect(haceCuanto('2026-10-07T12:00:00', ahora)).toBe('hace 2 d');
  });

  it('vacío o inválido devuelve cadena vacía', () => {
    expect(haceCuanto(undefined, ahora)).toBe('');
    expect(haceCuanto('no-fecha', ahora)).toBe('');
  });
});

describe('construirMigas', () => {
  it('inicio sin segmentos', () => {
    expect(construirMigas('/')).toEqual([{ titulo: 'Inicio', ruta: '/' }]);
  });

  it('resuelve módulo del catálogo', () => {
    expect(construirMigas('/admin/modulo/personal')).toEqual([
      { titulo: 'Inicio', ruta: '/' },
      { titulo: 'Módulos', ruta: '/admin', fragment: 'modulo-personal' },
      { titulo: 'Personal' },
    ]);
  });

  it('construye ruta acumulada para rutas planas', () => {
    expect(construirMigas('/personal/personas')).toEqual([
      { titulo: 'Inicio', ruta: '/' },
      { titulo: 'Personal', ruta: '/personal' },
      { titulo: 'Personas' },
    ]);
  });
});
