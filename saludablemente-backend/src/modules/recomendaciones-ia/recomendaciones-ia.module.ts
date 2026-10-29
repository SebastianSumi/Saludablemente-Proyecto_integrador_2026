import { Module } from '@nestjs/common';
import { RecomendacionesIaController } from './recomendaciones-ia.controller';
import { RecomendacionesIaService } from './recomendaciones-ia.service';

@Module({
  controllers: [RecomendacionesIaController],
  providers: [RecomendacionesIaService],
  exports: [RecomendacionesIaService],
})
export class RecomendacionesIaModule {}
