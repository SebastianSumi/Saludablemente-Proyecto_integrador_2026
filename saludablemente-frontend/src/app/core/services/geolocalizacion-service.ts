import { Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';

export interface Ubicacion {
  latitud: number;
  longitud: number;
  precisionM: number | null;
}

export type EstadoGps = 'idle' | 'detectando' | 'ok' | 'error';

@Injectable({ providedIn: 'root' })
export class GeolocalizacionService {
  readonly estado = signal<EstadoGps>('idle');
  readonly ubicacion = signal<Ubicacion | null>(null);
  readonly error = signal<string | null>(null);

  /** Captura única con alta precisión. GPS obligatorio: sin fix no se registra. */
  detectar(): Observable<Ubicacion> {
    return new Observable<Ubicacion>((sub) => {
      if (!('geolocation' in navigator)) {
        const msg = 'Este navegador no soporta geolocalización. Usa Chrome o Edge con HTTPS.';
        this.estado.set('error');
        this.error.set(msg);
        sub.error(new Error(msg));
        return;
      }
      this.estado.set('detectando');
      this.error.set(null);
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const u: Ubicacion = {
            latitud: pos.coords.latitude,
            longitud: pos.coords.longitude,
            precisionM: pos.coords.accuracy ?? null,
          };
          this.ubicacion.set(u);
          this.estado.set('ok');
          sub.next(u);
          sub.complete();
        },
        (err) => {
          const msg =
            err.code === err.PERMISSION_DENIED
              ? 'Permiso de ubicación denegado. Activa el GPS y otorga permiso al navegador.'
              : err.code === err.POSITION_UNAVAILABLE
                ? 'No se pudo obtener la ubicación. Verifica que el GPS esté encendido.'
                : 'Tiempo de espera agotado buscando GPS. Intenta de nuevo al aire libre.';
          this.estado.set('error');
          this.error.set(msg);
          sub.error(new Error(msg));
        },
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
      );
    });
  }

  limpiar(): void {
    this.estado.set('idle');
    this.ubicacion.set(null);
    this.error.set(null);
  }

  /** Enlace universal para compartir/abrir en Google Maps (sin API key). */
  static enlaceGoogleMaps(lat: number, lon: number): string {
    return `https://www.google.com/maps?q=${lat},${lon}`;
  }

  static textoCompartir(titulo: string, lat: number, lon: number): string {
    return `${titulo}: ${GeolocalizacionService.enlaceGoogleMaps(lat, lon)}`;
  }
}
