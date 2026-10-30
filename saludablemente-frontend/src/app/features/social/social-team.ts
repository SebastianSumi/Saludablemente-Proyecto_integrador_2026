import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EquipoSocial } from './social.model';
@Component({
  selector: 'social-team',
  imports: [RouterLink],
  styleUrl: './social.css',
  template: ` @if (equipo(); as e) {
    <section class="panel">
      <div class="identity">
        @if (e.imagenUrl) {
          <img
            class="team-image"
            [src]="e.imagenUrl"
            alt="Imagen del equipo"
            (error)="foto.hidden = true"
            #foto
          />
        }
        <div>
          <span class="eyebrow">Nuestro equipo</span>
          <h2>
            <a [routerLink]="['/social/equipos', e.idEquipo]">{{ e.nombre }}</a>
          </h2>
          <p class="muted">{{ e.slogan }}</p>
        </div>
      </div>
      <p>{{ e.descripcion }}</p>
      @if (e.textoVersiculo) {
        <blockquote>
          {{ e.textoVersiculo }} <cite>{{ e.referenciaBiblica }}</cite>
        </blockquote>
      }
      @if (e.lider; as l) {
        <p>
          Líder: <a [routerLink]="['/social/usuarios', l.idUsuario]">{{ l.nombreCompleto }}</a>
          @if (!l.activo) {
            <small> · No disponible</small>
          }
        </p>
      } @else {
        <p class="muted">Líder no asignado.</p>
      }
      <small>{{ e.totalMiembros }} integrantes</small>
    </section>
  }`,
})
export class SocialTeam {
  readonly equipo = input.required<EquipoSocial>();
}
