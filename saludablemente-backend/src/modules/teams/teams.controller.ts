import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Post,
  PutMapping,
  UseGuards,
} from '@nestjs/common';
import { TeamsService } from './teams.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { CurrentUser, JwtPayload } from '../../common/decorators/current-user.decorator';

@Controller('equipos')
@UseGuards(JwtAuthGuard)
export class TeamsController {
  constructor(private readonly teamsService: TeamsService) {}

  @Get('mi-equipo')
  async obtenerMiEquipo(@CurrentUser() user: JwtPayload) {
    return this.teamsService.obtenerMiEquipo(Number(user.sub));
  }

  @Get('mi-equipo/miembros')
  async obtenerMiembrosMiEquipo(@CurrentUser() user: JwtPayload) {
    return this.teamsService.obtenerMiembrosMiEquipo(Number(user.sub));
  }

  @Get(':id/puede-editar')
  async puedeEditar(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: JwtPayload
  ) {
    const esAdmin = user.roles.includes('Administrador');
    return this.teamsService.puedeEditar(id, Number(user.sub), esAdmin);
  }

  @Get(':id/candidatos-lider')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async listarCandidatosLider(@Param('id', ParseIntPipe) id: number) {
    return this.teamsService.listarCandidatosLider(id);
  }

  @PutMapping(':id/lider')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async asignarLider(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { idUsuario: number | null }
  ) {
    return this.teamsService.asignarLider(id, body.idUsuario);
  }

  @Get()
  async listarEquipos() {
    return this.teamsService.listarEquipos();
  }

  @Get(':id')
  async obtenerEquipo(@Param('id', ParseIntPipe) id: number) {
    return this.teamsService.obtenerEquipoPorId(id);
  }

  @Post()
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async crearEquipo(
    @Body()
    body: {
      nombre: string;
      descripcion?: string;
      color?: string;
      slogan?: string;
      referenciaBiblica?: string;
      textoVersiculo?: string;
    }
  ) {
    return this.teamsService.crearEquipo(body);
  }

  @PutMapping(':id')
  async actualizarEquipo(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: any
  ) {
    return this.teamsService.actualizarEquipo(id, body);
  }

  @Delete(':id')
  @UseGuards(RolesGuard)
  @Roles('Administrador')
  async eliminarEquipo(@Param('id', ParseIntPipe) id: number) {
    return this.teamsService.eliminarEquipo(id);
  }
}
