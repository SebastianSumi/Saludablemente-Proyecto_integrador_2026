import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth-service';

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();
  if (!token) return next(req);

  return next(
    req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
  );
};
