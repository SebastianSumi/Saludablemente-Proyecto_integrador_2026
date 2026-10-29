import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { RecomendacionesIaService } from './recomendaciones-ia.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';

@Controller('recomendaciones-ia')
@UseGuards(JwtAuthGuard)
export class RecomendacionesIaController {
  constructor(private readonly recomendacionesService: RecomendacionesIaService) {}

  @Get('persona/:idPersona')
  async listarPorPersona(
    @Param('idPersona', ParseIntPipe) idPersona: number,
    @Query('page') page: string = '0',
    @Query('size') size: string = '20'
  ) {
    return this.recomendacionesService.listarPorPersona(
      idPersona,
      Number(page),
      Number(size)
    );
  }

  @Get(':id')
  async obtenerPorId(@Param('id', ParseIntPipe) id: number) {
    return this.recomendacionesService.obtenerPorId(id);
  }

  @Post('generar')
  @UseGuards(RolesGuard)
  @Roles('Administrador', 'Evaluador')
  async encolarGeneracion(
    @Body()
    body: {
      idPersona: number;
      prompt: string;
      datosEntrada: any;
    }
  ) {
    return this.recomendacionesService.encolarGeneracion(
      body.idPersona,
      body.prompt,
      body.datosEntrada
    );
  }
}
