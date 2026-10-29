import {
  BadRequestException,
  ConflictException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  catAreaTrabajo,
  credencialPrograma,
  equipo,
  persona,
  preferenciaComunicacion,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';
import { R2StorageService } from '../../storage/r2-storage.service';

@Injectable()
export class PersonalService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly r2Storage: R2StorageService
  ) {}

  async listarPersonas(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(persona);
    const total = Number(totalRes?.total || 0);

    const personas = await this.db
      .select({
        id: persona.idPersona,
        nombres: persona.nombres,
        apellidoPaterno: persona.apellidoPaterno,
        apellidoMaterno: persona.apellidoMaterno,
        celular: persona.celular,
        fechaNacimiento: persona.fechaNacimiento,
        sexo: persona.sexo,
        email: persona.email,
        tallaPolo: persona.tallaPolo,
        estadoCivil: persona.estadoCivil,
        nivelEducativo: persona.nivelEducativo,
        documentoIdentidad: persona.documentoIdentidad,
        avatarUrl: persona.avatarUrl,
        activo: persona.activo,
        areaTrabajo: {
          id: catAreaTrabajo.idArea,
          nombre: catAreaTrabajo.nombre,
        },
        equipo: {
          id: equipo.idEquipo,
          nombre: equipo.nombre,
        },
      })
      .from(persona)
      .leftJoin(catAreaTrabajo, eq(persona.idArea, catAreaTrabajo.idArea))
      .leftJoin(equipo, eq(persona.idEquipo, equipo.idEquipo))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(personas, total, safePage, safeSize);
  }

  async listarResumen() {
    return this.db
      .select({
        id: persona.idPersona,
        nombres: persona.nombres,
        apellidoPaterno: persona.apellidoPaterno,
        apellidoMaterno: persona.apellidoMaterno,
        documentoIdentidad: persona.documentoIdentidad,
      })
      .from(persona)
      .where(eq(persona.activo, true));
  }

  async obtenerPersonaPorId(id: number) {
    const [p] = await this.db
      .select({
        id: persona.idPersona,
        nombres: persona.nombres,
        apellidoPaterno: persona.apellidoPaterno,
        apellidoMaterno: persona.apellidoMaterno,
        celular: persona.celular,
        fechaNacimiento: persona.fechaNacimiento,
        sexo: persona.sexo,
        email: persona.email,
        tallaPolo: persona.tallaPolo,
        estadoCivil: persona.estadoCivil,
        nivelEducativo: persona.nivelEducativo,
        documentoIdentidad: persona.documentoIdentidad,
        avatarUrl: persona.avatarUrl,
        activo: persona.activo,
        idArea: persona.idArea,
        idEquipo: persona.idEquipo,
        areaTrabajo: {
          id: catAreaTrabajo.idArea,
          nombre: catAreaTrabajo.nombre,
        },
        equipo: {
          id: equipo.idEquipo,
          nombre: equipo.nombre,
        },
      })
      .from(persona)
      .leftJoin(catAreaTrabajo, eq(persona.idArea, catAreaTrabajo.idArea))
      .leftJoin(equipo, eq(persona.idEquipo, equipo.idEquipo))
      .where(eq(persona.idPersona, id))
      .limit(1);

    if (!p) {
      throw new NotFoundException(`Persona con ID ${id} no encontrada`);
    }

    return p;
  }

  async listarPorEquipo(idEquipo: number) {
    return this.db
      .select()
      .from(persona)
      .where(eq(persona.idEquipo, idEquipo));
  }

  async crearPersona(data: {
    nombres: string;
    apellidoPaterno: string;
    apellidoMaterno?: string;
    documentoIdentidad: string;
    celular?: string;
    fechaNacimiento: string;
    sexo: string;
    idArea?: number;
    tallaPolo?: string;
    estadoCivil?: string;
    nivelEducativo?: string;
    idEquipo?: number;
    email?: string;
  }) {
    const [existente] = await this.db
      .select()
      .from(persona)
      .where(eq(persona.documentoIdentidad, data.documentoIdentidad))
      .limit(1);

    if (existente) {
      throw new ConflictException(`El documento '${data.documentoIdentidad}' ya está registrado`);
    }

    const [nueva] = await this.db
      .insert(persona)
      .values({
        nombres: data.nombres,
        apellidoPaterno: data.apellidoPaterno,
        apellidoMaterno: data.apellidoMaterno,
        documentoIdentidad: data.documentoIdentidad,
        celular: data.celular,
        fechaNacimiento: data.fechaNacimiento,
        sexo: data.sexo,
        idArea: data.idArea,
        tallaPolo: data.tallaPolo,
        estadoCivil: data.estadoCivil,
        nivelEducativo: data.nivelEducativo,
        idEquipo: data.idEquipo,
        email: data.email,
        activo: true,
      })
      .returning();

    return this.obtenerPersonaPorId(nueva.idPersona);
  }

  async actualizarPersona(id: number, data: Partial<typeof persona.$inferInsert>) {
    await this.obtenerPersonaPorId(id);
    await this.db.update(persona).set(data).where(eq(persona.idPersona, id));
    return this.obtenerPersonaPorId(id);
  }

  async actualizarCelular(id: number, celular: string) {
    await this.obtenerPersonaPorId(id);
    await this.db
      .update(persona)
      .set({ celular })
      .where(eq(persona.idPersona, id));
    return { mensaje: 'Celular actualizado con éxito' };
  }

  async inactivarPersona(id: number) {
    await this.obtenerPersonaPorId(id);
    await this.db
      .update(persona)
      .set({ activo: false })
      .where(eq(persona.idPersona, id));
    return { mensaje: 'Persona inactivada' };
  }

  async reactivarPersona(id: number) {
    await this.obtenerPersonaPorId(id);
    await this.db
      .update(persona)
      .set({ activo: true })
      .where(eq(persona.idPersona, id));
    return { mensaje: 'Persona reactivada' };
  }

  async subirAvatar(id: number, buffer: Buffer, mime: string) {
    await this.obtenerPersonaPorId(id);
    const ext = mime.split('/')[1] || 'jpg';
    const key = `personal/${id}/avatar.${ext}`;

    const { url } = await this.r2Storage.subirArchivo(key, buffer, mime);

    await this.db
      .update(persona)
      .set({ avatarUrl: url })
      .where(eq(persona.idPersona, id));

    return { avatarUrl: url };
  }

  async eliminarAvatar(id: number) {
    const p = await this.obtenerPersonaPorId(id);
    if (p.avatarUrl) {
      const key = `personal/${id}/avatar`;
      await this.r2Storage.eliminarArchivo(key);
      await this.db
        .update(persona)
        .set({ avatarUrl: null })
        .where(eq(persona.idPersona, id));
    }
    return { mensaje: 'Avatar eliminado' };
  }

  // ===================== PREFERENCIAS =====================

  async obtenerPreferencias(idPersona: number) {
    const [pref] = await this.db
      .select()
      .from(preferenciaComunicacion)
      .where(eq(preferenciaComunicacion.idPersona, idPersona))
      .limit(1);

    return pref || { canalPreferido: 'EMAIL', recibirNotificaciones: true };
  }

  async actualizarPreferencias(
    idPersona: number,
    data: { canalPreferido: string; recibirNotificaciones: boolean }
  ) {
    const [pref] = await this.db
      .select()
      .from(preferenciaComunicacion)
      .where(eq(preferenciaComunicacion.idPersona, idPersona))
      .limit(1);

    if (pref) {
      const [act] = await this.db
        .update(preferenciaComunicacion)
        .set(data)
        .where(eq(preferenciaComunicacion.idPersona, idPersona))
        .returning();
      return act;
    } else {
      const [nuevo] = await this.db
        .insert(preferenciaComunicacion)
        .values({
          idPersona,
          ...data,
        })
        .returning();
      return nuevo;
    }
  }

  // ===================== CREDENCIAL =====================

  async generarCredencial(idPersona: number) {
    const p = await this.obtenerPersonaPorId(idPersona);
    const codigoQr = `SLB-${p.documentoIdentidad}-${Date.now()}`;

    const [credencial] = await this.db
      .insert(credencialPrograma)
      .values({
        idPersona,
        codigoQr,
        fechaEmision: new Date(),
        activo: true,
      })
      .returning();

    return credencial;
  }

  async verificarCredencial(codigoQr: string) {
    const [c] = await this.db
      .select()
      .from(credencialPrograma)
      .where(eq(credencialPrograma.codigoQr, codigoQr))
      .limit(1);

    if (!c || !c.activo) {
      throw new NotFoundException('Credencial no encontrada o inactiva');
    }

    const personaData = await this.obtenerPersonaPorId(c.idPersona);
    return {
      valido: true,
      persona: personaData,
      fechaEmision: c.fechaEmision,
    };
  }
}
