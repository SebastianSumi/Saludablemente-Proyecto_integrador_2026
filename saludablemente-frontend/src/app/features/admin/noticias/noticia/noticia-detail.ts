import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { NoticiaService, NoticiaPublicaService } from '../noticia-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { ImagenMini } from '../../../shared/visor-imagen/imagen-mini';
import { Noticia, urlImagenNoticia } from '../models';

@Component({
  selector: 'app-noticia-detail',
  imports: [RouterLink, TuiBadge, TuiButton, TuiButtonLoading, TuiIcon, ImagenMini],
  templateUrl: './noticia-detail.html',
  styleUrl: './noticia-detail.css',
})
export class NoticiaDetail implements OnInit {
  private readonly adminSrv = inject(NoticiaService);
  private readonly publicSrv = inject(NoticiaPublicaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly noticia = signal<Noticia | null>(null);
  protected readonly loading = signal(false);
  protected readonly procesando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly esAdmin = signal(false);

  private id = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam) {
        this.router.navigate(['/noticias']);
        return;
      }
      this.id = Number(idParam);
      this.esAdmin.set(this.router.url.startsWith('/noticias/admin'));
      this.noticia.set(null);
      this.cargar();
    });
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    const req = this.esAdmin()
      ? this.adminSrv.obtener(this.id)
      : this.publicSrv.detalle(this.id);
    req.subscribe({
      next: (n) => {
        this.noticia.set(n);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 404
            ? 'Noticia no encontrada.'
            : 'No se pudo cargar la noticia.'
        );
        this.loading.set(false);
      },
    });
  }

  transicion(accion: 'publicar' | 'despublicar' | 'archivar'): void {
    const etiquetas: Record<string, string> = {
      publicar: 'publicar',
      despublicar: 'devolver a borrador',
      archivar: 'archivar',
    };
    this.confirmar
      .confirmar(
        `¿Seguro que deseas ${etiquetas[accion]} esta noticia?`,
        accion === 'publicar' ? 'Publicar' : accion === 'archivar' ? 'Archivar' : 'Aceptar'
      )
      .subscribe((ok) => {
        if (!ok) return;
        this.procesando.set(true);
        this.adminSrv[accion](this.id).subscribe({
          next: (n) => {
            this.noticia.set(n);
            this.procesando.set(false);
          },
          error: () => {
            this.error.set('No se pudo cambiar el estado.');
            this.procesando.set(false);
          },
        });
      });
  }

  subirImagen(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    input.value = '';
    if (!archivo) return;
    const actual = this.noticia();
    this.confirmar
      .confirmar(
        actual?.imagenUrl
          ? `¿Reemplazar la imagen de portada por "${archivo.name}"?`
          : `¿Subir "${archivo.name}" como imagen de portada?`,
        'Subir imagen'
      )
      .subscribe((ok) => {
        if (!ok) return;
        this.procesando.set(true);
        this.error.set(null);
        this.adminSrv.subirImagen(this.id, archivo).subscribe({
          next: (n) => {
            this.noticia.set(n);
            this.procesando.set(false);
          },
          error: (err: HttpErrorResponse) => {
            this.error.set(err.error?.message ?? 'No se pudo subir la imagen.');
            this.procesando.set(false);
          },
        });
      });
  }

  eliminarImagen(): void {
    this.confirmar
      .confirmar('¿Eliminar la imagen de portada?', 'Eliminar')
      .subscribe((ok) => {
        if (!ok) return;
        this.procesando.set(true);
        this.adminSrv.eliminarImagen(this.id).subscribe({
          next: () => {
            this.cargar();
            this.procesando.set(false);
          },
          error: () => {
            this.error.set('No se pudo eliminar la imagen.');
            this.procesando.set(false);
          },
        });
      });
  }

  protected imagenDe(n: Noticia): string | undefined {
    return urlImagenNoticia(n.imagenUrl);
  }

  protected galeriaDe(n: Noticia): { src: string; titulo: string; subtitulo?: string }[] {
    const src = this.imagenDe(n);
    if (!src) return [];
    return [{ src, titulo: n.titulo, subtitulo: n.subtitulo }];
  }

  protected aparienciaEstado(e?: string): string {    switch (e) {
      case 'PUBLICADO':
        return 'positive';
      case 'ARCHIVADO':
        return 'neutral';
      default:
        return 'warning';
    }
  }
}
