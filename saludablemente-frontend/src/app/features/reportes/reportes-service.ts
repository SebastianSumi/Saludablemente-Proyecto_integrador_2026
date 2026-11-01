import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/reportes/personales';

  asistencias(periodo: string): Observable<Blob> {
    return this.http.get(`${this.base}/asistencias`, {
      params: { periodo },
      responseType: 'blob',
    });
  }

  datosPersonales(): Observable<Blob> {
    return this.http.get(`${this.base}/datos-personales`, { responseType: 'blob' });
  }

  actividades(desde: string, hasta: string): Observable<Blob> {
    return this.http.get(`${this.base}/actividades`, {
      params: { desde, hasta },
      responseType: 'blob',
    });
  }

  evaluaciones(): Observable<Blob> {
    return this.http.get(`${this.base}/evaluaciones`, { responseType: 'blob' });
  }

  alertas(desde: string, hasta: string): Observable<Blob> {
    return this.http.get(`${this.base}/alertas`, {
      params: { desde, hasta },
      responseType: 'blob',
    });
  }
}
