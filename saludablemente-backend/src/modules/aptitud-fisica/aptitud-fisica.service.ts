import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  catPeriodo,
  detalleCardiovascular,
  detalleFuerzaResistencia,
  evaluacionAptitud,
  persona,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class AptitudFisicaService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  async listarEvaluaciones(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(evaluacionAptitud);
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idEvaluacion: evaluacionAptitud.idEvaluacion,
        idPersona: evaluacionAptitud.idPersona,
        fechaEvaluacion: evaluacionAptitud.fechaEvaluacion,
        edadAlMomento: evaluacionAptitud.edadAlMomento,
        observaciones: evaluacionAptitud.observaciones,
        activo: evaluacionAptitud.activo,
        periodo: {
          id: catPeriodo.idPeriodo,
          codigo: catPeriodo.codigo,
        },
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(evaluacionAptitud)
      .innerJoin(catPeriodo, eq(evaluacionAptitud.idPeriodo, catPeriodo.idPeriodo))
      .innerJoin(persona, eq(evaluacionAptitud.idPersona, persona.idPersona))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async obtenerEvaluacionPorId(id: number) {
    const [evaluacion] = await this.db
      .select()
      .from(evaluacionAptitud)
      .where(eq(evaluacionAptitud.idEvaluacion, id))
      .limit(1);

    if (!evaluacion) {
      throw new NotFoundException(`Evaluación de aptitud con ID ${id} no encontrada`);
    }

    const [cardio] = await this.db
      .select()
      .from(detalleCardiovascular)
      .where(eq(detalleCardiovascular.idEvaluacion, id))
      .limit(1);

    const [fuerza] = await this.db
      .select()
      .from(detalleFuerzaResistencia)
      .where(eq(detalleFuerzaResistencia.idEvaluacion, id))
      .limit(1);

    return {
      ...evaluacion,
      cardio: cardio || null,
      fuerza: fuerza || null,
    };
  }

  async crearEvaluacion(data: typeof evaluacionAptitud.$inferInsert) {
    const [nueva] = await this.db.insert(evaluacionAptitud).values(data).returning();
    return nueva;
  }

  async actualizarObservaciones(id: number, observaciones: string) {
    await this.obtenerEvaluacionPorId(id);
    const [act] = await this.db
      .update(evaluacionAptitud)
      .set({ observaciones })
      .where(eq(evaluacionAptitud.idEvaluacion, id))
      .returning();
    return act;
  }

  async guardarCardio(idEvaluacion: number, data: any) {
    await this.obtenerEvaluacionPorId(idEvaluacion);
    const [existente] = await this.db
      .select()
      .from(detalleCardiovascular)
      .where(eq(detalleCardiovascular.idEvaluacion, idEvaluacion))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(detalleCardiovascular)
        .set(data)
        .where(eq(detalleCardiovascular.idEvaluacion, idEvaluacion))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(detalleCardiovascular)
        .values({
          idEvaluacion,
          ...data,
        })
        .returning();
      return nuevo;
    }
  }

  async guardarFuerza(idEvaluacion: number, data: any) {
    await this.obtenerEvaluacionPorId(idEvaluacion);
    const [existente] = await this.db
      .select()
      .from(detalleFuerzaResistencia)
      .where(eq(detalleFuerzaResistencia.idEvaluacion, idEvaluacion))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(detalleFuerzaResistencia)
        .set(data)
        .where(eq(detalleFuerzaResistencia.idEvaluacion, idEvaluacion))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(detalleFuerzaResistencia)
        .values({
          idEvaluacion,
          ...data,
        })
        .returning();
      return nuevo;
    }
  }
}
