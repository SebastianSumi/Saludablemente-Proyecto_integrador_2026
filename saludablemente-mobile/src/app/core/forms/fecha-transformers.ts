import type { AbstractControl } from '@angular/forms';
import type { Provider } from '@angular/core';
import { tuiDateFormatProvider } from '@taiga-ui/core';
import {
  tuiInputDateOptionsProvider,
  tuiInputDateTimeOptionsProvider,
  tuiInputTimeOptionsProvider,
} from '@taiga-ui/kit';
import { TuiValueTransformer } from '@taiga-ui/cdk/classes';
import { TuiDay, TuiTime } from '@taiga-ui/cdk/date-time';

/**
 * Transformers ISO string <-> objetos Taiga.
 * Los formularios siguen guardando strings ('YYYY-MM-DD', 'HH:mm',
 * 'YYYY-MM-DDTHH:mm') tal como los espera el backend; solo los templates
 * usan los pickers de calendario de Taiga UI.
 */

const FECHA_ISO = /^(\d{4})-(\d{2})-(\d{2})$/;
const HORA = /^(\d{2}):(\d{2})(?::(\d{2}))?$/;

export class IsoDateTransformer extends TuiValueTransformer<TuiDay | null, unknown> {
  public fromControlValue(controlValue: unknown): TuiDay | null {
    if (typeof controlValue !== 'string') return null;
    const m = FECHA_ISO.exec(controlValue.slice(0, 10));
    if (!m) return null;
    try {
      const dia = TuiDay.jsonParse(`${m[1]}-${m[2]}-${m[3]}`);
      return TuiDay.isValidDay(dia.year, dia.month, dia.day) ? dia : null;
    } catch {
      return null;
    }
  }

  public toControlValue(componentValue: TuiDay | null): unknown {
    return componentValue ? componentValue.toJSON() : '';
  }
}

export class HoraTransformer extends TuiValueTransformer<TuiTime | null, unknown> {
  public fromControlValue(controlValue: unknown): TuiTime | null {
    if (typeof controlValue !== 'string') return null;
    const m = HORA.exec(controlValue.trim());
    if (!m) return null;
    const h = Number(m[1]);
    const min = Number(m[2]);
    return TuiTime.isValidTime(h, min) ? new TuiTime(h, min) : null;
  }

  public toControlValue(componentValue: TuiTime | null): unknown {
    return componentValue ? componentValue.toString('HH:MM') : '';
  }
}

/**
 * Providers de formatos/transformers para tests unitarios.
 * El TestBed no carga app.config, así que los specs que rendericen
 * pickers de fecha/hora deben incluir provideFormatosFecha().
 */
export function provideFormatosFecha(): Provider[] {
  return [
    tuiDateFormatProvider({ mode: 'yyyy/mm/dd', separator: '-' }),
    tuiInputDateOptionsProvider({ valueTransformer: new IsoDateTransformer() }),
    tuiInputTimeOptionsProvider({ valueTransformer: new HoraTransformer() }),
    tuiInputDateTimeOptionsProvider({ valueTransformer: new FechaHoraTransformer() }),
  ];
}

/**
 * Helpers para los pies de calendario (Hoy / Limpiar).
 * Úsalos desde métodos del componente:
 *   fijarHoy(campo: string): void { fijarHoyEn(this.form.get(campo)); }
 */
export function fijarHoyEn(control: AbstractControl | null | undefined): void {
  if (!control) return;
  control.setValue(TuiDay.currentLocal().toJSON());
  control.markAsTouched();
}

export function limpiarFechaEn(control: AbstractControl | null | undefined): void {
  if (!control) return;
  control.setValue('');
  control.markAsTouched();
}

export class FechaHoraTransformer extends TuiValueTransformer<
  [TuiDay, TuiTime | null] | null,
  unknown
> {
  private readonly fecha = new IsoDateTransformer();
  private readonly hora = new HoraTransformer();

  public fromControlValue(controlValue: unknown): [TuiDay, TuiTime | null] | null {
    if (typeof controlValue !== 'string' || !controlValue) return null;
    const [f, h] = controlValue.split('T');
    const dia = this.fecha.fromControlValue(f);
    if (!dia) return null;
    return [dia, h ? this.hora.fromControlValue(h) : null];
  }

  public toControlValue(componentValue: [TuiDay, TuiTime | null] | null): unknown {
    if (!componentValue) return '';
    const [dia, hora] = componentValue;
    const f = this.fecha.toControlValue(dia);
    if (typeof f !== 'string' || !f) return '';
    const h = this.hora.toControlValue(hora);
    return typeof h === 'string' && h ? `${f}T${h}` : f;
  }
}
