import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { FastifyReply, FastifyRequest } from 'fastify';

const HTTP_STATUS_NAMES: Record<number, string> = {
  [HttpStatus.BAD_REQUEST]: 'Bad Request',
  [HttpStatus.UNAUTHORIZED]: 'Unauthorized',
  [HttpStatus.FORBIDDEN]: 'Forbidden',
  [HttpStatus.NOT_FOUND]: 'Not Found',
  [HttpStatus.CONFLICT]: 'Conflict',
  [HttpStatus.INTERNAL_SERVER_ERROR]: 'Internal Server Error',
  [HttpStatus.BAD_GATEWAY]: 'Bad Gateway',
};

@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(GlobalExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<FastifyReply>();
    const request = ctx.getRequest<FastifyRequest>();

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Error interno del servidor';
    let errorName = 'Internal Server Error';

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      errorName = HTTP_STATUS_NAMES[status] || 'Error';
      const responseBody = exception.getResponse();

      if (typeof responseBody === 'string') {
        message = responseBody;
      } else if (typeof responseBody === 'object' && responseBody !== null) {
        const bodyObj = responseBody as Record<string, any>;
        if (Array.isArray(bodyObj.message)) {
          // Fallo de validación (class-validator) en formato Spring Boot: "campo: mensaje; campo2: mensaje"
          message = bodyObj.message.join('; ');
        } else if (typeof bodyObj.message === 'string') {
          message = bodyObj.message;
        } else {
          message = exception.message;
        }

        if (bodyObj.error && typeof bodyObj.error === 'string') {
          errorName = bodyObj.error;
        }
      }
    } else if (exception instanceof Error) {
      this.logger.error(`Excepción no controlada en ${request.url}: ${exception.message}`, exception.stack);
    }

    // Estructura idéntica a GlobalExceptionHandler.java
    const errorResponse = {
      timestamp: new Date().toISOString(),
      status,
      error: errorName,
      message,
      path: request.url,
    };

    response.status(status).send(errorResponse);
  }
}
