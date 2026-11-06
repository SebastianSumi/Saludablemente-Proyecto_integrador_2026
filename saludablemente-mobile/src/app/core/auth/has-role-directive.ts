import {
  Directive,
  TemplateRef,
  ViewContainerRef,
  effect,
  inject,
  input,
} from '@angular/core';
import { AuthService } from './auth-service';

@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective {
  readonly appHasRole = input<string | string[]>([], { alias: 'appHasRole' });

  private readonly tpl = inject(TemplateRef<unknown>);
  private readonly vcr = inject(ViewContainerRef);
  private readonly auth = inject(AuthService);

  constructor() {
    effect(() => {
      const raw = this.appHasRole();
      const lista = Array.isArray(raw) ? raw : [raw];
      if (this.auth.tieneAlgunRol(lista)) {
        this.vcr.clear();
        this.vcr.createEmbeddedView(this.tpl);
      } else {
        this.vcr.clear();
      }
    });
  }
}
