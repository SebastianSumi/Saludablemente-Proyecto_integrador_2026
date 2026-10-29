import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { persona, recomendacionIa, recomendacionPendiente } from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class RecomendacionesIaService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  async listarPorPersona(idPersona: number, page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(recomendacionIa)
      .where(eq(recomendacionIa.idPersona, idPersona));
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select()
      .from(recomendacionIa)
      .where(eq(recomendacionIa.idPersona, idPersona))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async obtenerPorId(id: number) {
    const [rec] = await this.db
      .select()
      .from(recomendacionIa)
      .where(eq(recomendacionIa.idRecomendacion, id))
      .limit(1);

    if (!rec) throw new NotFoundException('Recomendación no encontrada');
    return rec;
  }

  async encolarGeneracion(idPersona: number, prompt: string, datosEntrada: any) {
    const [pendiente] = await this.db
      .insert(recomendacionPendiente)
      .values({
        idPersona,
        prompt,
        datosEntradaJson: JSON.stringify(datosEntrada),
        estado: 'PENDIENTE',
        intentos: 0,
        fechaCreacion: new Date(),
      })
      .returning();

    return pendiente;
  }
}
