import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { SocialService } from './social.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { CurrentUser, JwtPayload } from '../../common/decorators/current-user.decorator';

@Controller('social')
@UseGuards(JwtAuthGuard)
export class SocialController {
  constructor(private readonly socialService: SocialService) {}

  // ===================== MURO (/api/v1/social/muro) =====================

  @Get('muro')
  async muro(
    @CurrentUser() user: JwtPayload,
    @Query('tab') tab: string = 'PARA_TI',
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.socialService.muro(tab, Number(page), Number(size), Number(user.sub));
  }

  @Get('muro/equipos/:id')
  async muroEquipo(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.socialService.muroEquipo(id, Number(page), Number(size), Number(user.sub));
  }

  // ===================== POSTS (/api/v1/social/posts) =====================

  @Get('posts/:id')
  async obtenerPostDetalle(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    await this.socialService.registrarVista(id, Number(user.sub));
    return this.socialService.obtenerPostDetalle(id, Number(user.sub));
  }

  @Post('posts')
  async crearPost(@CurrentUser() user: JwtPayload, @Body() body: any) {
    return this.socialService.crearPost(body, Number(user.sub));
  }

  @Patch('posts/:id')
  async editarPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: any
  ) {
    return this.socialService.editarPost(id, body, Number(user.sub));
  }

  @Delete('posts/:id')
  async eliminarPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    const esAdmin = user.roles.includes('Administrador');
    return this.socialService.eliminarPost(id, Number(user.sub), esAdmin);
  }

  @Post('posts/:id/guardar')
  async guardarPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.socialService.guardarPost(id, Number(user.sub), true);
  }

  @Delete('posts/:id/guardar')
  async quitarGuardado(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.socialService.guardarPost(id, Number(user.sub), false);
  }

  @Post('posts/:id/share')
  async compartirPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.socialService.compartirPost(id, Number(user.sub));
  }

  // ===================== COMENTARIOS (/api/v1/social/posts/{post}/comentarios) =====================

  @Get('posts/:post/comentarios')
  async listarComentarios(
    @Param('post', ParseIntPipe) post: number,
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.socialService.listarComentarios(post, Number(page), Number(size));
  }

  @Post('posts/:post/comentarios')
  async crearComentario(
    @Param('post', ParseIntPipe) post: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: { contenido: string; idComentarioPadre?: number }
  ) {
    return this.socialService.crearComentario(
      post,
      body.contenido,
      Number(user.sub),
      body.idComentarioPadre
    );
  }

  @Patch('posts/:post/comentarios/:id')
  async editarComentario(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: { contenido: string }
  ) {
    return this.socialService.editarComentario(id, body.contenido, Number(user.sub));
  }

  @Delete('posts/:post/comentarios/:id')
  async eliminarComentario(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    const esAdmin = user.roles.includes('Administrador');
    return this.socialService.eliminarComentario(id, Number(user.sub), esAdmin);
  }

  // ===================== REACCIONES (/api/v1/social) =====================

  @Post('posts/:id/reacciones')
  async reaccionarPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: { tipo: string }
  ) {
    return this.socialService.reaccionarPost(id, body.tipo, Number(user.sub));
  }

  @Delete('posts/:id/reacciones')
  async quitarReaccionPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.socialService.quitarReaccionPost(id, Number(user.sub));
  }

  @Post('comentarios/:id/reacciones')
  async reaccionarComentario(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: { tipo: string }
  ) {
    return this.socialService.reaccionarComentario(id, body.tipo, Number(user.sub));
  }

  @Delete('comentarios/:id/reacciones')
  async quitarReaccionComentario(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    return this.socialService.quitarReaccionComentario(id, Number(user.sub));
  }

  // ===================== MODERACIÓN & REPORTES =====================

  @Post('posts/:id/reportar')
  async reportarPost(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload,
    @Body() body: { motivo: string; detalle?: string }
  ) {
    return this.socialService.reportar({
      idPost: id,
      motivo: body.motivo,
      detalle: body.detalle,
      idUsuario: Number(user.sub),
    });
  }

  @Get('admin/reportes')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async listarReportesAdmin(
    @Query('estado') estado: string = 'PENDIENTE',
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.socialService.listarReportesAdmin(estado, Number(page), Number(size));
  }
}
