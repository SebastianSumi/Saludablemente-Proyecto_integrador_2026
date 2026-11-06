import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, shareReplay } from 'rxjs';
import { PersonaService } from '../../personal/persona/persona-service';

/**
 * Resuelve idPersona -> nombre completo para las vistas de alertas.
 * El backend de alertas solo envía personaId, así que se carga la
 * lista de personas una vez y se cachea. Si una persona no aparece
 * (inactiva, sin permiso de lectura), se usa el fallback `Persona #id`.
 */
@Injectable({ providedIn: 'root' })
export class PersonaNombreService {
  private readonly personas = inject(PersonaService);
  private cache?: Observable<Map<number, string>>;

  nombres(): Observable<Map<number, string>> {
    this.cache ??= this.personas.listar().pipe(
      map((lista) => {
        const m = new Map<number, string>();
        for (const p of lista) {
          const nombre = [p.nombres, p.apellidoPaterno, p.apellidoMaterno]
            .filter((x) => x && x.trim().length > 0)
            .join(' ');
          m.set(p.id, nombre || `Persona #${p.id}`);
        }
        return m;
      }),
      catchError(() => of(new Map<number, string>())),
      shareReplay(1),
    );
    return this.cache;
  }

  nombreDe(nombres: Map<number, string>, idPersona: number): string {
    return nombres.get(idPersona) ?? `Persona #${idPersona}`;
  }
}
