import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  PutMapping,
  Query,
  UseGuards,
} from '@nestjs/common';
import { AsistenciasService } from './asistencias.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller()
@UseGuards(JwtAuthGuard)
export class AsistenciasController {
  constructor(private readonly asistenciasService: AsistenciasService) {}

  // ===================== MARCACIÓN (/api/v1/asistencias) =====================

  @Post('asistencias/marcacion')
  async registrarMarcacion(@Body() body: any) {
    return this.asistenciasService.registrarMarcacion(body);
  }

  @Post('asistencias/marcacion/lote')
  async registrarMarcacionLote(@Body() body: { items: any[] }) {
    return this.asistenciasService.registrarMarcacionLote(body.items);
  }

  @Get('asistencias/actividad/:idActividad')
  async listarPorActividad(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.asistenciasService.listarPorActividad(idActividad);
  }

  @Get('asistencias/historial/:idPersona')
  async historialPersona(@Param('idPersona', ParseIntPipe) idPersona: number) {
    return this.asistenciasService.historialPersona(idPersona);
  }

  // ===================== QR (/api/v1/actividades/{idActividad}/qr) =====================

  @Get('actividades/:idActividad/qr/actual')
  async obtenerQrActual(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.asistenciasService.obtenerQrActual(idActividad);
  }

  @Post('actividades/:idActividad/qr/rotar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async rotarQr(@Param('idActividad', ParseIntPipe) idActividad: number) {
    return this.asistenciasService.rotarQr(idActividad);
  }

  // ===================== EXONERACIONES (/api/v1/exoneraciones) =====================

  @Post('exoneraciones/solicitar')
  async solicitarJustificacion(@Body() body: any) {
    return this.asistenciasService.solicitarJustificacion(body);
  }

  @PutMapping('exoneraciones/:id/dictaminar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async dictaminarJustificacion(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { estado: 'APROBADA' | 'RECHAZADA' }
  ) {
    return this.asistenciasService.dictaminarJustificacion(id, body.estado);
  }

  @Get('exoneraciones')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Coordinador')
  async listarJustificaciones(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.asistenciasService.listarJustificaciones(Number(page), Number(size));
  }

  // ===================== CUARENTENA (/api/v1/cuarentena) =====================

  @Get('cuarentena')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async listarCuarentena(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.asistenciasService.listarCuarentena(Number(page), Number(size));
  }

  @Get('cuarentena/pendientes/count')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async contarCuarentenaPendiente() {
    return this.asistenciasService.contarCuarentenaPendiente();
  }
}
