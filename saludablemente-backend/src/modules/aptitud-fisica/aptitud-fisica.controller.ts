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
import { AptitudFisicaService } from './aptitud-fisica.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller('aptitud')
@UseGuards(JwtAuthGuard)
export class AptitudFisicaController {
  constructor(private readonly aptitudService: AptitudFisicaService) {}

  @Get()
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador', 'Coordinador')
  async listarEvaluaciones(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.aptitudService.listarEvaluaciones(Number(page), Number(size));
  }

  @Get(':id')
  async obtenerEvaluacion(@Param('id', ParseIntPipe) id: number) {
    return this.aptitudService.obtenerEvaluacionPorId(id);
  }

  @Post()
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async crearEvaluacion(@Body() body: any) {
    return this.aptitudService.crearEvaluacion(body);
  }

  @PutMapping(':id/observaciones')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarObservaciones(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { observaciones: string }
  ) {
    return this.aptitudService.actualizarObservaciones(id, body.observaciones);
  }

  @PostMapping(':idEvaluacion/cardio')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async guardarCardio(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.aptitudService.guardarCardio(idEvaluacion, body);
  }

  @PutMapping(':idEvaluacion/cardio')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarCardio(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.aptitudService.guardarCardio(idEvaluacion, body);
  }

  @PostMapping(':idEvaluacion/fuerza')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async guardarFuerza(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.aptitudService.guardarFuerza(idEvaluacion, body);
  }

  @PutMapping(':idEvaluacion/fuerza')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async actualizarFuerza(
    @Param('idEvaluacion', ParseIntPipe) idEvaluacion: number,
    @Body() body: any
  ) {
    return this.aptitudService.guardarFuerza(idEvaluacion, body);
  }
}
