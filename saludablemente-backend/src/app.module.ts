import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { DbModule } from './db/db.module';
import { R2StorageModule } from './storage/r2-storage.module';

// Módulos de Dominio (14 dominios oficiales)
import { AuthModule } from './modules/auth/auth.module';
import { CatalogoModule } from './modules/catalogo/catalogo.module';
import { SeguridadModule } from './modules/seguridad/seguridad.module';
import { PersonalModule } from './modules/personal/personal.module';
import { TeamsModule } from './modules/teams/teams.module';
import { ActividadesModule } from './modules/actividades/actividades.module';
import { AsistenciasModule } from './modules/asistencias/asistencias.module';
import { NutricionalModule } from './modules/nutricional/nutricional.module';
import { AptitudFisicaModule } from './modules/aptitud-fisica/aptitud-fisica.module';
import { AlertasModule } from './modules/alertas/alertas.module';
import { MetasModule } from './modules/metas/metas.module';
import { NoticiasModule } from './modules/noticias/noticias.module';
import { NotificacionesModule } from './modules/notificaciones/notificaciones.module';
import { RecomendacionesIaModule } from './modules/recomendaciones-ia/recomendaciones-ia.module';
import { SocialModule } from './modules/social/social.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env', '.env.local'],
    }),
    DbModule,
    R2StorageModule,

    // Registro de todos los módulos del backend
    AuthModule,
    CatalogoModule,
    SeguridadModule,
    PersonalModule,
    TeamsModule,
    ActividadesModule,
    AsistenciasModule,
    NutricionalModule,
    AptitudFisicaModule,
    AlertasModule,
    MetasModule,
    NoticiasModule,
    NotificacionesModule,
    RecomendacionesIaModule,
    SocialModule,
  ],
})
export class AppModule {}
