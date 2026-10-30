import { Routes } from '@angular/router';
import { RoleGuard } from '../../core/auth/role-guard';
export const SOCIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./social-shell').then((m) => m.SocialShell),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./social-feed').then((m) => m.SocialFeed),
      },
      {
        path: 'buscar',
        loadComponent: () => import('./social-search').then((m) => m.SocialSearch),
      },
      {
        path: 'mi-contenido',
        data: { mine: true },
        loadComponent: () => import('./social-profile').then((m) => m.SocialProfile),
      },
      {
        path: 'usuarios/:id',
        loadComponent: () => import('./social-profile').then((m) => m.SocialProfile),
      },
      {
        path: 'equipos/:id',
        data: { scope: 'EQUIPO' },
        loadComponent: () => import('./social-feed').then((m) => m.SocialFeed),
      },
      {
        path: 'actividades/:id',
        data: { scope: 'ACTIVIDAD' },
        loadComponent: () => import('./social-feed').then((m) => m.SocialFeed),
      },
      {
        path: 'hashtags/:tag',
        loadComponent: () => import('./social-feed').then((m) => m.SocialFeed),
      },
      {
        path: 'posts/:id',
        loadComponent: () => import('./social-detail').then((m) => m.SocialDetail),
      },
      {
        path: 'admin/reportes',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () => import('./social-moderation').then((m) => m.SocialModeration),
      },
      {
        path: 'admin/metricas',
        canActivate: [RoleGuard],
        data: { roles: ['Administrador'] },
        loadComponent: () => import('./social-metrics').then((m) => m.SocialMetrics),
      },
      { path: '**', redirectTo: '' },
    ],
  },
];
