import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  PutMapping,
  Query,
  UseGuards,
} from '@nestjs/common';
import { NotificacionesService } from './notificaciones.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { CurrentUser, JwtPayload } from '../../common/decorators/current-user.decorator';

@Controller('notificaciones')
@UseGuards(JwtAuthGuard)
export class NotificacionesController {
  constructor(private readonly notificacionesService: NotificacionesService) {}

  @Get()
  async listarMisNotificaciones(
    @CurrentUser() user: JwtPayload,
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.notificacionesService.listarMisNotificaciones(
      Number(user.sub),
      Number(page),
      Number(size)
    );
  }

  @Get('contador-no-leidas')
  async contarNoLeidas(@CurrentUser() user: JwtPayload) {
    return this.notificacionesService.contarNoLeidas(Number(user.sub));
  }

  @PutMapping(':id/marcar-leida')
  async marcarLeida(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.notificacionesService.marcarLeida(id, Number(user.sub));
  }

  @Delete(':id')
  async eliminarNotificacion(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.notificacionesService.eliminarNotificacion(id, Number(user.sub));
  }

  // ===================== PLANTILLAS (/api/v1/notificaciones/plantillas) =====================

  @Get('plantillas')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async listarPlantillas() {
    return this.notificacionesService.listarPlantillas();
  }

  @Get('plantillas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async obtenerPlantilla(@Param('id', ParseIntPipe) id: number) {
    return this.notificacionesService.obtenerPlantillaPorId(id);
  }

  @Post('plantillas')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async crearPlantilla(@Body() body: any) {
    return this.notificacionesService.crearPlantilla(body);
  }

  @PutMapping('plantillas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async actualizarPlantilla(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.notificacionesService.actualizarPlantilla(id, body);
  }

  @Delete('plantillas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async eliminarPlantilla(@Param('id', ParseIntPipe) id: number) {
    return this.notificacionesService.eliminarPlantilla(id);
  }
}
