import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth-service';
import { isApiRequest } from '../auth/session-utils';
import { environment } from '../../../environments/environment';

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  if (!isApiRequest(req.url, environment.apiBaseUrl)) return next(req);
  const token = inject(AuthService).token();
  if (!token) return next(req);

  return next(
    req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
  );
};
