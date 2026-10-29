import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiCalendar,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import {
  TuiBadge,
  TuiButtonLoading,
  TuiChevron,
  TuiDataListWrapper,
  TuiComboBox,
  TuiInputDateTime,
} from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { SincronizacionService } from '../sincronizacion-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { LoteResponse, TipoAsistencia } from '../models';

interface FilaLote {
  idLocal: string;
  idActividad: string;
  tipo: TipoAsistencia;
  horaCaptura: string;
  codigo: string;
  codigoActividad: string;
}

@Component({
  selector: 'app-sincronizacion',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TuiBadge,
    TuiButton,
    TuiButtonLoading,
    TuiCalendar,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiInputDateTime,
    TuiLabel,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './sincronizacion.html',
  styleUrl: './sincronizacion.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal((item: unknown): string => String(item ?? '')),
      identityMatcher: signal((a: unknown, b: unknown): boolean => a === b),
    }),
  ],
})
export class Sincronizacion implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(SincronizacionService);
  private readonly confirmar = inject(ConfirmService);

  protected readonly ping = signal<{ status: string; timestamp: number } | null>(null);
  protected readonly filas = signal<FilaLote[]>([this.filaVacia()]);
  protected readonly resultado = signal<LoteResponse | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected readonly tipos: TipoAsistencia[] = ['CHECK_IN', 'CHECK_OUT', 'SIMPLE'];
  protected readonly flujos: string[] = ['COORDINADOR', 'AUTOSERVICIO'];

  protected readonly form = this.fb.nonNullable.group({
    dispositivoId: ['COORDINADOR-01', [Validators.required]],
    tipoFlujo: ['COORDINADOR', [Validators.required]],
  });

  ngOnInit(): void {
    this.service.ping().subscribe({
      next: (p) => this.ping.set(p),
      error: () => undefined,
    });
  }

  private filaVacia(): FilaLote {
    const ahora = new Date();
    const iso = new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);
    return {
      idLocal: crypto.randomUUID(),
      idActividad: '',
      tipo: 'CHECK_IN',
      horaCaptura: iso,
      codigo: '',
      codigoActividad: '',
    };
  }

  protected agregarFila(): void {
    this.filas.update((f) => [...f, this.filaVacia()]);
  }

  protected quitarFila(i: number): void {
    this.filas.update((f) => f.filter((_, idx) => idx !== i));
  }

  protected actualizarFila(i: number, campo: keyof FilaLote, valor: string): void {
    this.filas.update((f) => f.map((r, idx) => (idx === i ? { ...r, [campo]: valor } : r)));
  }

  protected enviar(): void {
    if (this.form.invalid || this.filas().length === 0) {
      this.form.markAllAsTouched();
      this.error.set('Completa dispositivo, flujo y al menos un registro.');
      return;
    }
    const v = this.form.getRawValue();
    const registros: {
      idLocal: string;
      idActividad: number;
      tipoAsistencia: TipoAsistencia;
      horaCaptura: string;
      codigoQrPersonal?: string;
      codigoQrActividad?: string;
    }[] = [];
    for (const f of this.filas()) {
      const idActividad = Number(f.idActividad);
      if (!Number.isInteger(idActividad) || idActividad <= 0) {
        this.error.set('Cada fila necesita un idActividad válido.');
        return;
      }
      if (!f.codigoActividad.trim()) {
        this.error.set('Cada fila necesita el QR de actividad vigente.');
        return;
      }
      registros.push({
        idLocal: f.idLocal,
        idActividad,
        tipoAsistencia: f.tipo,
        horaCaptura: new Date(f.horaCaptura).toISOString(),
        codigoQrActividad: f.codigoActividad.trim(),
        ...(v.tipoFlujo === 'COORDINADOR'
          ? { codigoQrPersonal: f.codigo.trim() || undefined }
          : {}),
      });
    }
    this.confirmar
      .confirmar(`¿Enviar lote con ${registros.length} registro(s)?`, 'Enviar lote')
      .subscribe((ok) => {
        if (!ok) return;
        this.enviando.set(true);
        this.error.set(null);
        this.resultado.set(null);
        this.service
          .procesarLote({
            dispositivoId: v.dispositivoId.trim(),
            tipoFlujo: v.tipoFlujo as 'COORDINADOR' | 'AUTOSERVICIO',
            registros,
          })
          .subscribe({
            next: (r) => {
              this.resultado.set(r);
              this.enviando.set(false);
            },
            error: (err: HttpErrorResponse) => {
              this.error.set(err.error?.message ?? 'No se pudo procesar el lote.');
              this.enviando.set(false);
            },
          });
      });
  }

  protected aparienciaResultado(r: string): string {
    switch (r) {
      case 'SYNCED':
        return 'positive';
      case 'CUARENTENA':
        return 'warning';
      default:
        return 'negative';
    }
  }
}