import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../services/api-service';
import { normalizarRoles, tieneAlgunRol } from './roles';

export interface SesionAuth {
  token: string;
  email: string;
  nombre: string;
  roles: string[];
  expiraEn: number;
}

export interface Credenciales {
  identificador: string;
  password: string;
}

const CLAVE_SESION = 'slb.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly sesion = signal<SesionAuth | null>(this.leerSesion());

  login(credenciales: Credenciales): Observable<SesionAuth> {
    return this.http
      .post<SesionAuth>(this.api.buildUrl('/api/v1/auth/login'), credenciales)
      .pipe(
        tap((sesion) => {
          this.guardarSesion(sesion);
        })
      );
  }

  iniciarSesion(returnUrl?: string): void {
    const destino = returnUrl && returnUrl.startsWith('/') ? returnUrl : '/';
    this.router.navigate(['/login'], {
      queryParams: destino !== '/' ? { returnUrl: destino } : {},
    });
  }

  cerrarSesion(): void {
    if (this.token()) {
      this.http
        .post(this.api.buildUrl('/api/v1/auth/logout'), {})
        .subscribe({ error: () => undefined });
    }
    this.limpiarSesion();
    this.router.navigate(['/login']);
  }

  estaAutenticado(): boolean {
    const s = this.sesion();
    return !!s?.token && s.expiraEn * 1000 > Date.now();
  }

  token(): string | null {
    return this.estaAutenticado() ? this.sesion()!.token : null;
  }

  obtenerEmail(): string | undefined {
    return this.sesion()?.email;
  }

  obtenerNombre(): string | undefined {
    return this.sesion()?.nombre;
  }

  obtenerRoles(): string[] {
    return this.sesion()?.roles ?? [];
  }

  obtenerRolesNormalizados(): string[] {
    return normalizarRoles(this.obtenerRoles());
  }

  tieneRol(rol: string): boolean {
    return tieneAlgunRol(this.obtenerRoles(), [rol]);
  }

  tieneAlgunRol(roles: string[]): boolean {
    return tieneAlgunRol(this.obtenerRoles(), roles);
  }

  private guardarSesion(sesion: SesionAuth): void {
    this.sesion.set(sesion);
    try {
      localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    } catch {
      // Almacenamiento no disponible (modo privado); la sesión dura lo que la pestaña.
    }
  }

  private limpiarSesion(): void {
    this.sesion.set(null);
    try {
      localStorage.removeItem(CLAVE_SESION);
    } catch {
      // sin almacenamiento no hay nada que limpiar
    }
  }

  private leerSesion(): SesionAuth | null {
    try {
      const raw = localStorage.getItem(CLAVE_SESION);
      if (!raw) return null;
      const s = JSON.parse(raw) as SesionAuth;
      if (!s?.token || !s?.email || !Array.isArray(s?.roles) || !s?.expiraEn) {
        localStorage.removeItem(CLAVE_SESION);
        return null;
      }
      if (s.expiraEn * 1000 <= Date.now()) {
        localStorage.removeItem(CLAVE_SESION);
        return null;
      }
      return s;
    } catch {
      return null;
    }
  }
}
