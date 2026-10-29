import { Inject, Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { eq, or, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { rol, usuario, usuarioRol } from '../../db/schema';
import { LoginRequestDto, LoginResponseDto } from './dto/login.dto';

@Injectable()
export class AuthService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly jwtService: JwtService
  ) {}

  async login(request: LoginRequestDto): Promise<LoginResponseDto> {
    const rawId = request.identificador.trim();
    const normalized = rawId.toLowerCase();

    // Buscar por email o username (exacto o en minúsculas)
    const [foundUser] = await this.db
      .select()
      .from(usuario)
      .where(
        or(
          eq(usuario.email, rawId),
          eq(usuario.username, rawId),
          eq(usuario.email, normalized),
          eq(usuario.username, normalized),
          eq(sql`lower(${usuario.email})`, normalized),
          eq(sql`lower(${usuario.username})`, normalized)
        )
      )
      .limit(1);

    if (!foundUser) {
      throw new UnauthorizedException('Credenciales inválidas');
    }

    if (!foundUser.activo) {
      throw new UnauthorizedException('La cuenta está inactiva');
    }

    // Verificar hash BCrypt
    const passwordMatch = await bcrypt.compare(request.password, foundUser.passwordHash);
    if (!passwordMatch) {
      throw new UnauthorizedException('Credenciales inválidas');
    }

    // Cargar roles asignados
    const userRoles = await this.db
      .select({ nombre: rol.nombre })
      .from(usuarioRol)
      .innerJoin(rol, eq(usuarioRol.idRol, rol.idRol))
      .where(eq(usuarioRol.idUsuario, foundUser.idUsuario));

    const rolesList = userRoles.map((r) => r.nombre).sort();

    // Construir nombre completo desde los snapshots de anticorrupción
    const nombreCompleto = [
      foundUser.nombresSnapshot,
      foundUser.apellidoPaternoSnapshot,
      foundUser.apellidoMaternoSnapshot,
    ]
      .filter(Boolean)
      .join(' ')
      .trim();

    const ttlMinutos = Number(process.env.JWT_TTL_MINUTOS) || 60;
    const ahoraSegundos = Math.floor(Date.now() / 1000);
    const expiraEnSegundos = ahoraSegundos + ttlMinutos * 60;

    const payload = {
      iss: process.env.JWT_ISSUER || 'saludablemente-backend',
      sub: String(foundUser.idUsuario),
      email: foundUser.email,
      username: foundUser.username,
      name: nombreCompleto,
      roles: rolesList,
    };

    const token = await this.jwtService.signAsync(payload, {
      secret: process.env.JWT_SECRET || 'dev-only-jwt-secret-change-me-0123456789abcdef0123456789abcdef',
      expiresIn: `${ttlMinutos}m`,
    });

    return {
      token,
      email: foundUser.email,
      nombre: nombreCompleto,
      roles: rolesList,
      expiraEn: expiraEnSegundos,
    };
  }
}
