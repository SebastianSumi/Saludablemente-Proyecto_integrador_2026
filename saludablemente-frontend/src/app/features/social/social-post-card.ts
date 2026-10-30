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
import { TuiButton } from '@taiga-ui/core';
import { finalize, Observable, filter, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/auth/auth-service';
import { ConfirmService } from '../../core/services/confirm-service';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { TeamService } from '../admin/teams/team-service';
import { Team } from '../admin/teams/team.model';
import { SocialService } from './social-service';
import { PostResumen, Reacciones, Scope } from './social.model';
import { errorSocial } from './social.utils';
import { SocialText } from './social-text';
import { SocialReactions } from './social-reactions';
import { SocialReport } from './social-report';
import { SocialPostEditor } from './social-post-editor';
@Component({
  selector: 'social-post-card',
  imports: [
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
  styleUrl: './social.css',
  template: ` <article class="panel" [attr.aria-label]="p().titulo || 'Publicación'">
      <header class="identity">
        <app-avatar
          [foto]="avatar(p().autor.fotoUrl)"
          [iniciales]="initials(p().autor.nombreCompleto)"
          size="m"
        />
        <div class="grow">
          <a [routerLink]="['/social/usuarios', p().autor.idUsuario]"
            ><strong>{{ p().autor.nombreCompleto }}</strong></a
          ><small>{{ p().autor.rolPrincipal }} · {{ p().fechaPublicacion | date: 'medium' }}</small>
          @if (p().equipo; as team) {
            <a class="chip" [routerLink]="['/social/equipos', team.idEquipo]">{{ team.nombre }}</a>
          }
        </div>
        <span class="chip" [class.official]="p().esOficial">{{
          p().esOficial ? 'Noticia oficial' : tipoLabel(p().tipoContenido)
        }}</span>
      </header>
      @if (pinned()) {
        <p class="official">📌 Publicación fijada</p>
      }
      @if (p().titulo) {
        <h2 class="post-title">
          <a [routerLink]="['/social/posts', p().idPost]">{{ p().titulo }}</a>
        </h2>
      }
      <social-text [text]="p().resumen" />
      @if (p().imagenUrl) {
        <a [routerLink]="['/social/posts', p().idPost]"
          ><img
            class="post-photo"
            [src]="p().imagenUrl"
            alt="Imagen de la publicación"
            loading="lazy"
            (error)="imagen.hidden = true"
            #imagen
        /></a>
      }
      @if (p().tipoContenido === 'SHARE' && p().postOriginal; as original) {
        <section class="original">
          <small
            >Compartido de
            <a [routerLink]="['/social/usuarios', original.autor.idUsuario]">{{
              original.autor.nombreCompleto
            }}</a></small
          >
          <h3>
            <a [routerLink]="['/social/posts', original.idPost]">{{
              original.titulo || 'Ver publicación original'
            }}</a>
          </h3>
          <social-text [text]="original.resumen" />
          @if (original.imagenUrl) {
            <img
              class="post-photo"
              [src]="original.imagenUrl"
              alt="Imagen de la publicación original"
              loading="lazy"
            />
          }
        </section>
      }
      @if (p().tipoContenido === 'ACTIVIDAD') {
        <div class="post-meta">
          <span
            >{{ p().fechaEvento | date: 'longDate' }} {{ p().horaInicio }}–{{ p().horaFin }}</span
          ><span>{{ p().lugar }}</span
          ><span>{{ p().estado }}</span>
          @if (p().coordinadorNombre) {
            <span>Coordina: {{ p().coordinadorNombre }}</span>
          }
        </div>
        <a class="chip" [routerLink]="['/social/actividades', p().refId]">Muro de esta actividad</a>
      }
      @if (p().equipo?.lider; as leader) {
        <small
          >Líder del equipo:
          <a [routerLink]="['/social/usuarios', leader.idUsuario]">{{
            leader.nombreCompleto
          }}</a></small
        >
      }
      <social-reactions
        [id]="p().idPost"
        [value]="reaction()"
        (changed)="reactionChanged($event)"
      />
      <hr class="divider" />
      <div class="actions">
        <a tuiButton appearance="flat" size="s" [routerLink]="['/social/posts', p().idPost]"
          >{{ p().totalComentarios }} comentarios</a
        ><button
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          [disabled]="busy()"
          (click)="shareOpen.set(!shareOpen()); loadTeam()"
        >
          Compartir · {{ p().totalShares }}</button
        ><button
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          [disabled]="busy()"
          [attr.aria-pressed]="p().guardadoPorMi"
          (click)="save()"
        >
          {{ p().guardadoPorMi ? 'Guardado ✓' : 'Guardar' }}</button
        ><button
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          (click)="more.set(!more())"
          [attr.aria-expanded]="more()"
        >
          Opciones
        </button>
      </div>
      @if (more()) {
        <div class="actions">
          @if (p().puedoEditar) {
            <button tuiButton appearance="flat" size="s" type="button" (click)="editing.set(true)">
              Editar
            </button>
          }
          @if (p().puedoEliminar) {
            <button
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              [disabled]="busy()"
              (click)="remove()"
            >
              Eliminar
            </button>
          }
          <button
            tuiButton
            appearance="flat"
            size="s"
            type="button"
            (click)="reportOpen.set(!reportOpen())"
          >
            Reportar
          </button>
          @if (admin()) {
            <button
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              (click)="pinOpen.set(!pinOpen())"
            >
              Fijados</button
            ><button
              tuiButton
              appearance="flat"
              size="s"
              type="button"
              [disabled]="busy()"
              (click)="hide()"
            >
              Ocultar
            </button>
          }
        </div>
      }
      @if (error()) {
        <p role="alert" class="error">{{ error() }}</p>
      }
      @if (notice()) {
        <p class="notice" role="status">{{ notice() }}</p>
      }
      @if (shareOpen()) {
        <form class="panel fields" (ngSubmit)="share()">
          <h3>Compartir publicación</h3>
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
        </form>
      }
      @if (pinOpen()) {
        <form class="panel fields" (ngSubmit)="pin()">
          <h3>Fijar publicación</h3>
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
            >Expiración opcional<input name="expiracion" type="datetime-local" [(ngModel)]="expiry"
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
        </form>
      }
      @if (reportOpen()) {
        <social-report
          [post]="p().idPost"
          (cancel)="reportOpen.set(false)"
          (sent)="
            reportOpen.set(false);
            notice.set('Reporte enviado. Gracias por ayudarnos a cuidar este espacio.')
          "
        />
      }
    </article>
    @if (editing()) {
      <social-post-editor
        [postId]="p().idPost"
        (cancel)="editing.set(false)"
        (saved)="editing.set(false); changed.emit()"
      />
    }`,
})
export class SocialPostCard {
  readonly post = input.required<PostResumen>();
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
  protected readonly team = signal<Team | null>(null);
  protected shareText = '';
  protected shareTeam: number | null = null;
  protected pinScope: Scope = 'GLOBAL';
  protected pinId: number | null = null;
  protected expiry = '';
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
