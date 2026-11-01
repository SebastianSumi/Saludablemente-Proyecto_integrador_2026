import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MiEquipo } from './mi-equipo';
import { TeamService } from './team-service';

describe('Mi equipo · perfil', () => {
  const team = { id: 7, nombre: 'Bienestar', activo: true, idUsuarioLider: 20,
    descripcion: 'Juntos por nuestra salud', color: '#2563EB', imagenUrl: '/logo.png' };
  let api: { miEquipo: ReturnType<typeof vi.fn>; puedeEditar: ReturnType<typeof vi.fn>; miembrosMiEquipo: ReturnType<typeof vi.fn> };
  beforeEach(() => {
    api = {
      miEquipo: vi.fn(() => of(team)), puedeEditar: vi.fn(() => of(false)),
      miembrosMiEquipo: vi.fn(() => of([{ idUsuario: 21, nombre: 'Ana Pérez' }, { idUsuario: 20, nombre: 'Luis García' }])),
    };
    TestBed.configureTestingModule({ imports: [MiEquipo], providers: [provideRouter([]), { provide: TeamService, useValue: api }] });
  });
  async function render() {
    const f = TestBed.createComponent(MiEquipo);
    f.detectChanges(); await f.whenStable(); f.detectChanges();
    return f;
  }
  it('presenta al líder primero y conserva los enlaces reales', async () => {
    const f = await render();
    const root = f.nativeElement as HTMLElement;
    expect(root.querySelector('.member-card')?.textContent).toContain('Luis García');
    expect(root.querySelector('.leader-badge')?.textContent).toContain('Líder del equipo');
    expect(root.querySelector('.member-count')?.textContent).toContain('2 integrantes');
    expect(root.querySelector('a[href="/social/equipos/7"]')).not.toBeNull();
    expect(root.querySelector('a[href="/teams/7/editar"]')).toBeNull();
  });
  it('filtra sin acentos ni consultas nuevas y permite limpiar', async () => {
    const f = await render();
    const root = f.nativeElement as HTMLElement;
    const input = root.querySelector('input')!;
    input.value = 'perez'; input.dispatchEvent(new Event('input')); f.detectChanges();
    expect(root.querySelectorAll('.member-card').length).toBe(1);
    expect(root.querySelector('.member-card')?.textContent).toContain('Ana Pérez');
    input.value = 'nadie'; input.dispatchEvent(new Event('input')); f.detectChanges();
    expect(root.textContent).toContain('No encontramos coincidencias');
    (root.querySelector('.members-empty button') as HTMLButtonElement).click(); f.detectChanges();
    expect(root.querySelectorAll('.member-card').length).toBe(2);
    expect(api.miembrosMiEquipo).toHaveBeenCalledTimes(1);
  });
  it('muestra edición solo con permiso y respaldo del logo', async () => {
    api.puedeEditar.mockReturnValue(of(true));
    const f = await render();
    expect(f.nativeElement.querySelector('a[href="/teams/7/editar"]')).not.toBeNull();
    f.nativeElement.querySelector('.team-logo img').dispatchEvent(new Event('error')); f.detectChanges();
    expect(f.nativeElement.querySelector('.team-logo').textContent).toContain('B');
  });
  it('recupera un error de integrantes sin ocultar el equipo', async () => {
    api.miembrosMiEquipo.mockReturnValueOnce(throwError(() => new Error('offline')));
    const f = await render();
    expect(f.nativeElement.textContent).toContain('No pudimos cargar los integrantes');
    expect(f.nativeElement.querySelector('.team-name').textContent).toContain('Bienestar');
    f.nativeElement.querySelector('.inline-state button').click(); f.detectChanges();
    expect(f.nativeElement.querySelectorAll('.member-card').length).toBe(2);
  });
  it('permite reintentar la carga del equipo', async () => {
    api.miEquipo.mockReturnValueOnce(throwError(() => new Error('offline')));
    const f = await render();
    expect(f.nativeElement.textContent).toContain('Tu equipo aún no está disponible');
    f.nativeElement.querySelector('.state-panel button').click(); f.detectChanges();
    expect(f.nativeElement.querySelector('.team-name').textContent).toContain('Bienestar');
  });
});
