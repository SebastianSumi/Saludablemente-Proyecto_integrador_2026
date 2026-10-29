import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  alertaClinica,
  alertaClinicaDetalle,
  alertaEtiqueta,
  etiquetaClinica,
  parametroEvaluacion,
  persona,
  usoCardAlerta,
  usuario,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class AlertasService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== ALERTAS CLÍNICAS =====================

  async listarAlertas(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(alertaClinica);
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idAlerta: alertaClinica.idAlerta,
        idPersona: alertaClinica.idPersona,
        nivelSeveridad: alertaClinica.nivelSeveridad,
        estado: alertaClinica.estado,
        fechaGeneracion: alertaClinica.fechaGeneracion,
        fechaAtencion: alertaClinica.fechaAtencion,
        observacion: alertaClinica.observacion,
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(alertaClinica)
      .innerJoin(persona, eq(alertaClinica.idPersona, persona.idPersona))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async obtenerAlertaPorId(id: number) {
    const [alerta] = await this.db
      .select()
      .from(alertaClinica)
      .where(eq(alertaClinica.idAlerta, id))
      .limit(1);

    if (!alerta) {
      throw new NotFoundException(`Alerta con ID ${id} no encontrada`);
    }

    const detalles = await this.db
      .select()
      .from(alertaClinicaDetalle)
      .where(eq(alertaClinicaDetalle.idAlerta, id));

    const etiquetas = await this.db
      .select({
        idEtiqueta: etiquetaClinica.idEtiqueta,
        nombre: etiquetaClinica.nombre,
        color: etiquetaClinica.color,
      })
      .from(alertaEtiqueta)
      .innerJoin(etiquetaClinica, eq(alertaEtiqueta.idEtiqueta, etiquetaClinica.idEtiqueta))
      .where(eq(alertaEtiqueta.idAlerta, id));

    return {
      ...alerta,
      detalles,
      etiquetas,
    };
  }

  async atenderAlerta(id: number, observacion?: string) {
    await this.obtenerAlertaPorId(id);
    const [act] = await this.db
      .update(alertaClinica)
      .set({
        estado: 'ATENDIDA',
        fechaAtencion: new Date(),
        observacion,
      })
      .where(eq(alertaClinica.idAlerta, id))
      .returning();
    return act;
  }

  async anularAlerta(id: number, observacion?: string) {
    await this.obtenerAlertaPorId(id);
    const [act] = await this.db
      .update(alertaClinica)
      .set({
        estado: 'ANULADA',
        observacion,
      })
      .where(eq(alertaClinica.idAlerta, id))
      .returning();
    return act;
  }

  async listarMisAlertas(idUsuario: number) {
    const [u] = await this.db
      .select()
      .from(usuario)
      .where(eq(usuario.idUsuario, idUsuario))
      .limit(1);

    if (!u) return [];

    return this.db
      .select()
      .from(alertaClinica)
      .where(eq(alertaClinica.idPersona, u.idPersonaSnapshot));
  }

  // ===================== ETIQUETAS CLÍNICAS =====================

  async listarEtiquetas() {
    return this.db.select().from(etiquetaClinica);
  }

  async obtenerEtiquetaPorId(id: number) {
    const [et] = await this.db
      .select()
      .from(etiquetaClinica)
      .where(eq(etiquetaClinica.idEtiqueta, id))
      .limit(1);

    if (!et) throw new NotFoundException('Etiqueta no encontrada');
    return et;
  }

  async crearEtiqueta(data: typeof etiquetaClinica.$inferInsert) {
    const [nueva] = await this.db.insert(etiquetaClinica).values(data).returning();
    return nueva;
  }

  async actualizarEtiqueta(id: number, data: Partial<typeof etiquetaClinica.$inferInsert>) {
    await this.obtenerEtiquetaPorId(id);
    const [act] = await this.db
      .update(etiquetaClinica)
      .set(data)
      .where(eq(etiquetaClinica.idEtiqueta, id))
      .returning();
    return act;
  }

  async eliminarEtiqueta(id: number) {
    await this.obtenerEtiquetaPorId(id);
    await this.db.delete(etiquetaClinica).where(eq(etiquetaClinica.idEtiqueta, id));
    return { mensaje: 'Etiqueta eliminada' };
  }

  // ===================== PARÁMETROS =====================

  async listarParametros() {
    return this.db.select().from(parametroEvaluacion);
  }

  // ===================== USO DE CARDS =====================

  async listarUsoCards(idUsuario: number) {
    return this.db
      .select()
      .from(usoCardAlerta)
      .where(eq(usoCardAlerta.idUsuario, idUsuario));
  }

  async registrarUsoCard(idUsuario: number, ruta: string) {
    const [existente] = await this.db
      .select()
      .from(usoCardAlerta)
      .where(sql`${usoCardAlerta.idUsuario} = ${idUsuario} and ${usoCardAlerta.ruta} = ${ruta}`)
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(usoCardAlerta)
        .set({
          contador: existente.contador + 1,
          fechaUltimoUso: new Date(),
        })
        .where(eq(usoCardAlerta.idUso, existente.idUso))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(usoCardAlerta)
        .values({
          idUsuario,
          ruta,
          contador: 1,
          fechaUltimoUso: new Date(),
        })
        .returning();
      return nuevo;
    }
  }
}
