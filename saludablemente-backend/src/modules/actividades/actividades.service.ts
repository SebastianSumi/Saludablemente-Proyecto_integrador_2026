import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  actividad,
  actividadImagen,
  actividadInscrito,
  actividadMaterial,
  persona,
} from '../../db/schema';
import { R2StorageService } from '../../storage/r2-storage.service';

@Injectable()
export class ActividadesService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly r2Storage: R2StorageService
  ) {}

  // ===================== ACTIVIDADES Y AGENDA =====================

  async listarActividades(page: number = 0, size: number = 20) {
    return this.db.select().from(actividad);
  }

  async obtenerActividadPorId(id: number) {
    const [act] = await this.db
      .select()
      .from(actividad)
      .where(eq(actividad.idActividad, id))
      .limit(1);

    if (!act) {
      throw new NotFoundException(`Actividad con ID ${id} no encontrada`);
    }
    return act;
  }

  async crearActividad(data: typeof actividad.$inferInsert) {
    const [nueva] = await this.db
      .insert(actividad)
      .values({
        ...data,
        estado: 'PROGRAMADA',
        activo: true,
      })
      .returning();
    return nueva;
  }

  async actualizarSede(id: number, lugar: string) {
    await this.obtenerActividadPorId(id);
    const [act] = await this.db
      .update(actividad)
      .set({ lugar })
      .where(eq(actividad.idActividad, id))
      .returning();
    return act;
  }

  async obtenerCalendario() {
    return this.db.select().from(actividad).where(eq(actividad.activo, true));
  }

  async obtenerActividadesHoy() {
    const hoyStr = new Date().toISOString().split('T')[0];
    return this.db
      .select()
      .from(actividad)
      .where(sql`${actividad.fecha} = ${hoyStr} and ${actividad.activo} = true`);
  }

  // ===================== OPERACIÓN =====================

  async cambiarEstado(id: number, nuevoEstado: string) {
    await this.obtenerActividadPorId(id);
    const [act] = await this.db
      .update(actividad)
      .set({ estado: nuevoEstado })
      .where(eq(actividad.idActividad, id))
      .returning();
    return act;
  }

  async asignarCoordinador(id: number, idCoordinador: number) {
    await this.obtenerActividadPorId(id);
    const [act] = await this.db
      .update(actividad)
      .set({ idCoordinador })
      .where(eq(actividad.idActividad, id))
      .returning();
    return act;
  }

  // ===================== INSCRIPCIONES =====================

  async listarInscripciones(idActividad: number) {
    return this.db
      .select({
        idInscrito: actividadInscrito.idInscrito,
        idPersona: actividadInscrito.idPersona,
        asistio: actividadInscrito.asistio,
        fechaInscripcion: actividadInscrito.fechaInscripcion,
        persona: {
          nombres: persona.nombres,
          apellidoPaterno: persona.apellidoPaterno,
          documentoIdentidad: persona.documentoIdentidad,
        },
      })
      .from(actividadInscrito)
      .innerJoin(persona, eq(actividadInscrito.idPersona, persona.idPersona))
      .where(eq(actividadInscrito.idActividad, idActividad));
  }

  async contarInscripciones(idActividad: number) {
    const [res] = await this.db
      .select({ total: count() })
      .from(actividadInscrito)
      .where(eq(actividadInscrito.idActividad, idActividad));
    return { count: Number(res?.total || 0) };
  }

  async inscribirPersona(idActividad: number, idPersona: number) {
    await this.obtenerActividadPorId(idActividad);

    const [inscrito] = await this.db
      .insert(actividadInscrito)
      .values({
        idActividad,
        idPersona,
        fechaInscripcion: new Date(),
        asistio: false,
      })
      .returning();

    return inscrito;
  }

  async desinscribirPersona(idActividad: number, idPersona: number) {
    await this.db
      .delete(actividadInscrito)
      .where(
        sql`${actividadInscrito.idActividad} = ${idActividad} and ${actividadInscrito.idPersona} = ${idPersona}`
      );
    return { mensaje: 'Inscripción cancelada' };
  }

  // ===================== MATERIALES =====================

  async listarMateriales(idActividad: number) {
    return this.db
      .select()
      .from(actividadMaterial)
      .where(eq(actividadMaterial.idActividad, idActividad));
  }

  async agregarMaterial(idActividad: number, nombre: string, cantidad: number) {
    await this.obtenerActividadPorId(idActividad);
    const [mat] = await this.db
      .insert(actividadMaterial)
      .values({
        idActividad,
        nombre,
        cantidad,
      })
      .returning();
    return mat;
  }

  // ===================== IMÁGENES =====================

  async listarImagenes(idActividad: number) {
    return this.db
      .select()
      .from(actividadImagen)
      .where(eq(actividadImagen.idActividad, idActividad));
  }

  async subirImagen(idActividad: number, buffer: Buffer, mime: string) {
    await this.obtenerActividadPorId(idActividad);
    const ext = mime.split('/')[1] || 'jpg';
    const key = `actividades/${idActividad}/imagenes/${Date.now()}.${ext}`;

    const { url } = await this.r2Storage.subirArchivo(key, buffer, mime);

    const [img] = await this.db
      .insert(actividadImagen)
      .values({
        idActividad,
        imagenUrl: url,
        orden: 1,
      })
      .returning();

    return img;
  }

  async eliminarImagen(idImagen: number) {
    await this.db.delete(actividadImagen).where(eq(actividadImagen.idImagen, idImagen));
    return { mensaje: 'Imagen eliminada' };
  }
}
