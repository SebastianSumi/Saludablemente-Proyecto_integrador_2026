import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { equipo, persona, usuario } from '../../db/schema';
import { R2StorageService } from '../../storage/r2-storage.service';

@Injectable()
export class TeamsService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly r2Storage: R2StorageService
  ) {}

  async listarEquipos() {
    const equipos = await this.db.select().from(equipo);
    return equipos.map((eqp) => ({
      ...eqp,
      imagenUrl: eqp.imagenKey ? this.r2Storage.obtenerUrlPublica(eqp.imagenKey) : null,
    }));
  }

  async obtenerEquipoPorId(id: number) {
    const [eqp] = await this.db
      .select()
      .from(equipo)
      .where(eq(equipo.idEquipo, id))
      .limit(1);

    if (!eqp) {
      throw new NotFoundException(`Equipo con ID ${id} no encontrado`);
    }

    return {
      ...eqp,
      imagenUrl: eqp.imagenKey ? this.r2Storage.obtenerUrlPublica(eqp.imagenKey) : null,
    };
  }

  async obtenerMiEquipo(idUsuario: number) {
    // 1. Cargar usuario
    const [u] = await this.db
      .select()
      .from(usuario)
      .where(eq(usuario.idUsuario, idUsuario))
      .limit(1);

    if (!u) throw new NotFoundException('Usuario no encontrado');

    // 2. Cargar persona vinculada
    const [p] = await this.db
      .select()
      .from(persona)
      .where(eq(persona.idPersona, u.idPersonaSnapshot))
      .limit(1);

    if (!p || !p.idEquipo) {
      throw new NotFoundException('El usuario actual no pertenece a ningún equipo activo');
    }

    return this.obtenerEquipoPorId(p.idEquipo);
  }

  async obtenerMiembrosMiEquipo(idUsuario: number) {
    const eqp = await this.obtenerMiEquipo(idUsuario);

    const miembros = await this.db
      .select({
        idPersona: persona.idPersona,
        nombres: persona.nombres,
        apellidoPaterno: persona.apellidoPaterno,
        apellidoMaterno: persona.apellidoMaterno,
        celular: persona.celular,
        email: persona.email,
        avatarUrl: persona.avatarUrl,
        activo: persona.activo,
      })
      .from(persona)
      .where(eq(persona.idEquipo, eqp.idEquipo));

    return miembros.map((m) => ({
      ...m,
      esLider: eqp.idUsuarioLider !== null && eqp.idUsuarioLider !== undefined,
    }));
  }

  async puedeEditar(idEquipo: number, idUsuario: number, esAdmin: boolean) {
    if (esAdmin) return { puedeEditar: true };

    const eqp = await this.obtenerEquipoPorId(idEquipo);
    return { puedeEditar: eqp.idUsuarioLider === idUsuario };
  }

  async listarCandidatosLider(idEquipo: number) {
    const miembros = await this.db
      .select({
        idPersona: persona.idPersona,
        nombres: persona.nombres,
        apellidoPaterno: persona.apellidoPaterno,
        apellidoMaterno: persona.apellidoMaterno,
      })
      .from(persona)
      .where(eq(persona.idEquipo, idEquipo));

    const candidatos = [];
    for (const m of miembros) {
      const [u] = await this.db
        .select({
          idUsuario: usuario.idUsuario,
          email: usuario.email,
          username: usuario.username,
        })
        .from(usuario)
        .where(eq(usuario.idPersonaSnapshot, m.idPersona))
        .limit(1);

      if (u) {
        candidatos.push({
          idUsuario: u.idUsuario,
          nombreCompleto: `${m.nombres} ${m.apellidoPaterno}`,
          email: u.email,
        });
      }
    }

    return candidatos;
  }

  async asignarLider(idEquipo: number, idUsuarioLider: number | null) {
    await this.obtenerEquipoPorId(idEquipo);

    const [actualizado] = await this.db
      .update(equipo)
      .set({ idUsuarioLider })
      .where(eq(equipo.idEquipo, idEquipo))
      .returning();

    return actualizado;
  }

  async crearEquipo(data: {
    nombre: string;
    descripcion?: string;
    color?: string;
    slogan?: string;
    referenciaBiblica?: string;
    textoVersiculo?: string;
  }) {
    const [nuevo] = await this.db
      .insert(equipo)
      .values({
        nombre: data.nombre,
        descripcion: data.descripcion,
        color: data.color || '#4CAF50',
        slogan: data.slogan,
        referenciaBiblica: data.referenciaBiblica,
        textoVersiculo: data.textoVersiculo,
        activo: true,
        version: 0,
      })
      .returning();

    return nuevo;
  }

  async actualizarEquipo(id: number, data: Partial<typeof equipo.$inferInsert>) {
    await this.obtenerEquipoPorId(id);
    const [act] = await this.db
      .update(equipo)
      .set(data)
      .where(eq(equipo.idEquipo, id))
      .returning();
    return act;
  }

  async eliminarEquipo(id: number) {
    await this.obtenerEquipoPorId(id);
    const [inactivado] = await this.db
      .update(equipo)
      .set({ activo: false })
      .where(eq(equipo.idEquipo, id))
      .returning();
    return inactivado;
  }

  async subirImagenEquipo(idEquipo: number, buffer: Buffer, mime: string) {
    await this.obtenerEquipoPorId(idEquipo);
    const ext = mime.split('/')[1] || 'png';
    const key = `teams/${idEquipo}/logo.${ext}`;

    const { url } = await this.r2Storage.subirArchivo(key, buffer, mime);

    await this.db
      .update(equipo)
      .set({ imagenKey: key })
      .where(eq(equipo.idEquipo, idEquipo));

    return { imagenUrl: url };
  }
}
