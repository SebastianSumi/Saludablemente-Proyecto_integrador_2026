import { Module } from '@nestjs/common';
import { NutricionalController } from './nutricional.controller';
import { NutricionalService } from './nutricional.service';

@Module({
  controllers: [NutricionalController],
  providers: [NutricionalService],
  exports: [NutricionalService],
})
export class NutricionalModule {}
