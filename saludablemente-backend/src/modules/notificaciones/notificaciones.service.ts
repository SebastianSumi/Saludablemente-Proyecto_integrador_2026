import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { notificacion, plantillaMensaje } from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class NotificacionesService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== NOTIFICACIONES DE USUARIO =====================

  async listarMisNotificaciones(idUsuario: number, page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(notificacion)
      .where(eq(notificacion.idUsuario, idUsuario));
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select()
      .from(notificacion)
      .where(eq(notificacion.idUsuario, idUsuario))
      .orderBy(sql`${notificacion.fechaCreacion} desc`)
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async contarNoLeidas(idUsuario: number) {
    const [res] = await this.db
      .select({ total: count() })
      .from(notificacion)
      .where(sql`${notificacion.idUsuario} = ${idUsuario} and ${notificacion.leida} = false`);
    return { noLeidas: Number(res?.total || 0) };
  }

  async marcarLeida(idNotificacion: number, idUsuario: number) {
    const [act] = await this.db
      .update(notificacion)
      .set({
        leida: true,
        fechaLeida: new Date(),
      })
      .where(
        sql`${notificacion.idNotificacion} = ${idNotificacion} and ${notificacion.idUsuario} = ${idUsuario}`
      )
      .returning();

    if (!act) throw new NotFoundException('Notificación no encontrada');
    return act;
  }

  async eliminarNotificacion(idNotificacion: number, idUsuario: number) {
    await this.db
      .delete(notificacion)
      .where(
        sql`${notificacion.idNotificacion} = ${idNotificacion} and ${notificacion.idUsuario} = ${idUsuario}`
      );
    return { mensaje: 'Notificación eliminada' };
  }

  async crearNotificacion(idUsuario: number, titulo: string, mensaje: string, canal: string = 'IN_APP') {
    const [nueva] = await this.db
      .insert(notificacion)
      .values({
        idUsuario,
        titulo,
        mensaje,
        canal,
        leida: false,
        fechaCreacion: new Date(),
      })
      .returning();
    return nueva;
  }

  // ===================== PLANTILLAS =====================

  async listarPlantillas() {
    return this.db.select().from(plantillaMensaje);
  }

  async obtenerPlantillaPorId(id: number) {
    const [p] = await this.db
      .select()
      .from(plantillaMensaje)
      .where(eq(plantillaMensaje.idPlantilla, id))
      .limit(1);

    if (!p) throw new NotFoundException('Plantilla no encontrada');
    return p;
  }

  async crearPlantilla(data: typeof plantillaMensaje.$inferInsert) {
    const [nueva] = await this.db.insert(plantillaMensaje).values(data).returning();
    return nueva;
  }

  async actualizarPlantilla(id: number, data: Partial<typeof plantillaMensaje.$inferInsert>) {
    await this.obtenerPlantillaPorId(id);
    const [act] = await this.db
      .update(plantillaMensaje)
      .set(data)
      .where(eq(plantillaMensaje.idPlantilla, id))
      .returning();
    return act;
  }

  async eliminarPlantilla(id: number) {
    await this.obtenerPlantillaPorId(id);
    await this.db.delete(plantillaMensaje).where(eq(plantillaMensaje.idPlantilla, id));
    return { mensaje: 'Plantilla eliminada' };
  }
}
