import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth-service';
import { ApiService } from '../../../../core/services/api-service';
import type { QrActividad } from '../models';

/**
 * SSE del QR de actividad con Bearer token.
 * EventSource nativo no puede enviar Authorization, por eso se usa fetch + reader.
 * Formato del backend: `event: qr` + `data: {...json...}` por frame.
 */
@Injectable({ providedIn: 'root' })
export class QrStreamService {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  stream(idActividad: number, signal: AbortSignal): Observable<QrActividad> {
    return new Observable<QrActividad>((sub) => {
      let alive = true;
      (async () => {
        try {
          const token = this.auth.token();
          const res = await fetch(
            this.api.buildUrl(`/api/v1/actividades/${idActividad}/qr/stream`),
            {
              headers: {
                Accept: 'text/event-stream',
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
              },
              signal,
            }
          );
          if (!res.ok || !res.body) {
            if (alive) sub.error(new Error(`SSE ${res.status}`));
            return;
          }
          const reader = res.body.getReader();
          const dec = new TextDecoder();
          let buf = '';
          let data = '';
          for (;;) {
            const { done, value } = await reader.read();
            if (done) break;
            buf += dec.decode(value, { stream: true });
            const lines = buf.split('\n');
            buf = lines.pop() ?? '';
            for (const raw of lines) {
              const line = raw.replace(/\r$/, '');
              if (line === '') {
                if (data) {
                  try {
                    sub.next(JSON.parse(data) as QrActividad);
                  } catch {
                    // frame parcial: ignorar
                  }
                  data = '';
                }
                continue;
              }
              if (line.startsWith(':')) continue;
              if (line.startsWith('data:')) data += line.slice(5).trim();
            }
          }
          if (alive) sub.complete();
        } catch (e) {
          if (alive && !(e instanceof DOMException && e.name === 'AbortError')) sub.error(e);
        }
      })();
      return () => {
        alive = false;
      };
    });
  }
}
