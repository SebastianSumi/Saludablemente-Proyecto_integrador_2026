import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiTitle } from '@taiga-ui/core';
import { TuiAvatar, TuiBadge, TuiProgressBar } from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiTable } from '@taiga-ui/addon-table';
import { switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { MetaService } from '../../admin/metas/meta-service';
import type { Meta } from '../../admin/metas/models';

@Component({
  selector: 'app-col-meta-list',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiBadge, TuiAvatar, TuiProgressBar, TuiTable, TuiCardLarge, TuiHeader, TuiTitle, DatePipe],
  templateUrl: './meta-list.html',
  styleUrls: ['./meta-list.css'],
})
export class ColMetaList implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly metaSrv = inject(MetaService);

  protected readonly items = signal<Meta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.perfilSrv
      .obtener()
      .pipe(switchMap((p) => this.metaSrv.listarPorPersona(p.idPersona)))
      .subscribe({
        next: (data) => {
          this.items.set(data);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar tus metas.');
          this.loading.set(false);
        },
      });
  }

  protected estadoBadge(e: string): string {
    return e === 'COMPLETADA' ? 'positive' : e === 'ACTIVA' ? 'primary' : 'neutral';
  }
}
