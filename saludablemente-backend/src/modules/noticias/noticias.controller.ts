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
import { NoticiasService } from './noticias.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller('noticias')
export class NoticiasController {
  constructor(private readonly noticiasService: NoticiasService) {}

  // ===================== PÚBLICAS (/api/v1/noticias) =====================

  @Get('feed')
  async feed(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.noticiasService.feed(Number(page), Number(size));
  }

  @Get('categorias/public')
  async listarCategoriasPublicas() {
    return this.noticiasService.listarCategorias();
  }

  @Get(':id')
  async obtenerNoticia(@Param('id', ParseIntPipe) id: number) {
    return this.noticiasService.obtenerNoticiaPorId(id);
  }

  // ===================== ADMIN (/api/v1/noticias/admin) =====================

  @Get('admin')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async listarAdminNoticias(
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.noticiasService.listarAdminNoticias(Number(page), Number(size));
  }

  @Post('admin')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async crearNoticia(@Body() body: any) {
    return this.noticiasService.crearNoticia(body);
  }

  @PutMapping('admin/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async actualizarNoticia(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.noticiasService.actualizarNoticia(id, body);
  }

  @Patch('admin/:id/publicar')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async publicarNoticia(@Param('id', ParseIntPipe) id: number) {
    return this.noticiasService.publicarNoticia(id);
  }

  @Patch('admin/:id/despublicar')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async despublicarNoticia(@Param('id', ParseIntPipe) id: number) {
    return this.noticiasService.despublicarNoticia(id);
  }

  // ===================== CATEGORÍAS ADMIN (/api/v1/noticias/categorias) =====================

  @Get('categorias')
  @UseGuards(JwtAuthGuard)
  async listarCategorias() {
    return this.noticiasService.listarCategorias();
  }

  @Post('categorias')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async crearCategoria(@Body() body: { nombre: string; descripcion?: string }) {
    return this.noticiasService.crearCategoria(body.nombre, body.descripcion);
  }

  @PutMapping('categorias/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async actualizarCategoria(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { nombre: string; descripcion?: string }
  ) {
    return this.noticiasService.actualizarCategoria(id, body);
  }

  @Delete('categorias/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('Administrador')
  async eliminarCategoria(@Param('id', ParseIntPipe) id: number) {
    return this.noticiasService.eliminarCategoria(id);
  }
}
