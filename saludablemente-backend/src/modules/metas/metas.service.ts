import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { checkIn, meta } from '../../db/schema';

@Injectable()
export class MetasService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== METAS =====================

  async crearMeta(data: typeof meta.$inferInsert) {
    const [nueva] = await this.db.insert(meta).values(data).returning();
    return nueva;
  }

  async listarPorPersona(idPersona: number) {
    return this.db.select().from(meta).where(eq(meta.idPersona, idPersona));
  }

  async obtenerProgreso(idMeta: number) {
    const [m] = await this.db
      .select()
      .from(meta)
      .where(eq(meta.idMeta, idMeta))
      .limit(1);

    if (!m) throw new NotFoundException('Meta no encontrada');

    const checkins = await this.listarCheckIns(idMeta);
    return {
      meta: m,
      totalCheckIns: checkins.length,
      checkIns: checkins,
    };
  }

  // ===================== CHECK-INS =====================

  async registrarCheckIn(data: typeof checkIn.$inferInsert) {
    const [nuevo] = await this.db.insert(checkIn).values(data).returning();
    return nuevo;
  }

  async listarCheckIns(idMeta: number) {
    return this.db
      .select()
      .from(checkIn)
      .where(eq(checkIn.idMeta, idMeta));
  }
}
