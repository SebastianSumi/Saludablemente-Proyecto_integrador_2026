import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import { SeguridadService } from './seguridad.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { CurrentUser, JwtPayload } from '../../common/decorators/current-user.decorator';

@Controller()
export class SeguridadController {
  constructor(private readonly seguridadService: SeguridadService) {}

  // ===================== ROLES (/api/v1/roles) =====================

  @Get('roles')
  @UseGuards(JwtAuthGuard)
  async listarRoles() {
    return this.seguridadService.listarRoles();
  }

  // ===================== USUARIOS (/api/v1/usuarios) =====================

  @Get('usuarios')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async listarUsuarios(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.seguridadService.listarUsuarios(Number(page), Number(size));
  }

  @Get('usuarios/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async obtenerUsuario(@Param('id', ParseIntPipe) id: number) {
    return this.seguridadService.obtenerUsuarioPorId(id);
  }

  @Post('usuarios')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async crearUsuario(
    @Body()
    body: {
      email: string;
      username?: string;
      password: string;
      idPersona: number;
      rolesIds: number[];
    }
  ) {
    return this.seguridadService.crearUsuario(body);
  }

  @Put('usuarios/:id/roles')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async actualizarRoles(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { rolesIds: number[] }
  ) {
    return this.seguridadService.actualizarRoles(id, body.rolesIds);
  }

  @Put('usuarios/:id/password')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async cambiarPassword(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { password: string }
  ) {
    return this.seguridadService.cambiarPassword(id, body.password);
  }

  @Delete('usuarios/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async inactivarUsuario(@Param('id', ParseIntPipe) id: number) {
    return this.seguridadService.inactivarUsuario(id);
  }

  @Post('usuarios/:id/reactivar')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async reactivarUsuario(@Param('id', ParseIntPipe) id: number) {
    return this.seguridadService.reactivarUsuario(id);
  }

  // ===================== MI PERFIL (/api/v1/mi-perfil) =====================

  @Get('mi-perfil')
  @UseGuards(JwtAuthGuard)
  async obtenerMiPerfil(@CurrentUser() user: JwtPayload) {
    return this.seguridadService.obtenerMiPerfil(Number(user.sub));
  }

  // ===================== AUTORREGISTRO (/api/v1/registro) =====================

  @Post('registro/verificar')
  async verificarParaRegistro(@Body() body: { documentoIdentidad: string }) {
    return this.seguridadService.verificarParaRegistro(body.documentoIdentidad);
  }

  @Post('registro')
  async registrarColaborador(
    @Body()
    body: {
      email: string;
      password: string;
      idPersona: number;
    }
  ) {
    // Rol Colaborador por defecto en autorregistro público
    const roles = await this.seguridadService.listarRoles();
    const rolColaborador = roles.find((r) => r.nombre === 'Colaborador');
    const rolesIds = rolColaborador ? [rolColaborador.idRol] : [];

    return this.seguridadService.crearUsuario({
      ...body,
      rolesIds,
    });
  }
}
