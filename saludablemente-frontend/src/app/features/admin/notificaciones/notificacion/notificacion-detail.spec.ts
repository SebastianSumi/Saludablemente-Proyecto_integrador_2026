import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { NotificacionDetail, rutaOrigen } from './notificacion-detail';
import { NotificacionService } from '../notificacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';

describe('NotificacionDetail', () => {
  let fixture: ComponentFixture<NotificacionDetail>;

  const mock = {
    idNotificacion: 1,
    idPersona: 7,
    canal: 'IN_APP',
    titulo: 'Alerta generada',
    mensaje: 'Se detectó un valor fuera de rango.',
    origenModulo: 'alertaclinica',
    idOrigenReferencia: 3,
    estadoLectura: 'NO_LEIDO',
    estadoDelivery: 'ENVIADO',
    fechaEnvio: '2026-10-01T10:00:00',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificacionDetail],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } },
        },
        { provide: NotificacionService, useValue: { obtener: () => of(mock) } },
        { provide: ConfirmService, useValue: { confirmar: () => of(false) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificacionDetail);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the notification detail', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Alerta generada');
    expect(el.textContent).toContain('Se detectó un valor fuera de rango.');
    expect(el.textContent).toContain('No leído');
  });

  it('should link to the origin module', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Ir al origen');
  });
});

describe('rutaOrigen', () => {
  it('resuelve módulos conocidos y normaliza variantes', () => {
    expect(rutaOrigen('alertaclinica')).toBe('/alertaclinica/alertas');
    expect(rutaOrigen('alertas_clinicas')).toBe('/alertaclinica/alertas');
    expect(rutaOrigen('METAS')).toBe('/metas');
  });

  it('desconocido o vacío devuelve null', () => {
    expect(rutaOrigen('otro-modulo')).toBeNull();
    expect(rutaOrigen(undefined)).toBeNull();
  });
});
