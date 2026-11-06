import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth-service';
@Injectable({providedIn:'root'})
export class AuthGuard implements CanActivate {
  private readonly auth=inject(AuthService);
  private readonly router=inject(Router);
  canActivate(_route:ActivatedRouteSnapshot,state:RouterStateSnapshot) {
    return this.auth.estaAutenticado() || this.router.createUrlTree(['/login'],{queryParams:{returnUrl:state.url}});
  }
}
