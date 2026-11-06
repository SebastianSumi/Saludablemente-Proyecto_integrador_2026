import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-clinica-nav', standalone: true, imports: [RouterLink, RouterLinkActive],
  template: `<nav aria-label="Secciones de alertas clínicas">
    <a routerLink="/alertaclinica/alertas" routerLinkActive="active" ariaCurrentWhenActive="page">Alertas</a>
    <a routerLink="/alertaclinica/etiquetas" routerLinkActive="active" ariaCurrentWhenActive="page">Etiquetas clínicas</a>
    <a routerLink="/alertaclinica/parametros" routerLinkActive="active" ariaCurrentWhenActive="page">Parámetros</a>
    <a routerLink="/alertaclinica/reportes/etiquetas" routerLinkActive="active" ariaCurrentWhenActive="page">Reporte de etiquetas</a>
  </nav>`,
  styles: [`nav{display:flex;flex-wrap:wrap;gap:.4rem}a{padding:.65rem .9rem;min-height:44px;box-sizing:border-box;border-radius:12px;color:var(--sal-ink-soft);text-decoration:none;font-size:.85rem;font-weight:600;display:flex;align-items:center}a.active{background:var(--sal-card);color:var(--sal-action);box-shadow:inset 0 0 0 1px var(--sal-line)}a:hover{background:var(--sal-hover)}a:focus-visible{outline:2px solid var(--sal-action);outline-offset:2px}`],
})
export class ClinicaNav {}
