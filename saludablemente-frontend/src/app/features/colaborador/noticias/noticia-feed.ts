import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiTitle } from '@taiga-ui/core';
import { TuiAvatar } from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { NoticiaPublicaService } from '../../admin/noticias/noticia-service';
import type { NoticiaFeed } from '../../admin/noticias/models';

@Component({
  selector: 'app-col-noticia-feed',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiAvatar, TuiCardLarge, TuiHeader, TuiTitle, DatePipe],
  templateUrl: './noticia-feed.html',
  styleUrls: ['./noticia-feed.css'],
})
export class ColNoticiaFeed implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly feedSrv = inject(NoticiaPublicaService);

  protected readonly items = signal<NoticiaFeed[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.perfilSrv
      .obtener()
      .pipe(switchMap((p) => this.feedSrv.feed({ idPersona: p.idPersona, size: 20 })))
      .subscribe({
        next: (pag) => {
          this.items.set(pag.content);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las noticias.');
          this.loading.set(false);
        },
      });
  }
}
