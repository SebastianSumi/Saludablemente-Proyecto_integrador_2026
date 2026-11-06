import { Component, computed, effect, input, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';

/**
 * Resuelve la URL visible de un avatar: absoluta tal cual, nombre suelto
 * contra el prefijo de avatares. Sin esto, un nombre suelto se pide al
 * frontend y da 404.
 */
export function urlAvatarFoto(foto?: string | null): string | undefined {
  if (!foto || foto.trim() === '') return undefined;
  const valor = foto.trim();
  if (/^https?:\/\//i.test(valor) || valor.startsWith('data:')) return valor;
  const base = environment.minio.publicUrl.replace(/\/+$/, '');
  const objeto = valor.replace(/^\/+/, '');
  const conPrefijo = objeto.includes('/') ? objeto : `avatares/${objeto}`;
  return `${base}/${environment.minio.bucketNoticias}/${conPrefijo}`;
}

/**
 * Avatar con foto real e iniciales como respaldo.
 * Taiga `tuiAvatar` no soporta fotos (solo iconos/texto), por eso este
 * componente: muestra <img> y si falla la carga cae a iniciales.
 */
@Component({
  selector: 'app-avatar',
  template: `
    @if (foto() && !fallo()) {
      <img
        [src]="foto()"
        [alt]="iniciales()"
        [style.border-radius]="shape() === 'square' ? '10px' : '50%'"
        [style.width.px]="px()"
        [style.height.px]="px()"
        [style.object-fit]="fit()"
        class="avatar-foto"
        (error)="fallo.set(true)"
      />
    } @else {
      <span
        class="avatar-iniciales"
        [style.border-radius]="shape() === 'square' ? '10px' : '50%'"
        [style.width.px]="px()"
        [style.height.px]="px()"
        [style.font-size.px]="px() * 0.36"
      >
        {{ iniciales() }}
      </span>
    }
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        flex-shrink: 0;
      }
      .avatar-foto {
        object-fit: cover;
        aspect-ratio: 1;
        border-radius: 50%;
        display: block;
        background: var(--sal-hover);
      }
      .avatar-iniciales {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: var(--sal-accent);
        color: #fff;
        font-weight: 700;
        letter-spacing: 0.02em;
        user-select: none;
      }
    `,
  ],
})
export class Avatar {
  constructor() {
    effect(() => {
      this.foto();
      this.fallo.set(false);
    });
  }
  readonly foto = input<string | null | undefined>(null);
  readonly iniciales = input<string>('?');
  readonly shape = input<'circle' | 'square'>('circle');
  readonly fit = input<'cover' | 'contain'>('cover');
  readonly size = input<'s' | 'm' | 'l' | 'xl' | 'xxl'>('m');

  protected readonly fallo = signal(false);

  protected readonly px = computed(() => {
    switch (this.size()) {
      case 's':
        return 32;
      case 'l':
        return 56;
      case 'xl':
        return 72;
      case 'xxl':
        return 96;
      default:
        return 40;
    }
  });
}
