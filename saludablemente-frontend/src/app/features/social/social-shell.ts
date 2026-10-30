import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
@Component({
  selector: 'app-social',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  styleUrl: './social.css',
  template: ` <section class="social-wrap">
    <header class="social-hero">
      <div>
        <span class="eyebrow">Comunidad Saludablemente</span>
        <h1>Social</h1>
        <p class="muted">Compartimos experiencias. Crecemos en equipo.</p>
      </div>
      <span aria-hidden="true" class="chip">Un espacio para conectar</span>
    </header>
    <nav class="social-nav" aria-label="Secciones de Social">
      <a routerLink="/social" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }"
        >Muro</a
      >
      <a routerLink="/social/mi-contenido" routerLinkActive="active">Mi contenido</a>
      <a routerLink="/social/buscar" routerLinkActive="active">Buscar</a>
      @if (auth.tieneRol('Administrador')) {
        <a routerLink="/social/admin/reportes" routerLinkActive="active">Moderación</a
        ><a routerLink="/social/admin/metricas" routerLinkActive="active">Métricas</a>
      }
    </nav>
    <router-outlet />
  </section>`,
})
export class SocialShell {
  protected readonly auth = inject(AuthService);
}
