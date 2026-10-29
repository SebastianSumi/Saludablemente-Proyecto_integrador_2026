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
  detalleAntropometrico,
  detalleBioquimico,
  evaluacionNutricional,
  inscripcionJornada,
  jornadaEvaluacion,
  persona,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class NutricionalService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== JORNADAS =====================

  async listarJornadas() {
    return this.db.select().from(jornadaEvaluacion);
  }

  async obtenerJornadaPorId(id: number) {
    const [j] = await this.db
      .select()
      .from(jornadaEvaluacion)
      .where(eq(jornadaEvaluacion.idJornada, id))
      .limit(1);

    if (!j) throw new NotFoundException(`Jornada con ID ${id} no encontrada`);
    return j;
  }

  async crearJornada(data: typeof jornadaEvaluacion.$inferInsert) {
    const [nueva] = await this.db.insert(jornadaEvaluacion).values(data).returning();
    return nueva;
  }

  async actualizarJornada(id: number, data: Partial<typeof jornadaEvaluacion.$inferInsert>) {
    await this.obtenerJornadaPorId(id);
    const [act] = await this.db
      .update(jornadaEvaluacion)
      .set(data)
      .where(eq(jornadaEvaluacion.idJornada, id))
      .returning();
    return act;
  }

  async cambiarEstadoJornada(id: number, estado: string) {
    return this.actualizarJornada(id, { estado });
  }

  async listarInscripcionesJornada(idJornada: number) {
    return this.db
      .select({
        idInscripcion: inscripcionJornada.idInscripcion,
        idPersona: inscripcionJornada.idPersona,
        numeroTurno: inscripcionJornada.numeroTurno,
        estado: inscripcionJornada.estado,
        fechaInscripcion: inscripcionJornada.fechaInscripcion,
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(inscripcionJornada)
      .innerJoin(persona, eq(inscripcionJornada.idPersona, persona.idPersona))
      .where(eq(inscripcionJornada.idJornada, idJornada));
  }

  async inscribirJornada(idJornada: number, idPersona: number) {
    await this.obtenerJornadaPorId(idJornada);
    const inscripciones = await this.listarInscripcionesJornada(idJornada);
    const proximoTurno = inscripciones.length + 1;

    const [inscrita] = await this.db
      .insert(inscripcionJornada)
      .values({
        idJornada,
        idPersona,
        numeroTurno: proximoTurno,
        estado: 'PENDIENTE',
        fechaInscripcion: new Date(),
      })
      .returning();

    return inscrita;
  }

  // ===================== EVALUACIONES NUTRICIONALES =====================

  async listarEvaluaciones(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(evaluacionNutricional);
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idEvaluacion: evaluacionNutricional.idEvaluacion,
        idPersona: evaluacionNutricional.idPersona,
        fechaEvaluacion: evaluacionNutricional.fechaEvaluacion,
        edadAlMomento: evaluacionNutricional.edadAlMomento,
        observaciones: evaluacionNutricional.observaciones,
        activo: evaluacionNutricional.activo,
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
      .from(evaluacionNutricional)
      .innerJoin(catPeriodo, eq(evaluacionNutricional.idPeriodo, catPeriodo.idPeriodo))
      .innerJoin(persona, eq(evaluacionNutricional.idPersona, persona.idPersona))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async obtenerEvaluacionPorId(id: number) {
    const [evaluacion] = await this.db
      .select()
      .from(evaluacionNutricional)
      .where(eq(evaluacionNutricional.idEvaluacion, id))
      .limit(1);

    if (!evaluacion) {
      throw new NotFoundException(`Evaluación nutricional con ID ${id} no encontrada`);
    }

    const [antropo] = await this.db
      .select()
      .from(detalleAntropometrico)
      .where(eq(detalleAntropometrico.idEvaluacion, id))
      .limit(1);

    const [bioquim] = await this.db
      .select()
      .from(detalleBioquimico)
      .where(eq(detalleBioquimico.idEvaluacion, id))
      .limit(1);

    return {
      ...evaluacion,
      antropometria: antropo || null,
      bioquimica: bioquim || null,
    };
  }

  async crearEvaluacion(data: typeof evaluacionNutricional.$inferInsert) {
    const [nueva] = await this.db.insert(evaluacionNutricional).values(data).returning();
    return nueva;
  }

  async actualizarObservaciones(id: number, observaciones: string) {
    await this.obtenerEvaluacionPorId(id);
    const [act] = await this.db
      .update(evaluacionNutricional)
      .set({ observaciones })
      .where(eq(evaluacionNutricional.idEvaluacion, id))
      .returning();
    return act;
  }

  async guardarAntropometria(idEvaluacion: number, data: any) {
    await this.obtenerEvaluacionPorId(idEvaluacion);
    const [existente] = await this.db
      .select()
      .from(detalleAntropometrico)
      .where(eq(detalleAntropometrico.idEvaluacion, idEvaluacion))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(detalleAntropometrico)
        .set(data)
        .where(eq(detalleAntropometrico.idEvaluacion, idEvaluacion))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(detalleAntropometrico)
        .values({
          idEvaluacion,
          ...data,
        })
        .returning();
      return nuevo;
    }
  }

  async guardarBioquimica(idEvaluacion: number, data: any) {
    await this.obtenerEvaluacionPorId(idEvaluacion);
    const [existente] = await this.db
      .select()
      .from(detalleBioquimico)
      .where(eq(detalleBioquimico.idEvaluacion, idEvaluacion))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(detalleBioquimico)
        .set(data)
        .where(eq(detalleBioquimico.idEvaluacion, idEvaluacion))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(detalleBioquimico)
        .values({
          idEvaluacion,
          ...data,
        })
        .returning();
      return nuevo;
    }
  }
}
