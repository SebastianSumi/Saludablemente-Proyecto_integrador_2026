import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  UseGuards,
} from '@nestjs/common';
import { MetasService } from './metas.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';

@Controller('metas')
@UseGuards(JwtAuthGuard)
export class MetasController {
  constructor(private readonly metasService: MetasService) {}

  @Post('personales')
  async crearMetaPersonal(@Body() body: any) {
    return this.metasService.crearMeta(body);
  }

  @Post('clinicas')
  async crearMetaClinica(@Body() body: any) {
    return this.metasService.crearMeta(body);
  }

  @Get('personales/persona/:idPersona')
  async listarPorPersona(@Param('idPersona', ParseIntPipe) idPersona: number) {
    return this.metasService.listarPorPersona(idPersona);
  }

  @Get('personales/:idMeta/progreso')
  async obtenerProgreso(@Param('idMeta', ParseIntPipe) idMeta: number) {
    return this.metasService.obtenerProgreso(idMeta);
  }

  @Post('checkins')
  async registrarCheckIn(@Body() body: any) {
    return this.metasService.registrarCheckIn(body);
  }

  @Get('checkins/meta/:idMeta')
  async listarCheckIns(@Param('idMeta', ParseIntPipe) idMeta: number) {
    return this.metasService.listarCheckIns(idMeta);
  }
}
