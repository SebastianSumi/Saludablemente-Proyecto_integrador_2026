import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ColAsistenciaList } from './asistencia-list';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { AsistenciaService } from '../../admin/asistencias/asistencia-service';
import { AgendaService } from '../../admin/actividades/agenda-service';

describe('ColAsistenciaList', () => {
  it('crea el componente y carga historial propio', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: { obtener: () => of({ idPersona: 7 }) } },
        { provide: AsistenciaService, useValue: { historialPorPersona: () => of([]) } },
        { provide: AgendaService, useValue: { deHoy: () => of([]) } },
      ],
    });
    const fixture = TestBed.createComponent(ColAsistenciaList);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });
});
