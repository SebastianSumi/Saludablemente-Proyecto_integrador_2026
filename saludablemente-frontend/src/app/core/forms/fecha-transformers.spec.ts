import { TuiDay, TuiTime } from '@taiga-ui/cdk/date-time';
import {
  FechaHoraTransformer,
  HoraTransformer,
  IsoDateTransformer,
} from './fecha-transformers';

describe('IsoDateTransformer', () => {
  const t = new IsoDateTransformer();

  it('convierte ISO a TuiDay y de vuelta sin perder el formato', () => {
    const dia = t.fromControlValue('2026-10-09');
    expect(dia).toBeInstanceOf(TuiDay);
    expect([dia?.year, dia?.month, dia?.day]).toEqual([2026, 9, 9]);
    expect(t.toControlValue(dia)).toBe('2026-10-09');
  });

  it('acepta strings con hora (toma solo la fecha)', () => {
    expect(t.toControlValue(t.fromControlValue('2026-10-09T14:30'))).toBe('2026-10-09');
  });

  it('nulo o inválido no rompe el required', () => {
    expect(t.fromControlValue('')).toBeNull();
    expect(t.fromControlValue(null)).toBeNull();
    expect(t.fromControlValue('no-fecha')).toBeNull();
    expect(t.fromControlValue('2026-13-99')).toBeNull();
    expect(t.toControlValue(null)).toBe('');
  });
});

describe('HoraTransformer', () => {
  const t = new HoraTransformer();

  it('convierte HH:mm a TuiTime y de vuelta', () => {
    const hora = t.fromControlValue('14:30');
    expect(hora).toBeInstanceOf(TuiTime);
    expect(t.toControlValue(hora)).toBe('14:30');
  });

  it('nulo o inválido devuelve vacío', () => {
    expect(t.fromControlValue('')).toBeNull();
    expect(t.fromControlValue('25:99')).toBeNull();
    expect(t.toControlValue(null)).toBe('');
  });
});

describe('FechaHoraTransformer', () => {
  const t = new FechaHoraTransformer();

  it('convierte datetime-local en ida y vuelta', () => {
    const valor = t.fromControlValue('2026-10-09T14:30');
    expect(valor?.[0]).toBeInstanceOf(TuiDay);
    expect(t.toControlValue(valor)).toBe('2026-10-09T14:30');
  });

  it('fecha sola sobrevive el ida y vuelta', () => {
    expect(t.toControlValue(t.fromControlValue('2026-10-09'))).toBe('2026-10-09');
  });

  it('vacío devuelve vacío', () => {
    expect(t.fromControlValue('')).toBeNull();
    expect(t.toControlValue(null)).toBe('');
  });
});
