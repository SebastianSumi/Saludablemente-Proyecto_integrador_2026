import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  PostMapping,
  Put,
  UseGuards,
} from '@nestjs/common';
import { CatalogoService } from './catalogo.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller()
@UseGuards(JwtAuthGuard, RolesGuard)
export class CatalogoController {
  constructor(private readonly catalogoService: CatalogoService) {}

  // ===================== ÁREAS (/api/v1/areas) =====================

  @Get('areas')
  async listarAreas() {
    return this.catalogoService.listarAreas();
  }

  @Get('areas/resumen')
  async listarAreasResumen() {
    return this.catalogoService.listarAreasResumen();
  }

  @Get('areas/:id')
  async obtenerArea(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.obtenerAreaPorId(id);
  }

  @Post('areas')
  @Roles('Administrador')
  async crearArea(@Body() body: { nombre: string; descripcion?: string }) {
    return this.catalogoService.crearArea(body);
  }

  @Put('areas/:id')
  @Roles('Administrador')
  async actualizarArea(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { nombre: string; descripcion?: string }
  ) {
    return this.catalogoService.actualizarArea(id, body);
  }

  @Delete('areas/:id')
  @Roles('Administrador')
  async eliminarArea(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.eliminarArea(id);
  }

  // ===================== PERIODOS (/api/v1/periodos) =====================

  @Get('periodos')
  async listarPeriodos() {
    return this.catalogoService.listarPeriodos();
  }

  @Get('periodos/resumen')
  async listarPeriodosResumen() {
    return this.catalogoService.listarPeriodosResumen();
  }

  @Get('periodos/vigente')
  async obtenerPeriodoVigente() {
    return this.catalogoService.obtenerPeriodoVigente();
  }

  @Get('periodos/codigo/:codigo')
  async obtenerPeriodoPorCodigo(@Param('codigo') codigo: string) {
    return this.catalogoService.obtenerPeriodoPorCodigo(codigo);
  }

  @Get('periodos/:id')
  async obtenerPeriodo(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.obtenerPeriodoPorId(id);
  }

  @Post('periodos')
  @Roles('Administrador')
  async crearPeriodo(
    @Body()
    body: {
      codigo: string;
      descripcion: string;
      fechaInicio: string;
      fechaFin: string;
    }
  ) {
    return this.catalogoService.crearPeriodo(body);
  }

  @Put('periodos/:id')
  @Roles('Administrador')
  async actualizarPeriodo(
    @Param('id', ParseIntPipe) id: number,
    @Body()
    body: {
      codigo: string;
      descripcion: string;
      fechaInicio: string;
      fechaFin: string;
    }
  ) {
    return this.catalogoService.actualizarPeriodo(id, body);
  }

  @Patch('periodos/:id/vigente')
  @Roles('Administrador')
  async marcarPeriodoVigente(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.marcarPeriodoVigente(id);
  }

  @Patch('periodos/:id/cerrar')
  @Roles('Administrador')
  async cerrarPeriodo(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.cerrarPeriodo(id);
  }

  @Delete('periodos/:id')
  @Roles('Administrador')
  async eliminarPeriodo(@Param('id', ParseIntPipe) id: number) {
    return this.catalogoService.eliminarPeriodo(id);
  }
}
