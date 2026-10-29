import { NestFactory } from '@nestjs/core';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';
import { GlobalExceptionFilter } from './common/filters/global-exception.filter';

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>(
    AppModule,
    new FastifyAdapter({
      logger: process.env.NODE_ENV !== 'production',
    })
  );

  // 1. Prefijo Global compatible con frontend Angular (/api/v1)
  app.setGlobalPrefix('api/v1');

  // 2. Configuración CORS compatible con SecurityConfig.java
  app.enableCors({
    origin: [
      'http://localhost:4200',
      /^https:\/\/.*\.trycloudflare\.com$/,
    ],
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['*'],
    exposedHeaders: ['X-Trace-ID'],
    credentials: true,
  });

  // 3. Validación automática de DTOs con class-validator
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    })
  );

  // 4. Formato de errores idéntico a GlobalExceptionHandler de Spring Boot
  app.useGlobalFilters(new GlobalExceptionFilter());

  const port = Number(process.env.PORT) || 8080;
  await app.listen(port, '0.0.0.0');
  console.log(`Saludablemente Backend (NestJS + Fastify) ejecutándose en http://localhost:${port}/api/v1`);
}

bootstrap();
