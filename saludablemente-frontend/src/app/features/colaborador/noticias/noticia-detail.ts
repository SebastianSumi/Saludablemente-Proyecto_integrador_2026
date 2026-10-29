import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiTitle } from '@taiga-ui/core';
import { TuiAvatar, TuiBadge } from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { NoticiaPublicaService } from '../../admin/noticias/noticia-service';
import { ImagenMini } from '../../shared/visor-imagen/imagen-mini';
import { urlImagenNoticia, type Noticia } from '../../admin/noticias/models';

@Component({
  selector: 'app-col-noticia-detail',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiBadge, TuiAvatar, TuiCardLarge, TuiHeader, TuiTitle, DatePipe, ImagenMini],
  templateUrl: './noticia-detail.html',
  styleUrls: ['./noticia-feed.css'],
})
export class ColNoticiaDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly perfilSrv = inject(PerfilService);
  private readonly feedSrv = inject(NoticiaPublicaService);

  protected readonly noticia = signal<Noticia | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected imagenDe(n: Noticia): string | undefined {
    return urlImagenNoticia(n.imagenUrl);
  }

  protected galeriaDe(n: Noticia): { src: string; titulo: string; subtitulo?: string }[] {
    const src = this.imagenDe(n);
    if (!src) return [];
    return [{ src, titulo: n.titulo, subtitulo: n.subtitulo }];
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.error.set('Identificador inválido.');
      return;
    }
    this.loading.set(true);
    this.perfilSrv
      .obtener()
      .pipe(switchMap((p) => this.feedSrv.detalle(id, p.idPersona)))
      .subscribe({
        next: (n) => {
          this.noticia.set(n);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar la noticia.');
          this.loading.set(false);
        },
      });
  }
}
