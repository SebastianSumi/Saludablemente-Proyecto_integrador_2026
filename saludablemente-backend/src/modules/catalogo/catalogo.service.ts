import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { catAreaTrabajo, catPeriodo } from '../../db/schema';

@Injectable()
export class CatalogoService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== ÁREAS DE TRABAJO =====================

  async listarAreas() {
    return this.db.select().from(catAreaTrabajo);
  }

  async listarAreasResumen() {
    return this.db
      .select({
        id: catAreaTrabajo.idArea,
        nombre: catAreaTrabajo.nombre,
      })
      .from(catAreaTrabajo)
      .where(eq(catAreaTrabajo.activo, true));
  }

  async obtenerAreaPorId(id: number) {
    const [area] = await this.db
      .select()
      .from(catAreaTrabajo)
      .where(eq(catAreaTrabajo.idArea, id))
      .limit(1);

    if (!area) {
      throw new NotFoundException(`Área con ID ${id} no encontrada`);
    }
    return area;
  }

  async crearArea(data: { nombre: string; descripcion?: string }) {
    const [nueva] = await this.db
      .insert(catAreaTrabajo)
      .values({
        nombre: data.nombre,
        descripcion: data.descripcion,
        activo: true,
      })
      .returning();
    return nueva;
  }

  async actualizarArea(id: number, data: { nombre: string; descripcion?: string }) {
    await this.obtenerAreaPorId(id);
    const [actualizada] = await this.db
      .update(catAreaTrabajo)
      .set({
        nombre: data.nombre,
        descripcion: data.descripcion,
      })
      .where(eq(catAreaTrabajo.idArea, id))
      .returning();
    return actualizada;
  }

  async eliminarArea(id: number) {
    await this.obtenerAreaPorId(id);
    const [inactivada] = await this.db
      .update(catAreaTrabajo)
      .set({ activo: false })
      .where(eq(catAreaTrabajo.idArea, id))
      .returning();
    return inactivada;
  }

  // ===================== PERIODOS =====================

  async listarPeriodos() {
    return this.db.select().from(catPeriodo);
  }

  async listarPeriodosResumen() {
    return this.db
      .select({
        id: catPeriodo.idPeriodo,
        codigo: catPeriodo.codigo,
        descripcion: catPeriodo.descripcion,
        estado: catPeriodo.estadoPeriodo,
      })
      .from(catPeriodo);
  }

  async obtenerPeriodoVigente() {
    const [vigente] = await this.db
      .select()
      .from(catPeriodo)
      .where(eq(catPeriodo.estadoPeriodo, 'VIGENTE'))
      .limit(1);

    return vigente || null;
  }

  async obtenerPeriodoPorCodigo(codigo: string) {
    const [p] = await this.db
      .select()
      .from(catPeriodo)
      .where(eq(catPeriodo.codigo, codigo))
      .limit(1);

    if (!p) {
      throw new NotFoundException(`Periodo con código '${codigo}' no encontrado`);
    }
    return p;
  }

  async obtenerPeriodoPorId(id: number) {
    const [p] = await this.db
      .select()
      .from(catPeriodo)
      .where(eq(catPeriodo.idPeriodo, id))
      .limit(1);

    if (!p) {
      throw new NotFoundException(`Periodo con ID ${id} no encontrado`);
    }
    return p;
  }

  async crearPeriodo(data: {
    codigo: string;
    descripcion: string;
    fechaInicio: string;
    fechaFin: string;
  }) {
    const [nuevo] = await this.db
      .insert(catPeriodo)
      .values({
        codigo: data.codigo,
        descripcion: data.descripcion,
        fechaInicio: data.fechaInicio,
        fechaFin: data.fechaFin,
        estadoPeriodo: 'PLANIFICADO',
      })
      .returning();
    return nuevo;
  }

  async actualizarPeriodo(
    id: number,
    data: { codigo: string; descripcion: string; fechaInicio: string; fechaFin: string }
  ) {
    await this.obtenerPeriodoPorId(id);
    const [actualizado] = await this.db
      .update(catPeriodo)
      .set({
        codigo: data.codigo,
        descripcion: data.descripcion,
        fechaInicio: data.fechaInicio,
        fechaFin: data.fechaFin,
      })
      .where(eq(catPeriodo.idPeriodo, id))
      .returning();
    return actualizado;
  }

  async marcarPeriodoVigente(id: number) {
    await this.obtenerPeriodoPorId(id);
    // Desmarcar otros vigentes
    await this.db
      .update(catPeriodo)
      .set({ estadoPeriodo: 'PLANIFICADO' })
      .where(eq(catPeriodo.estadoPeriodo, 'VIGENTE'));

    const [vigente] = await this.db
      .update(catPeriodo)
      .set({ estadoPeriodo: 'VIGENTE' })
      .where(eq(catPeriodo.idPeriodo, id))
      .returning();
    return vigente;
  }

  async cerrarPeriodo(id: number) {
    await this.obtenerPeriodoPorId(id);
    const [cerrado] = await this.db
      .update(catPeriodo)
      .set({ estadoPeriodo: 'CERRADO' })
      .where(eq(catPeriodo.idPeriodo, id))
      .returning();
    return cerrado;
  }

  async eliminarPeriodo(id: number) {
    await this.obtenerPeriodoPorId(id);
    await this.db.delete(catPeriodo).where(eq(catPeriodo.idPeriodo, id));
    return { ok: true };
  }
}
