import {
  CanActivate,
  ExecutionContext,
  Inject,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { usuario } from '../../db/schema';
import { JwtPayload } from '../decorators/current-user.decorator';

@Injectable()
export class UsuarioActivoGuard implements CanActivate {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDb) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const { user } = context.switchToHttp().getRequest<{ user?: JwtPayload }>();

    if (!user || !user.email) {
      return true; // Si no hay usuario en contexto, JwtAuthGuard ya habrá actuado o es ruta pública
    }

    const [usuarioDb] = await this.db
      .select({ id: usuario.idUsuario, activo: usuario.activo })
      .from(usuario)
      .where(eq(usuario.email, user.email))
      .limit(1);

    if (!usuarioDb || !usuarioDb.activo) {
      throw new UnauthorizedException(
        'Usuario no registrado en el sistema o inactivo. Contacte al administrador.'
      );
    }

    return true;
  }
}
