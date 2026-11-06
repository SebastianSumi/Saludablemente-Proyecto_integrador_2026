import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { Capacitor } from '@capacitor/core';
import { map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { isApiRequest } from '../auth/session-utils';
import { localMedia } from './local-media';

export const localMediaInterceptor:HttpInterceptorFn=(req,next)=>next(req).pipe(map(event=>{
  if(environment.production || !Capacitor.isNativePlatform() || !isApiRequest(req.url,environment.apiBaseUrl) || !(event instanceof HttpResponse)) return event;
  return event.clone({body:localMedia(event.body,environment.minio.publicUrl)});
}));
