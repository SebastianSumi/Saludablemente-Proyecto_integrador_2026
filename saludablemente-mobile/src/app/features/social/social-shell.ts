import { SocialIcon } from './social-icon';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
@Component({
  selector: 'app-social',
  imports: [SocialIcon, RouterLink, RouterLinkActive, RouterOutlet],
  styleUrls: ['./social.css', './social-shell.css'],
  template: ` <section class="social-wrap">
    <header class="social-hero">
      <span class="hero-mark"><social-icon name="messages-square" /></span>
      <div>
        <span class="eyebrow">Comunidad Saludablemente</span>
        <h1>Social</h1>
        <p class="muted">Compartimos experiencias. Crecemos en equipo.</p>
      </div>
    </header>
    <nav class="social-nav" aria-label="Secciones de Social">
      <a routerLink="/social" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }"
        ><social-icon name="layout-grid" />Muro</a
      >
      <a routerLink="/social/mi-contenido" routerLinkActive="active"
        ><social-icon name="bookmark" />Mi contenido</a
      >
      <a routerLink="/social/buscar" routerLinkActive="active"
        ><social-icon name="search" />Buscar</a
      >
      @if (auth.tieneRol('Administrador')) {
        <a routerLink="/social/admin/reportes" routerLinkActive="active"
          ><social-icon name="shield-check" />Moderación</a
        ><a routerLink="/social/admin/metricas" routerLinkActive="active"
          ><social-icon name="chart-no-axes-combined" />Métricas</a
        >
      }
    </nav>
    <router-outlet />
  </section>`,
})
export class SocialShell {
  protected readonly auth = inject(AuthService);
}
