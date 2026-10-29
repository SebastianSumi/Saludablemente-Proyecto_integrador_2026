import { Injectable, inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { AuthService } from './auth-service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  canActivate(
    _route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    if (this.auth.estaAutenticado()) return true;
    return this.router.createUrlTree(['/login'], {
      queryParams: state.url && state.url !== '/' ? { returnUrl: state.url } : {},
    });
  }
}
