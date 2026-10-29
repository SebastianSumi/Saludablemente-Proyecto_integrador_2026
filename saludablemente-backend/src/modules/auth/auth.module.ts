import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UsuarioActivoGuard } from '../../common/guards/usuario-activo.guard';

@Module({
  imports: [
    JwtModule.register({
      global: true,
      secret: process.env.JWT_SECRET || 'dev-only-jwt-secret-change-me-0123456789abcdef0123456789abcdef',
      signOptions: {
        issuer: process.env.JWT_ISSUER || 'saludablemente-backend',
        expiresIn: `${process.env.JWT_TTL_MINUTOS || 60}m`,
      },
    }),
  ],
  controllers: [AuthController],
  providers: [AuthService, JwtAuthGuard, RolesGuard, UsuarioActivoGuard],
  exports: [AuthService, JwtAuthGuard, RolesGuard, UsuarioActivoGuard],
})
export class AuthModule {}
