import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiDialogService, TuiIcon, TuiTitle } from '@taiga-ui/core';
import { TuiAvatar, TuiProgressBar } from '@taiga-ui/kit';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { MetaService } from '../../admin/metas/meta-service';
import { CheckinModal } from '../../admin/metas/meta/checkin-modal';
import type { ProgresoMeta } from '../../admin/metas/models';

@Component({
  selector: 'app-col-meta-detail',
  standalone: true,
  imports: [RouterLink, TuiButton, TuiIcon, TuiAvatar, TuiProgressBar, TuiCardLarge, TuiHeader, TuiTitle, DatePipe],
  templateUrl: './meta-detail.html',
  styleUrls: ['./meta-list.css'],
})
export class ColMetaDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly perfilSrv = inject(PerfilService);
  private readonly metaSrv = inject(MetaService);
  private readonly dialogs = inject(TuiDialogService);

  private idMeta = 0;

  protected readonly progreso = signal<ProgresoMeta | null>(null);
  protected readonly historial = signal<ProgresoMeta[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.error.set('Identificador inválido.');
      return;
    }
    this.idMeta = id;
    this.cargar();
  }

  registrarCheckin(): void {
    const p = this.progreso();
    if (!p) return;
    this.dialogs
      .open<ProgresoMeta | null>(new PolymorpheusComponent(CheckinModal), {
        label: 'Registrar check-in',
        data: { idMeta: p.idMeta, idPersona: p.idPersona, modalidad: p.modalidad },
      })
      .subscribe({
        next: (res) => {
          if (res) {
            this.progreso.set(res);
            this.cargarHistorial();
          }
        },
        error: () => undefined,
      });
  }

  private cargarHistorial(): void {
    this.metaSrv
      .historial(this.idMeta)
      .pipe(catchError(() => of([] as ProgresoMeta[])))
      .subscribe((h) => this.historial.set(h));
  }

  private cargar(): void {
    const id = this.idMeta;
    this.loading.set(true);
    this.perfilSrv
      .obtener()
      .pipe(
        switchMap((p) =>
          forkJoin({
            prog: this.metaSrv.progreso(id),
            hist: this.metaSrv.historial(id).pipe(catchError(() => of([] as ProgresoMeta[]))),
          }).pipe(
            map((r) => {
              if (r.prog.idPersona !== p.idPersona) throw new Error('Esta meta no pertenece a tu persona.');
              return r;
            }),
          ),
        ),
      )
      .subscribe({
        next: (r) => {
          this.progreso.set(r.prog);
          this.historial.set(r.hist);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse | Error) => {
          this.error.set(
            err instanceof Error && !(err instanceof HttpErrorResponse)
              ? err.message
              : 'No se pudo cargar tu meta.',
          );
          this.loading.set(false);
        },
      });
  }
}
