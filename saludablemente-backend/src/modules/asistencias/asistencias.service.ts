import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  asistencia,
  asistenciaCuarentena,
  justificacion,
  qrActividad,
  persona,
  actividad,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class AsistenciasService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== MARCACIÓN Y REGISTRO =====================

  async registrarMarcacion(data: {
    idActividad: number;
    idPersona: number;
    tipoAsistencia?: string;
    idLocal?: string;
    observacion?: string;
  }) {
    // Verificar si ya existe marcación para esta persona en esta actividad
    const [existente] = await this.db
      .select()
      .from(asistencia)
      .where(
        sql`${asistencia.idActividad} = ${data.idActividad} and ${asistencia.idPersona} = ${data.idPersona}`
      )
      .limit(1);

    if (existente) {
      return existente;
    }

    const [nueva] = await this.db
      .insert(asistencia)
      .values({
        idActividad: data.idActividad,
        idPersona: data.idPersona,
        tipoAsistencia: data.tipoAsistencia || 'PRESENCIAL',
        idLocal: data.idLocal,
        observacion: data.observacion,
        fechaHora: new Date(),
      })
      .returning();

    return nueva;
  }

  async registrarMarcacionLote(
    items: Array<{
      idActividad: number;
      idPersona: number;
      tipoAsistencia?: string;
      idLocal?: string;
    }>
  ) {
    const resultados = [];
    for (const item of items) {
      const res = await this.registrarMarcacion(item);
      resultados.push(res);
    }
    return { procesados: resultados.length, asistencias: resultados };
  }

  async listarPorActividad(idActividad: number) {
    return this.db
      .select({
        idAsistencia: asistencia.idAsistencia,
        idPersona: asistencia.idPersona,
        tipoAsistencia: asistencia.tipoAsistencia,
        fechaHora: asistencia.fechaHora,
        observacion: asistencia.observacion,
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(asistencia)
      .innerJoin(persona, eq(asistencia.idPersona, persona.idPersona))
      .where(eq(asistencia.idActividad, idActividad));
  }

  async historialPersona(idPersona: number) {
    return this.db
      .select({
        idAsistencia: asistencia.idAsistencia,
        fechaHora: asistencia.fechaHora,
        tipoAsistencia: asistencia.tipoAsistencia,
        actividad: {
          idActividad: actividad.idActividad,
          titulo: actividad.titulo,
          fecha: actividad.fecha,
        },
      })
      .from(asistencia)
      .innerJoin(actividad, eq(asistencia.idActividad, actividad.idActividad))
      .where(eq(asistencia.idPersona, idPersona));
  }

  // ===================== QR ACTIVIDAD =====================

  async obtenerQrActual(idActividad: number) {
    const [qr] = await this.db
      .select()
      .from(qrActividad)
      .where(eq(qrActividad.idActividad, idActividad))
      .orderBy(sql`${qrActividad.version} desc`)
      .limit(1);

    if (!qr) {
      return this.rotarQr(idActividad);
    }
    return qr;
  }

  async rotarQr(idActividad: number) {
    const [ultimo] = await this.db
      .select()
      .from(qrActividad)
      .where(eq(qrActividad.idActividad, idActividad))
      .orderBy(sql`${qrActividad.version} desc`)
      .limit(1);

    const proximaVersion = (ultimo?.version || 0) + 1;
    const ahora = new Date();
    const expira = new Date(ahora.getTime() + 180 * 1000); // 3 minutos de vigencia
    const codigoQr = `ACT-${idActividad}-V${proximaVersion}-${Date.now()}`;

    const [nuevo] = await this.db
      .insert(qrActividad)
      .values({
        idActividad,
        codigoQr,
        version: proximaVersion,
        fechaGeneracion: ahora,
        fechaExpiracion: expira,
      })
      .returning();

    return nuevo;
  }

  // ===================== JUSTIFICACIONES / EXONERACIONES =====================

  async solicitarJustificacion(data: {
    idActividad: number;
    idPersona: number;
    motivo: string;
    archivoAdjunto?: string;
  }) {
    const [just] = await this.db
      .insert(justificacion)
      .values({
        idActividad: data.idActividad,
        idPersona: data.idPersona,
        motivo: data.motivo,
        archivoAdjunto: data.archivoAdjunto,
        estado: 'PENDIENTE',
        fechaSolicitud: new Date(),
      })
      .returning();
    return just;
  }

  async dictaminarJustificacion(id: number, estado: 'APROBADA' | 'RECHAZADA') {
    const [just] = await this.db
      .update(justificacion)
      .set({
        estado,
        fechaResolucion: new Date(),
      })
      .where(eq(justificacion.idJustificacion, id))
      .returning();

    if (!just) throw new NotFoundException('Justificación no encontrada');

    if (estado === 'APROBADA') {
      await this.registrarMarcacion({
        idActividad: just.idActividad,
        idPersona: just.idPersona,
        tipoAsistencia: 'JUSTIFICADA',
      });
    }

    return just;
  }

  async listarJustificaciones(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(justificacion);
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idJustificacion: justificacion.idJustificacion,
        idActividad: justificacion.idActividad,
        idPersona: justificacion.idPersona,
        motivo: justificacion.motivo,
        archivoAdjunto: justificacion.archivoAdjunto,
        estado: justificacion.estado,
        fechaSolicitud: justificacion.fechaSolicitud,
        fechaResolucion: justificacion.fechaResolucion,
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(justificacion)
      .innerJoin(persona, eq(justificacion.idPersona, persona.idPersona))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  // ===================== CUARENTENA =====================

  async listarCuarentena(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(asistenciaCuarentena);
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select()
      .from(asistenciaCuarentena)
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async contarCuarentenaPendiente() {
    const [res] = await this.db
      .select({ total: count() })
      .from(asistenciaCuarentena);
    return { count: Number(res?.total || 0) };
  }
}
