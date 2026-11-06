import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../auth/auth-service';
import { homePorRol } from '../auth/roles';

@Component({
  selector:'app-mobile-navigation', imports:[RouterLink,RouterLinkActive],
  template:`
    <nav aria-label="Navegación móvil" class="mobile-bottom">
      <a [routerLink]="home()" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">Inicio</a>
      <a routerLink="/actividades" routerLinkActive="active">Actividades</a>
      <a routerLink="/social" routerLinkActive="active">Comunidad</a>
      <details><summary>Módulos</summary><div class="module-menu">
        @for (item of modules(); track item.path) {
          <a [routerLink]="item.path" (click)="close($event)">{{ item.label }}</a>
        }
      </div></details>
    </nav>`,
  styles:`
    :host{display:none}
    @media(max-width:768px){
      :host{display:block;height:calc(64px + env(safe-area-inset-bottom))}
      .mobile-bottom{position:fixed;bottom:0;left:0;right:0;z-index:100;display:flex;align-items:stretch;justify-content:space-around;background:var(--sal-card,#fff);border-top:1px solid var(--sal-line,#ddd);padding-bottom:env(safe-area-inset-bottom)}
      a,summary{display:block;padding:20px 10px;color:var(--sal-ink,#172132);text-decoration:none;font-size:12px;cursor:pointer;min-height:48px}
      .active{color:var(--sal-primary,#007acc);font-weight:700}
      .module-menu{position:absolute;bottom:calc(64px + env(safe-area-inset-bottom));left:12px;right:12px;max-height:65dvh;overflow:auto;background:var(--sal-card,#fff);border:1px solid var(--sal-line,#ddd);border-radius:16px;box-shadow:0 8px 32px #0002}
      .module-menu a{padding:14px 20px;border-bottom:1px solid var(--sal-line,#ddd)}
    }`,
})
export class MobileNavigation {
  private readonly auth=inject(AuthService);
  home() { return homePorRol(this.auth.obtenerRoles()); }
  close(event:Event) { (event.target as HTMLElement).closest('details')?.removeAttribute('open'); }
  modules() {
    const common=[{label:'Mi perfil',path:'/mi-perfil'},{label:'Mi equipo',path:'/mi-equipo'},{label:'Noticias',path:'/noticias'},{label:'Mis evaluaciones',path:'/colaborador/mis-evaluaciones'},{label:'Mis metas',path:'/colaborador/mis-metas'},{label:'Mis asistencias',path:'/colaborador/mis-asistencias'},{label:'Mis alertas',path:'/colaborador/mis-alertas'},{label:'Notificaciones',path:'/colaborador/mis-notificaciones'},{label:'Reportes personales',path:'/reportes'},{label:'Jornadas nutricionales',path:'/nutricional/jornadas'}];
    if(this.auth.tieneAlgunRol(['Evaluador'])) common.push({label:'Panel de evaluador',path:'/evaluador'});
    if(this.auth.tieneAlgunRol(['Administrador'])) common.push(
      {label:'Administración',path:'/admin'},{label:'Panel general',path:'/admin/panel'},
      {label:'Personas',path:'/personal/personas'},{label:'Equipos de Personal',path:'/personal/equipos'},
      {label:'Teams',path:'/teams'},{label:'Usuarios',path:'/seguridad/usuarios'},
      {label:'Áreas',path:'/catalogos/areas'},{label:'Períodos',path:'/catalogo/periodos'},
      {label:'Nutrición',path:'/nutricional/evaluaciones'},{label:'Aptitud física',path:'/aptitud'},
      {label:'Alertas clínicas',path:'/alertaclinica'},{label:'Metas',path:'/metas'},
      {label:'Asistencias',path:'/asistencias/registro'},{label:'Administrar noticias',path:'/noticias/admin'},
      {label:'Administrar notificaciones',path:'/notificaciones'},
      {label:'Moderación social',path:'/social/admin/reportes'},{label:'Métricas sociales',path:'/social/admin/metricas'});
    return common;
  }
}
