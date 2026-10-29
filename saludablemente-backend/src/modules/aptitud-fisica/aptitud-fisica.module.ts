import { Module } from '@nestjs/common';
import { AptitudFisicaController } from './aptitud-fisica.controller';
import { AptitudFisicaService } from './aptitud-fisica.service';

@Module({
  controllers: [AptitudFisicaController],
  providers: [AptitudFisicaService],
  exports: [AptitudFisicaService],
})
export class AptitudFisicaModule {}
