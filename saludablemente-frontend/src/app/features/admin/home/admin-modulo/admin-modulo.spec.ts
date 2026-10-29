import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AdminModulo } from './admin-modulo';
import { UsoCardAlertaService } from '../../alertaclinica/uso-card-alerta-service';
import type { AdminHijo } from '../admin-modulos';

const GLUCOSA: AdminHijo = {
  titulo: 'Regla: Glucosa',
  descripcion: 'Umbrales de glucosa.',
  icono: '@tui.droplet',
  ruta: '/alertaclinica/reglas/glucosa',
};

function usoStub() {
  return {
    obtener: () => of<Record<string, number>>({}),
    registrar: () => of(void 0),
    reiniciar: () => of(void 0),
  };
}

function providers(idModulo: string) {
  return [
    provideRouter([]),
    { provide: UsoCardAlertaService, useValue: usoStub() },
    {
      provide: ActivatedRoute,
      useValue: {
        snapshot: { paramMap: { get: () => idModulo } },
      },
    },
  ];
}

describe('AdminModulo', () => {
  let fixture: ComponentFixture<AdminModulo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminModulo],
      providers: providers('personal'),
    }).compileComponents();

    fixture = TestBed.createComponent(AdminModulo);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render children as rows inside the module', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Personal');
    expect(el.querySelectorAll('.hija').length).toBe(2);
  });

  it('should filter functions locally', () => {
    const comp = fixture.componentInstance;
    comp['filtro'].set('personas');
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.hija').length).toBe(1);
    expect(el.textContent).toContain('Personas');
  });
});

describe('AdminModulo alertas con destacadas', () => {
  let fixture: ComponentFixture<AdminModulo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminModulo],
      providers: providers('alertas'),
    }).compileComponents();

    fixture = TestBed.createComponent(AdminModulo);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('muestra 2 cards grandes y 9 de reglas', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.hija-grande').length).toBe(2);
    expect(el.querySelectorAll('.regla-card').length).toBe(9);
    expect(el.querySelectorAll('.hija').length).toBe(0);
  });

  it('el filtro respeta ambas secciones', () => {
    const comp = fixture.componentInstance;
    comp['filtro'].set('imc');
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.hija-grande').length).toBe(0);
    expect(el.querySelectorAll('.regla-card').length).toBe(1);
  });

  it('sin uso mantiene el orden por defecto', () => {
    const comp = fixture.componentInstance;
    expect(comp['hayUso']()).toBe(false);
    expect(comp['destacadas']()[0].titulo).toBe('Alertas');
    expect(comp['restantes']()[0].titulo).toBe('Regla: Perímetro abdominal');
  });

  it('reordena las reglas por frecuencia de uso', () => {
    const comp = fixture.componentInstance;
    for (let i = 0; i < 3; i++) comp['registrarUso'](GLUCOSA);
    fixture.detectChanges();
    expect(comp['hayUso']()).toBe(true);
    expect(comp['restantes']()[0].titulo).toBe('Regla: Glucosa');
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.regla-card h4')?.textContent).toContain('Glucosa');
    expect(el.querySelector('.hija-grande')?.textContent).toContain('Alertas');
  });

  it('el botón Reordenar vuelve al orden por defecto', () => {
    const comp = fixture.componentInstance;
    comp['registrarUso'](GLUCOSA);
    fixture.detectChanges();
    let el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('button')?.textContent).toContain('Reordenar');
    expect(comp['restantes']()[0].titulo).toBe('Regla: Glucosa');

    comp['restablecerOrden']();
    fixture.detectChanges();
    el = fixture.nativeElement as HTMLElement;
    expect(comp['hayUso']()).toBe(false);
    expect(comp['restantes']()[0].titulo).toBe('Regla: Perímetro abdominal');
    expect(el.querySelector('button')).toBeNull();
  });
});
