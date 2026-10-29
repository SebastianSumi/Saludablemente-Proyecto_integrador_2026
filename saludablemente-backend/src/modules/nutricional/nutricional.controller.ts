import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  PutMapping,
  Query,
  UseGuards,
} from '@nestjs/common';
import { NutricionalService } from './nutricional.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller()
@UseGuards(JwtAuthGuard)
export class NutricionalController {
  constructor(private readonly nutricionalService: NutricionalService) {}

  // ===================== JORNADAS (/api/v1/jornadas-nutricionales) =====================

  @Get('jornadas-nutricionales')
  async listarJornadas() {
    return this.nutricionalService.listarJornadas();
  }

  @Get('jornadas-nutricionales/:id')
  async obtenerJornada(@Param('id', ParseIntPipe) id: number) {
    return this.nutricionalService.obtenerJornadaPorId(id);
  }

  @Post('jornadas-nutricionales')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async crearJornada(@Body() body: any) {
    return this.nutricionalService.crearJornada(body);
  }

  @PutMapping('jornadas-nutricionales/:id')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarJornada(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.nutricionalService.actualizarJornada(id, body);
  }

  @Patch('jornadas-nutricionales/:id/estado')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async cambiarEstadoJornada(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { estado: string }
  ) {
    return this.nutricionalService.cambiarEstadoJornada(id, body.estado);
  }

  @Get('jornadas-nutricionales/:id/inscripciones')
  async listarInscripcionesJornada(@Param('id', ParseIntPipe) id: number) {
    return this.nutricionalService.listarInscripcionesJornada(id);
  }

  @Post('jornadas-nutricionales/:id/inscripciones')
  async inscribirJornada(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { idPersona: number }
  ) {
    return this.nutricionalService.inscribirJornada(id, body.idPersona);
  }

  // ===================== EVALUACIONES (/api/v1/evaluaciones-nutricionales) =====================

  @Get('evaluaciones-nutricionales')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador', 'Coordinador')
  async listarEvaluaciones(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.nutricionalService.listarEvaluaciones(Number(page), Number(size));
  }

  @Get('evaluaciones-nutricionales/:id')
  async obtenerEvaluacion(@Param('id', ParseIntPipe) id: number) {
    return this.nutricionalService.obtenerEvaluacionPorId(id);
  }

  @Post('evaluaciones-nutricionales')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async crearEvaluacion(@Body() body: any) {
    return this.nutricionalService.crearEvaluacion(body);
  }

  @Patch('evaluaciones-nutricionales/:id/observaciones')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarObservaciones(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { observaciones: string }
  ) {
    return this.nutricionalService.actualizarObservaciones(id, body.observaciones);
  }

  @PostMapping('evaluaciones-nutricionales/:idEvaluacion/antropometrico')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async guardarAntropometria(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.nutricionalService.guardarAntropometria(idEvaluacion, body);
  }

  @PutMapping('evaluaciones-nutricionales/:idEvaluacion/antropometrico')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarAntropometria(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.nutricionalService.guardarAntropometria(idEvaluacion, body);
  }

  @Post('evaluaciones-nutricionales/:idEvaluacion/bioquimico/manual')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async guardarBioquimica(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.nutricionalService.guardarBioquimica(idEvaluacion, body);
  }

  @PutMapping('evaluaciones-nutricionales/:idEvaluacion/bioquimico/manual')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarBioquimica(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.nutricionalService.guardarBioquimica(idEvaluacion, body);
  }
}
