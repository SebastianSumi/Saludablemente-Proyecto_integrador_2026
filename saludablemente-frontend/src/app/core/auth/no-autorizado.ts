import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-autorizado',
  imports: [RouterLink],
  template: `
    <h2>Acceso no autorizado</h2>
    <p>Tu cuenta no tiene los permisos necesarios para ver esta página.</p>
    <a routerLink="/">Volver al inicio</a>
  `,
})
export class NoAutorizado {}
