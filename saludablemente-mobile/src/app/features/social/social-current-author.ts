import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, map, of, shareReplay, switchMap } from 'rxjs';
import { PerfilService } from '../admin/perfil/perfil-service';
import { SocialService } from './social-service';

/** Perfil autenticado: el ID procede de mi-perfil, nunca de un campo editable. */
@Injectable({ providedIn: 'root' })
export class SocialCurrentAuthor {
  private readonly perfil = inject(PerfilService);
  private readonly social = inject(SocialService);
  private readonly reload = new BehaviorSubject(0);
  refresh() {
    this.reload.next(this.reload.value + 1);
  }
  readonly autor$ = this.reload.pipe(
    switchMap(() =>
      this.perfil.obtener().pipe(
        switchMap((p) => this.social.perfil(p.idUsuario)),
        map((p) => p.autor),
        catchError(() => of(null)),
      ),
    ),
    shareReplay({ bufferSize: 1, refCount: true }),
  );
}
