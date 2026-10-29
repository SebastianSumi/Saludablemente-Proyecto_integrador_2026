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
import { PersonalService } from './personal.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller()
export class PersonalController {
  constructor(private readonly personalService: PersonalService) {}

  // ===================== PERSONAL (/api/v1/personal) =====================

  @Get('personal')
  @UseGuards(JwtAuthGuard)
  async listarPersonas(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.personalService.listarPersonas(Number(page), Number(size));
  }

  @Get('personal/resumen')
  @UseGuards(JwtAuthGuard)
  async listarResumen() {
    return this.personalService.listarResumen();
  }

  @Get('personal/:id')
  @UseGuards(JwtAuthGuard)
  async obtenerPersona(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.obtenerPersonaPorId(id);
  }

  @Get('personal/equipo/:idEquipo')
  @UseGuards(JwtAuthGuard)
  async listarPorEquipo(@Param('idEquipo', ParseIntPipe) idEquipo: number) {
    return this.personalService.listarPorEquipo(idEquipo);
  }

  @Post('personal')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async crearPersona(
    @Body()
    body: {
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
    }
  ) {
    return this.personalService.crearPersona(body);
  }

  @Put('personal/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async actualizarPersona(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.personalService.actualizarPersona(id, body);
  }

  @Patch('personal/:id/celular')
  @UseGuards(JwtAuthGuard)
  async actualizarCelular(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { celular: string }
  ) {
    return this.personalService.actualizarCelular(id, body.celular);
  }

  @Delete('personal/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async inactivarPersona(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.inactivarPersona(id);
  }

  @Patch('personal/:id/reactivar')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async reactivarPersona(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.reactivarPersona(id);
  }

  @Delete('personal/:id/avatar')
  @UseGuards(JwtAuthGuard)
  async eliminarAvatar(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.eliminarAvatar(id);
  }

  @Get('personal/:id/preferencias')
  @UseGuards(JwtAuthGuard)
  async obtenerPreferencias(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.obtenerPreferencias(id);
  }

  @Put('personal/:id/preferencias')
  @UseGuards(JwtAuthGuard)
  async actualizarPreferencias(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { canalPreferido: string; recibirNotificaciones: boolean }
  ) {
    return this.personalService.actualizarPreferencias(id, body);
  }

  @Post('personal/:id/credencial')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async generarCredencial(@Param('id', ParseIntPipe) id: number) {
    return this.personalService.generarCredencial(id);
  }

  // ===================== CREDENCIALES PÚBLICAS (/api/v1/credenciales) =====================

  @Post('credenciales/verificar')
  async verificarCredencial(@Body() body: { codigoQr: string }) {
    return this.personalService.verificarCredencial(body.codigoQr);
  }
}
