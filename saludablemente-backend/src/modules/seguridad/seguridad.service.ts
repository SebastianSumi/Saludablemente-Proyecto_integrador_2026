import {
  BadRequestException,
  ConflictException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { count, eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { persona, rol, usuario, usuarioRol } from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';

@Injectable()
export class SeguridadService {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  // ===================== ROLES =====================

  async listarRoles() {
    return this.db.select().from(rol);
  }

  // ===================== USUARIOS =====================

  async listarUsuarios(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(usuario);
    const total = Number(totalRes?.total || 0);

    const usuarios = await this.db
      .select()
      .from(usuario)
      .limit(safeSize)
      .offset(offset);

    // Obtener roles para cada usuario
    const usuariosConRoles = await Promise.all(
      usuarios.map(async (u) => {
        const rolesUser = await this.db
          .select({ id: rol.idRol, nombre: rol.nombre })
          .from(usuarioRol)
          .innerJoin(rol, eq(usuarioRol.idRol, rol.idRol))
          .where(eq(usuarioRol.idUsuario, u.idUsuario));

        return {
          id: u.idUsuario,
          email: u.email,
          username: u.username,
          activo: u.activo,
          fechaCreacion: u.fechaCreacion,
          nombres: u.nombresSnapshot,
          apellidoPaterno: u.apellidoPaternoSnapshot,
          apellidoMaterno: u.apellidoMaternoSnapshot,
          areaTrabajo: u.areaTrabajoSnapshot,
          equipo: u.equipoSnapshot,
          roles: rolesUser.map((r) => r.nombre),
          idPersona: u.idPersonaSnapshot,
        };
      })
    );

    return buildSpringPage(usuariosConRoles, total, safePage, safeSize);
  }

  async obtenerUsuarioPorId(id: number) {
    const [u] = await this.db
      .select()
      .from(usuario)
      .where(eq(usuario.idUsuario, id))
      .limit(1);

    if (!u) {
      throw new NotFoundException(`Usuario con ID ${id} no encontrado`);
    }

    const rolesUser = await this.db
      .select({ id: rol.idRol, nombre: rol.nombre })
      .from(usuarioRol)
      .innerJoin(rol, eq(usuarioRol.idRol, rol.idRol))
      .where(eq(usuarioRol.idUsuario, u.idUsuario));

    return {
      id: u.idUsuario,
      email: u.email,
      username: u.username,
      activo: u.activo,
      fechaCreacion: u.fechaCreacion,
      nombres: u.nombresSnapshot,
      apellidoPaterno: u.apellidoPaternoSnapshot,
      apellidoMaterno: u.apellidoMaternoSnapshot,
      areaTrabajo: u.areaTrabajoSnapshot,
      equipo: u.equipoSnapshot,
      roles: rolesUser.map((r) => r.nombre),
      idPersona: u.idPersonaSnapshot,
    };
  }

  async crearUsuario(data: {
    email: string;
    username?: string;
    password: string;
    idPersona: number;
    rolesIds: number[];
  }) {
    // Verificar si ya existe usuario con ese email
    const [existente] = await this.db
      .select()
      .from(usuario)
      .where(eq(usuario.email, data.email))
      .limit(1);

    if (existente) {
      throw new ConflictException(`El correo '${data.email}' ya está registrado`);
    }

    // Cargar persona para snapshot de identidad anticorrupción
    const [p] = await this.db
      .select()
      .from(persona)
      .where(eq(persona.idPersona, data.idPersona))
      .limit(1);

    if (!p) {
      throw new BadRequestException(`Persona con ID ${data.idPersona} no encontrada`);
    }

    const passwordHash = await bcrypt.hash(data.password, 10);
    const username = data.username || data.email.split('@')[0];
    const now = new Date();

    const [nuevo] = await this.db
      .insert(usuario)
      .values({
        email: data.email,
        username,
        passwordHash,
        activo: true,
        fechaCreacion: now,
        idPersonaSnapshot: p.idPersona,
        documentoIdentidadSnapshot: p.documentoIdentidad,
        nombresSnapshot: p.nombres,
        apellidoPaternoSnapshot: p.apellidoPaterno,
        apellidoMaternoSnapshot: p.apellidoMaterno || null,
        areaTrabajoSnapshot: 'General',
        equipoSnapshot: null,
        activoPersonaSnapshot: p.activo,
        snapshotFecha: now,
      })
      .returning();

    // Asociar roles
    if (data.rolesIds && data.rolesIds.length > 0) {
      for (const idRol of data.rolesIds) {
        await this.db.insert(usuarioRol).values({
          idUsuario: nuevo.idUsuario,
          idRol,
        });
      }
    }

    return this.obtenerUsuarioPorId(nuevo.idUsuario);
  }

  async actualizarRoles(idUsuario: number, rolesIds: number[]) {
    await this.obtenerUsuarioPorId(idUsuario);

    // Limpiar roles actuales
    await this.db.delete(usuarioRol).where(eq(usuarioRol.idUsuario, idUsuario));

    // Asignar nuevos
    for (const idRol of rolesIds) {
      await this.db.insert(usuarioRol).values({ idUsuario, idRol });
    }

    return this.obtenerUsuarioPorId(idUsuario);
  }

  async cambiarPassword(idUsuario: number, nuevaClave: string) {
    await this.obtenerUsuarioPorId(idUsuario);
    const passwordHash = await bcrypt.hash(nuevaClave, 10);

    await this.db
      .update(usuario)
      .set({ passwordHash })
      .where(eq(usuario.idUsuario, idUsuario));

    return { mensaje: 'Contraseña actualizada con éxito' };
  }

  async inactivarUsuario(idUsuario: number) {
    await this.obtenerUsuarioPorId(idUsuario);
    await this.db
      .update(usuario)
      .set({ activo: false })
      .where(eq(usuario.idUsuario, idUsuario));
    return { mensaje: 'Usuario inactivado' };
  }

  async reactivarUsuario(idUsuario: number) {
    await this.obtenerUsuarioPorId(idUsuario);
    await this.db
      .update(usuario)
      .set({ activo: true })
      .where(eq(usuario.idUsuario, idUsuario));
    return { mensaje: 'Usuario reactivado' };
  }

  // ===================== PERFIL =====================

  async obtenerMiPerfil(idUsuario: number) {
    return this.obtenerUsuarioPorId(idUsuario);
  }

  // ===================== AUTORREGISTRO =====================

  async verificarParaRegistro(documentoIdentidad: string) {
    const [p] = await this.db
      .select()
      .from(persona)
      .where(eq(persona.documentoIdentidad, documentoIdentidad))
      .limit(1);

    if (!p) {
      throw new NotFoundException('Documento no encontrado en el padrón de colaboradores');
    }

    // Verificar si ya tiene usuario creado
    const [u] = await this.db
      .select()
      .from(usuario)
      .where(eq(usuario.idPersonaSnapshot, p.idPersona))
      .limit(1);

    if (u) {
      throw new ConflictException('Ya existe una cuenta creada para este documento');
    }

    return {
      idPersona: p.idPersona,
      nombres: p.nombres,
      apellidoPaterno: p.apellidoPaterno,
      email: p.email,
    };
  }
}
