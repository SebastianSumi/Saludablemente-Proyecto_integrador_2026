import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  PutMapping,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ActividadesService } from './actividades.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller()
@UseGuards(JwtAuthGuard)
export class ActividadesController {
  constructor(private readonly actividadesService: ActividadesService) {}

  // ===================== AGENDA (/api/v1/agenda) =====================

  @Get('agenda/calendario')
  async obtenerCalendario() {
    return this.actividadesService.obtenerCalendario();
  }

  @Get('agenda/hoy')
  async obtenerActividadesHoy() {
    return this.actividadesService.obtenerActividadesHoy();
  }

  @Get('agenda/actividades')
  async listarActividades() {
    return this.actividadesService.listarActividades();
  }

  @Get('agenda/actividades/:id')
  async obtenerActividad(@Param('id', ParseIntPipe) id: number) {
    return this.actividadesService.obtenerActividadPorId(id);
  }

  @Post('agenda/actividades')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async crearActividad(@Body() body: any) {
    return this.actividadesService.crearActividad(body);
  }

  @PutMapping('agenda/actividades/:id/sede')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async actualizarSede(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { lugar: string }
  ) {
    return this.actividadesService.actualizarSede(id, body.lugar);
  }

  // ===================== OPERACIÓN (/api/v1/operacion) =====================

  @Patch('operacion/:id/iniciar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async iniciarActividad(@Param('id', ParseIntPipe) id: number) {
    return this.actividadesService.cambiarEstado(id, 'EN_CURSO');
  }

  @Patch('operacion/:id/finalizar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async finalizarActividad(@Param('id', ParseIntPipe) id: number) {
    return this.actividadesService.cambiarEstado(id, 'FINALIZADA');
  }

  @Patch('operacion/:id/cancelar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async cancelarActividad(@Param('id', ParseIntPipe) id: number) {
    return this.actividadesService.cambiarEstado(id, 'CANCELADA');
  }

  @PutMapping('operacion/:id/coordinador')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async asignarCoordinador(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { idCoordinador: number }
  ) {
    return this.actividadesService.asignarCoordinador(id, body.idCoordinador);
  }

  // ===================== INSCRIPCIONES (/api/v1/actividades/{id}/inscripciones) =====================

  @Get('actividades/:idActividad/inscripciones')
  async listarInscripciones(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.actividadesService.listarInscripciones(idActividad);
  }

  @Get('actividades/:idActividad/inscripciones/count')
  async contarInscripciones(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.actividadesService.contarInscripciones(idActividad);
  }

  @Post('actividades/:idActividad/inscripciones')
  async inscribirPersona(
    @Param('idActividad', ParseIntPipe) idActividad: number,
    @Body() body: { idPersona: number }
  ) {
    return this.actividadesService.inscribirPersona(idActividad, body.idPersona);
  }

  @Delete('actividades/:idActividad/inscripciones/:idPersona')
  async desinscribirPersona(
    @Param('idActividad', ParseIntPipe) idActividad: number,
    @Param('idPersona', ParseIntPipe) idPersona: number
  ) {
    return this.actividadesService.desinscribirPersona(idActividad, idPersona);
  }

  // ===================== CONFIGURACIÓN & MATERIALES (/api/v1/configuracion) =====================

  @Get('configuracion/:id/materiales')
  async listarMateriales(@Param('id', ParseIntPipe) id: number) {
    return this.actividadesService.listarMateriales(id);
  }

  @Post('configuracion/:id/materiales')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async agregarMaterial(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { nombre: string; cantidad: number }
  ) {
    return this.actividadesService.agregarMaterial(id, body.nombre, body.cantidad);
  }

  // ===================== IMÁGENES (/api/v1/agenda/actividades/{id}/imagenes) =====================

  @Get('agenda/actividades/:idActividad/imagenes')
  async listarImagenes(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.actividadesService.listarImagenes(idActividad);
  }

  @Delete('agenda/actividades/:idActividad/imagenes/:idImagen')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async eliminarImagen(@Param('idImagen', ParseIntPipe) idImagen: number) {
    return this.actividadesService.eliminarImagen(idImagen);
  }
}
