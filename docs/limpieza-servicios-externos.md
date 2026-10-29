# Limpieza de servicios externos

Fecha: 29/10/2026

## Alcance

Se eliminaron los servicios que exigían contenedores Docker, salvo Oracle:

- Keycloak (y su PostgreSQL): reemplazado por autenticación propia con JWT HS256.
- MinIO: retirado; el almacenamiento de imágenes queda deshabilitado.
- RabbitMQ: retirado; las notificaciones se despachan de forma síncrona.
- Ollama: retirado; la IA generativa usa únicamente Gemini vía API key.

`docker-compose.yml` conserva solo Oracle de forma temporal. La migración a
PostgreSQL nativo y un backend equivalente en Node.js quedan para una fase
posterior, pendiente de autorización expresa.

## Autenticación propia

- `POST /api/v1/auth/login` acepta email o username y contraseña.
- Emite un JWT HS256 con claims `email`, `username`, `name` y `roles`.
- `POST /api/v1/auth/logout` no revoca el token: el JWT es stateless y el
  cliente descarta la sesión.
- La sesión del frontend se guarda en `localStorage` bajo `slb.auth.session`.
- El converter de roles lee `roles` y conserva el fallback
  `realm_access.roles` para no romper pruebas existentes.

### Riesgos y límites de seguridad

- Guardar el token en `localStorage` expone la sesión ante XSS. Se eligió por
  simplicidad; una cookie `HttpOnly` requiere cambiar el flujo de autenticación.
- Desactivar un usuario no revoca tokens ya emitidos: siguen siendo válidos
  hasta `expiraEn` (60 minutos por defecto).
- El cambio de contraseña desde el perfil no está implementado. Las
  contraseñas de Keycloak no eran exportables. Un administrador puede usar el
  endpoint existente de actualización de usuario; el autoservicio queda pendiente.
- Configurar `JWT_SECRET` (mínimo 32 bytes) y `GEMINI_API_KEY` por variables de
  entorno. El valor por defecto de desarrollo no debe usarse en producción.

## Funciones rotas o degradadas

| Función | Estado | Detalle |
| --- | --- | --- |
| Subida de imágenes (noticias, actividades, teams, avatares, perfil) | Rota | `AlmacenamientoImagenService.guardar` valida y luego lanza `BusinessRuleException` porque no hay proveedor. |
| Visualización de imágenes históricas guardadas como clave | Degradada | Solo se muestran URLs absolutas (`http`, `https`) y `data:`. Las claves internas devuelven `null`/`undefined`. |
| Adjuntos de comentarios sociales | Rota | Dependen de `AlmacenamientoImagenService`; no se pueden subir ni eliminar archivos. |
| Eliminación de imágenes | No-op | Sin almacenamiento físico no hay objeto que borrar. |
| Notificaciones | Degradada | El envío es síncrono dentro de la petición; un SMTP lento aumenta la latencia. Los fallos de email se guardan en `NOTIFICACION_DLQ` para el scheduler de reintentos. |
| Notificaciones in-app | Operativa | Se persisten directamente en base de datos. |
| Recomendaciones IA | Operativa con Gemini | Ollama fue retirado. Sin `GEMINI_API_KEY` las llamadas fallan. |
| Correo SMTP/Gmail | Operativa | No requiere Docker; se conserva la configuración existente. |

## Reemplazos futuros sugeridos

- Almacenamiento: disco local del servidor, S3 compatible o servicio cloud.
- Notificaciones: cola gestionada o outbox transaccional si se requiere
  desacoplar la latencia del SMTP.
- Autenticación: refresh tokens o cookies `HttpOnly` si se endurece la sesión.
- IA: mantener Gemini u otro proveedor por HTTP; no reintroducir Ollama local.

## Verificación

No se ejecutaron compilaciones, pruebas, `npm install` ni el backend. Tampoco
se arrancó el perfil `dev` ni se ejecutó DDL. El lockfile de Angular conserva
entradas de Keycloak hasta ejecutar `npm install` en la fase de verificación.
