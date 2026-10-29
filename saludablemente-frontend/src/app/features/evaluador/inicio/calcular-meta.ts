/** Avance de una meta sobre su rango inicial→objetivo, acotado a [0, 100]. */
export function calcularPorcentajeMeta(meta: {
  valorInicial: number;
  valorActual: number;
  valorObjetivo: number;
}): number {
  const rango = meta.valorObjetivo - meta.valorInicial;
  if (rango === 0) return meta.valorActual >= meta.valorObjetivo ? 100 : 0;
  const pct = ((meta.valorActual - meta.valorInicial) / rango) * 100;
  return Math.max(0, Math.min(100, Math.round(pct)));
}
