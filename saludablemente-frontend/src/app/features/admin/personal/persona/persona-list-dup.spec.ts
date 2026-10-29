import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { delay, of } from 'rxjs';
import { PersonaList } from './persona-list';
import { PersonaService } from './persona-service';
import { TeamService } from '../../teams/team-service';
import { AreaService } from '../../catalogo/area/area-service';

const PERSONAS = Array.from({ length: 8 }, (_, i) => ({
  id: i + 1,
  nombres: `Nombre${i + 1}`,
  apellidoPaterno: 'Paterno',
  apellidoMaterno: 'Materno',
  documentoIdentidad: `1000000${i}`,
  celular: '951000001',
  fechaNacimiento: '1990-01-01',
  sexo: 'M',
  areaTrabajo: { id: 1, nombre: 'Area 1' },
  equipo: { id: 1, nombre: 'Equipo 1' },
  edad: 30,
  activo: true,
  email: `u${i}@x.pe`,
}));

describe('PersonaList duplicados', () => {
  let fixture: ComponentFixture<PersonaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaList],
      providers: [
        {
          provide: PersonaService,
          useValue: {
            listar: () => of(PERSONAS).pipe(delay(50)),
            inactivar: () => of(undefined),
          },
        },
        { provide: TeamService, useValue: { listar: () => of([]).pipe(delay(80)) } },
        { provide: AreaService, useValue: { listar: () => of([]).pipe(delay(80)) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => null } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PersonaList);
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 400));
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('muestra el contador UNA sola vez', () => {
    const html = fixture.nativeElement.innerHTML as string;
    const veces = (html.match(/registrada\(s\)/g) ?? []).length;
    // eslint-disable-next-line no-console
    console.log(`contador aparece ${veces} veces`);
    expect(veces).toBe(1);
  });

  it('muestra 8 filas de datos', () => {
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    // eslint-disable-next-line no-console
    console.log(`filas en tbody: ${filas.length}`);
    expect(filas.length).toBe(8);
  });

  it('no duplica tras filtrar varias veces', async () => {
    const comp = fixture.componentInstance as unknown as {
      estadoCtrl: { setValue: (v: unknown) => void };
      areaCtrl: { setValue: (v: unknown) => void };
      limpiarFiltros: () => void;
    };
    const espera = (ms: number) => new Promise((r) => setTimeout(r, ms));
    comp.estadoCtrl.setValue({ id: 'activos', label: 'Solo activos' });
    fixture.detectChanges();
    await espera(200);
    comp.areaCtrl.setValue({ id: null, label: 'Todas' });
    fixture.detectChanges();
    await espera(200);
    comp.limpiarFiltros();
    fixture.detectChanges();
    await espera(200);
    fixture.detectChanges();

    const html = fixture.nativeElement.innerHTML as string;
    const veces = (html.match(/registrada\(s\)/g) ?? []).length;
    // eslint-disable-next-line no-console
    console.log(`tras filtros: contador ${veces} veces`);
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    // eslint-disable-next-line no-console
    console.log(`tras filtros: filas ${filas.length}`);
    expect(veces).toBe(1);
    expect(filas.length).toBe(8);
  });

  it('no duplica con respuestas asíncronas y filtros rápidos', async () => {
    const comp = fixture.componentInstance as unknown as {
      estadoCtrl: { setValue: (v: unknown) => void };
      sexoCtrl: { setValue: (v: unknown) => void };
      limpiarFiltros: () => void;
    };
    const espera = (ms: number) => new Promise((r) => setTimeout(r, ms));
    await espera(250);
    fixture.detectChanges();
    // Cambios rápidos sucesivos (respuestas desordenadas)
    comp.estadoCtrl.setValue({ id: 'activos', label: 'Solo activos' });
    comp.sexoCtrl.setValue({ id: 'M', label: 'Masculino' });
    comp.estadoCtrl.setValue({ id: 'todos', label: 'Todos' });
    await espera(250);
    fixture.detectChanges();
    comp.limpiarFiltros();
    await espera(250);
    fixture.detectChanges();

    const html = fixture.nativeElement.innerHTML as string;
    const veces = (html.match(/registrada\(s\)/g) ?? []).length;
    // eslint-disable-next-line no-console
    console.log(`async: contador ${veces} veces`);
    const cabeceras = fixture.nativeElement.querySelectorAll('.page-banner');
    // eslint-disable-next-line no-console
    console.log(`async: cabeceras ${cabeceras.length}`);
    const filas = fixture.nativeElement.querySelectorAll('tbody tr');
    // eslint-disable-next-line no-console
    console.log(`async: filas ${filas.length}`);
    expect(veces).toBe(1);
    expect(cabeceras.length).toBe(1);
    expect(filas.length).toBe(8);
  });
});
