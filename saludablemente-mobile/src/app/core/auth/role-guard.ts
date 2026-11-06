import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

export const RoleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data?.['roles'] ?? []) as string[];
  if (roles.length === 0 || auth.tieneAlgunRol(roles)) return true;
  router.navigate(['/no-autorizado'], {
    queryParams: { requerido: roles.join(',') },
  });
  return false;
};
