import { SocialGallery } from './social-gallery';
import { SocialIcon } from './social-icon';
import { SocialActivityMeta } from './social-activity-meta';
import { SocialModal } from './social-modal';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiDropdown, TuiDataList } from '@taiga-ui/core';
import { finalize, Observable, filter, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/auth/auth-service';
import { ConfirmService } from '../../core/services/confirm-service';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { TeamService } from '../admin/teams/team-service';
import { Team } from '../admin/teams/team.model';
import { SocialService } from './social-service';
import { PostResumen, Reacciones, Scope } from './social.model';
import { imagenesPublicacion, errorSocial } from './social.utils';
import { SocialText } from './social-text';
import { SocialReactions } from './social-reactions';
import { SocialReport } from './social-report';
import { SocialPostEditor } from './social-post-editor';
@Component({
  selector: 'social-post-card',
  imports: [
    SocialGallery,
    SocialIcon,
    SocialActivityMeta,
    TuiDropdown,
    TuiDataList,
    SocialModal,
    DatePipe,
    FormsModule,
    RouterLink,
    TuiButton,
    Avatar,
    SocialText,
    SocialReactions,
    SocialReport,
    SocialPostEditor,
  ],
  styleUrls: ['./social.css', './social-card.css'],
  template: `<article class="panel post-card" [attr.aria-label]="p().titulo || 'Publicación'">
      <header class="identity post-header">
        <app-avatar
          [foto]="avatar(p().autor.fotoUrl)"
          [iniciales]="initials(p().autor.nombreCompleto)"
          size="m"
        />
        <div class="grow">
          <a class="author-name" [routerLink]="['/social/usuarios', p().autor.idUsuario]">{{
            p().autor.nombreCompleto
          }}</a
          ><small
            ><time>{{ p().fechaPublicacion | date: 'dd/MM/yyyy · HH:mm' }}</time>
            @if (p().autor.rolPrincipal) {
              <span class="meta-dot">·</span>{{ p().autor.rolPrincipal }}
            }
          </small>
          @if (p().equipo; as team) {
            <a class="team-tag" [routerLink]="['/social/equipos', team.idEquipo]"
              ><social-icon name="users" />{{ team.nombre }}</a
            >
          }
        </div>
        <button
          tuiButton
          type="button"
          size="s"
          appearance="flat"
          class="icon-button"
          aria-label="Opciones de publicación"
          title="Opciones de publicación"
          [attr.aria-expanded]="more()"
          [tuiDropdown]="postOptions"
          [tuiDropdownOpen]="more()"
          (tuiDropdownOpenChange)="more.set($event)"
        >
          <social-icon name="ellipsis" /><span class="sr-only">Opciones</span>
        </button>
      </header>
      <div class="publication-heading">
        @if (p().tipoContenido === 'ACTIVIDAD' && p().imagenUrl) {
          <img
            class="activity-logo"
            [src]="p().imagenUrl"
            [alt]="'Logo de ' + p().titulo"
            (error)="logo.hidden = true"
            #logo
          />
        }
        <div class="publication-heading-text">
          @if (p().titulo) {
            <h2 class="post-title">
              <a [routerLink]="['/social/posts', p().idPost]">{{ p().titulo }}</a>
            </h2>
          }
          <div class="post-labels">
            <span class="chip" [class.official]="p().esOficial"
              ><social-icon
                [name]="
                  p().esOficial
                    ? 'badge-check'
                    : p().tipoContenido === 'ACTIVIDAD'
                      ? 'calendar'
                      : p().tipoContenido === 'SHARE'
                        ? 'share-2'
                        : 'file-text'
                "
              />{{ p().esOficial ? 'Noticia oficial' : tipoLabel(p().tipoContenido) }}</span
            >
            @if (pinned()) {
              <span class="chip official"><social-icon name="pin" />Publicación fijada</span>
            }
          </div>
        </div>
      </div>
      <social-text [text]="p().resumen ?? (p().cuerpo ? p().cuerpo!.slice(0, 280) : null)" [truncate]="!detail()" />
      @if (!p().resumen && p().cuerpo) {
        <a class="ver-mas" [routerLink]="['/social/posts', p().idPost]">Ver más</a>
      }
      @if (p().tipoContenido === 'ACTIVIDAD') {
        <social-activity-meta [post]="p()" /><a
          class="activity-link"
          [routerLink]="['/social/actividades', p().refId]"
          >Muro de esta actividad <social-icon name="arrow-up-right"
        /></a>
      }
      @if (gallery(p()).length) {
        <social-gallery
          class="post-gallery"
          [urls]="gallery(p())"
          [title]="p().titulo || 'Imágenes de la publicación'"
        />
      }
      @if (p().tipoContenido === 'SHARE' && p().postOriginal; as original) {
        <section class="original">
          <small
            ><social-icon name="share-2" /> Compartido de
            <a [routerLink]="['/social/usuarios', original.autor.idUsuario]">{{
              original.autor.nombreCompleto
            }}</a></small
          >
          <h3>
            <a [routerLink]="['/social/posts', original.idPost]">{{
              original.titulo || 'Ver publicación original'
            }}</a>
          </h3>
          <social-text [text]="original.resumen" [truncate]="!detail()" />
          @if (gallery(original).length) {
            <social-gallery
              [urls]="gallery(original)"
              [title]="original.titulo || 'Publicación original'"
            />
          }
        </section>
      }
      @if (p().equipo?.lider; as leader) {
        <p class="team-leader">
          <social-icon name="user-check" /><span
            >Líder del equipo:
            <a [routerLink]="['/social/usuarios', leader.idUsuario]">{{
              leader.nombreCompleto
            }}</a></span
          >
        </p>
      }
      <footer class="post-toolbar">
        <social-reactions
          [id]="p().idPost"
          [value]="reaction()"
          (changed)="reactionChanged($event)"
        /><a
          tuiButton
          appearance="flat"
          size="s"
          [routerLink]="['/social/posts', p().idPost]"
          [queryParams]="{ comentar: 1 }"
          aria-label="Comentar"
          title="Comentar"
          ><social-icon name="message-circle" /><span>{{ p().totalComentarios }}</span></a
        ><button
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          [disabled]="busy()"
          (click)="shareOpen.set(true); loadTeam()"
        >
          <social-icon name="share-2" />Compartir · {{ p().totalShares }}
        </button>
      </footer>
      <ng-template #postOptions>
        <tui-data-list class="post-options option-list" aria-label="Opciones de publicación">
          <button
            tuiOption
            tuiButton
            appearance="flat"
            size="s"
            type="button"
            [disabled]="busy()"
            (click)="save(); more.set(false)"
          >
            <social-icon [name]="p().guardadoPorMi ? 'bookmark-check' : 'bookmark'" />{{
              p().guardadoPorMi ? 'Guardado ✓' : 'Guardar'
            }}
          </button>
          @if (p().puedoEditar) {
            <button
              tuiOption
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              (click)="more.set(false); editing.set(true)"
            >
              <social-icon name="pencil" />Editar
            </button>
          }
          @if (p().puedoEliminar) {
            <button
              tuiOption
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              [disabled]="busy()"
              (click)="more.set(false); remove()"
            >
              <social-icon name="trash-2" />Eliminar
            </button>
          }
          <button
            tuiOption
            tuiButton
            appearance="flat"
            size="s"
            type="button"
            (click)="more.set(false); reportOpen.set(true)"
          >
            <social-icon name="flag" />Reportar
          </button>
          @if (admin()) {
            <button
              tuiOption
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              (click)="more.set(false); pinOpen.set(true)"
            >
              <social-icon name="pin" />Fijados</button
            ><button
              tuiOption
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              [disabled]="busy()"
              (click)="more.set(false); hide()"
            >
              <social-icon name="eye-off" />Ocultar
            </button>
          }
        </tui-data-list>
      </ng-template>
      @if (error()) {
        <p role="alert" class="error">{{ error() }}</p>
      }
      @if (notice()) {
        <p class="notice" role="status">{{ notice() }}</p>
      }
      @if (shareOpen()) {
        <social-modal title="Compartir publicación" [busy]="busy()" (closed)="shareOpen.set(false)">
          <form class="fields" (ngSubmit)="share()">
            @if (error()) {
              <p class="error" role="alert">{{ error() }}</p>
            }

            <label
              >Texto opcional<textarea
                name="contenido"
                [(ngModel)]="shareText"
                maxlength="500"
                rows="3"
              ></textarea></label
            ><label
              >Compartir como<select name="equipo" [(ngModel)]="shareTeam">
                <option [ngValue]="null">A título personal</option>
                @if (team(); as t) {
                  <option [ngValue]="t.id">{{ t.nombre }}</option>
                }
              </select></label
            >
            <div class="actions">
              <button tuiButton size="s" type="submit" [disabled]="busy()">Compartir</button
              ><button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                (click)="shareOpen.set(false)"
              >
                Cancelar
              </button>
            </div>
          </form></social-modal
        >
      }
      @if (pinOpen()) {
        <social-modal title="Gestionar fijado" [busy]="busy()" (closed)="pinOpen.set(false)">
          <form class="fields" (ngSubmit)="pin()">
            @if (error()) {
              <p class="error" role="alert">{{ error() }}</p>
            }

            <label
              >Ámbito<select name="scope" [(ngModel)]="pinScope">
                <option value="GLOBAL">Global</option>
                <option value="EQUIPO">Equipo</option>
                <option value="ACTIVIDAD">Actividad</option>
              </select></label
            >
            @if (pinScope !== 'GLOBAL') {
              <label
                >Identificador del ámbito<input
                  name="scopeId"
                  type="number"
                  min="1"
                  required
                  [(ngModel)]="pinId"
              /></label>
            }
            <label
              >Expiración opcional<input
                name="expiracion"
                type="datetime-local"
                [(ngModel)]="expiry"
            /></label>
            <div class="actions">
              <button tuiButton size="s" type="submit" [disabled]="busy()">Fijar</button
              ><button
                tuiButton
                size="s"
                type="button"
                appearance="secondary"
                [disabled]="busy()"
                (click)="unpin()"
              >
                Quitar de este ámbito</button
              ><button
                tuiButton
                size="s"
                type="button"
                appearance="flat"
                (click)="pinOpen.set(false)"
              >
                Cerrar
              </button>
            </div>
          </form></social-modal
        >
      }
      @if (reportOpen()) {
        <social-modal
          title="Reportar publicación"
          [busy]="childBusy()"
          (closed)="reportOpen.set(false)"
        >
          <social-report
            (pending)="childBusy.set($event)"
            [post]="p().idPost"
            (cancel)="reportOpen.set(false)"
            (sent)="
              reportOpen.set(false);
              notice.set('Reporte enviado. Gracias por ayudarnos a cuidar este espacio.')
            "
        /></social-modal>
      }
    </article>
    @if (editing()) {
      <social-modal title="Editar publicación" [busy]="childBusy()" (closed)="editing.set(false)">
        <social-post-editor
          (pending)="childBusy.set($event)"
          [postId]="p().idPost"
          (cancel)="editing.set(false)"
          (saved)="editing.set(false); changed.emit()"
      /></social-modal>
    }`,
})
export class SocialPostCard {
  readonly post = input.required<PostResumen>();
  readonly detail = input(false);
  readonly pinned = input(false);
  readonly scope = input<Scope>('GLOBAL');
  readonly scopeId = input<number | null>(null);
  readonly changed = output<void>();
  readonly deleted = output<void>();
  private readonly api = inject(SocialService);
  private readonly auth = inject(AuthService);
  private readonly confirm = inject(ConfirmService);
  private readonly teams = inject(TeamService);
  private readonly destroy = inject(DestroyRef);
  private readonly patch = signal<Partial<PostResumen>>({});
  protected readonly p = computed(() => ({ ...this.post(), ...this.patch() }));
  protected readonly reaction = computed<Reacciones>(() => ({
    total: this.p().totalReacciones,
    porTipo: this.p().reaccionesPorTipo,
    reaccionDelUsuario: this.p().reaccionDelUsuario,
  }));
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly notice = signal('');
  protected readonly more = signal(false);
  protected readonly shareOpen = signal(false);
  protected readonly pinOpen = signal(false);
  protected readonly reportOpen = signal(false);
  protected readonly editing = signal(false);
  protected readonly childBusy = signal(false);
  protected readonly team = signal<Team | null>(null);
  protected shareText = '';
  protected shareTeam: number | null = null;
  protected pinScope: Scope = 'GLOBAL';
  protected pinId: number | null = null;
  protected expiry = '';
  protected readonly gallery = imagenesPublicacion;
  protected readonly avatar = urlAvatarFoto;
  constructor() {
    effect(() => {
      this.post();
      this.patch.set({});
    });
    effect(() => {
      this.pinScope = this.scope();
      this.pinId = this.scopeId();
    });
  }
  protected admin() {
    return this.auth.tieneRol('Administrador');
  }
  protected initials(name: string) {
    return (
      name
        ?.split(' ')
        .slice(0, 2)
        .map((x) => x[0])
        .join('') || '?'
    );
  }
  protected tipoLabel(tipo: string) {
    return (
      {
        MANUAL: 'Publicación',
        ACTIVIDAD: 'Actividad',
        NOTICIA: 'Noticia',
        SHARE: 'Compartido',
      } as Record<string, string>
    )[tipo];
  }
  protected reactionChanged(r: Reacciones) {
    this.patch.update((p) => ({
      ...p,
      totalReacciones: r.total,
      reaccionesPorTipo: r.porTipo,
      reaccionDelUsuario: r.reaccionDelUsuario,
    }));
  }
  protected execute(op: Observable<unknown>, done: () => void) {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.notice.set('');
    op.pipe(
      takeUntilDestroyed(this.destroy),
      finalize(() => this.busy.set(false)),
    ).subscribe({ next: () => done(), error: (e) => this.error.set(errorSocial(e)) });
  }
  protected save() {
    const next = !this.p().guardadoPorMi;
    this.execute(this.api.guardar(this.p().idPost, next), () =>
      this.patch.update((p) => ({ ...p, guardadoPorMi: next })),
    );
  }
  protected loadTeam() {
    if (this.team()) return;
    this.teams
      .miEquipo()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (t) => this.team.set(t.activo ? t : null),
        error: () => this.team.set(null),
      });
  }
  protected share() {
    this.execute(
      this.api.compartir(this.p().idPost, this.shareText.trim() || null, this.shareTeam),
      () => {
        this.shareOpen.set(false);
        this.notice.set('Publicación compartida.');
        this.changed.emit();
      },
    );
  }
  protected remove() {
    this.confirm
      .confirmar('¿Eliminar esta publicación?', 'Eliminar')
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.eliminar(this.p().idPost)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({ next: () => this.deleted.emit(), error: (e) => this.error.set(errorSocial(e)) });
  }
  protected hide() {
    this.confirm
      .confirmar('¿Ocultar esta publicación para toda la comunidad?', 'Ocultar')
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.moderar(this.p().idPost, false)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({ next: () => this.deleted.emit(), error: (e) => this.error.set(errorSocial(e)) });
  }
  private validScope() {
    if (
      this.pinScope !== 'GLOBAL' &&
      (!this.pinId || !Number.isSafeInteger(this.pinId) || this.pinId < 1)
    ) {
      this.error.set('Selecciona un identificador válido para el ámbito.');
      return false;
    }
    return true;
  }
  protected pin() {
    if (!this.validScope()) return;
    if (this.expiry && new Date(this.expiry) <= new Date()) {
      this.error.set('La expiración debe ser futura.');
      return;
    }
    this.execute(
      this.api.fijar(this.p().idPost, {
        scopeTipo: this.pinScope,
        scopeId: this.pinScope === 'GLOBAL' ? null : this.pinId,
        fechaExpiracion: this.expiry ? `${this.expiry}:00` : null,
      }),
      () => {
        this.pinOpen.set(false);
        this.changed.emit();
      },
    );
  }
  protected unpin() {
    if (!this.validScope()) return;
    this.execute(
      this.api.desfijar(
        this.p().idPost,
        this.pinScope,
        this.pinScope === 'GLOBAL' ? null : this.pinId,
      ),
      () => {
        this.pinOpen.set(false);
        this.changed.emit();
      },
    );
  }
}
