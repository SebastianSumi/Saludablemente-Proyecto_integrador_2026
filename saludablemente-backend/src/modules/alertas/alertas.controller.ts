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
import { AlertasService } from './alertas.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { CurrentUser, JwtPayload } from '../../common/decorators/current-user.decorator';

@Controller()
@UseGuards(JwtAuthGuard)
export class AlertasController {
  constructor(private readonly alertasService: AlertasService) {}

  // ===================== ALERTAS (/api/v1/alertas) =====================

  @Get('alertas')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador', 'Coordinador')
  async listarAlertas(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.alertasService.listarAlertas(Number(page), Number(size));
  }

  @Get('alertas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador', 'Coordinador')
  async obtenerAlerta(@Param('id', ParseIntPipe) id: number) {
    return this.alertasService.obtenerAlertaPorId(id);
  }

  @Patch('alertas/:id/atender')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async atenderAlerta(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { observacion?: string }
  ) {
    return this.alertasService.atenderAlerta(id, body?.observacion);
  }

  @Patch('alertas/:id/anular')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async anularAlerta(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { observacion?: string }
  ) {
    return this.alertasService.anularAlerta(id, body?.observacion);
  }

  // ===================== MIS ALERTAS (/api/v1/mis-alertas) =====================

  @Get('mis-alertas')
  async listarMisAlertas(@CurrentUser() user: JwtPayload) {
    return this.alertasService.listarMisAlertas(Number(user.sub));
  }

  // ===================== ETIQUETAS (/api/v1/etiquetas-clinicas) =====================

  @Get('etiquetas-clinicas')
  async listarEtiquetas() {
    return this.alertasService.listarEtiquetas();
  }

  @Get('etiquetas-clinicas/:id')
  async obtenerEtiqueta(@Param('id', ParseIntPipe) id: number) {
    return this.alertasService.obtenerEtiquetaPorId(id);
  }

  @Post('etiquetas-clinicas')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async crearEtiqueta(@Body() body: any) {
    return this.alertasService.crearEtiqueta(body);
  }

  @PutMapping('etiquetas-clinicas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async actualizarEtiqueta(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.alertasService.actualizarEtiqueta(id, body);
  }

  @Delete('etiquetas-clinicas/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async eliminarEtiqueta(@Param('id', ParseIntPipe) id: number) {
    return this.alertasService.eliminarEtiqueta(id);
  }

  // ===================== PARÁMETROS (/api/v1/parametros-evaluacion) =====================

  @Get('parametros-evaluacion')
  async listarParametros() {
    return this.alertasService.listarParametros();
  }

  // ===================== USO DE CARDS (/api/v1/alertaclinica/uso-cards) =====================

  @Get('alertaclinica/uso-cards')
  async listarUsoCards(@CurrentUser() user: JwtPayload) {
    return this.alertasService.listarUsoCards(Number(user.sub));
  }

  @Post('alertaclinica/uso-cards')
  async registrarUsoCard(
    @CurrentUser() user: JwtPayload,
    @Body() body: { ruta: string }
  ) {
    return this.alertasService.registrarUsoCard(Number(user.sub), body.ruta);
  }
}
