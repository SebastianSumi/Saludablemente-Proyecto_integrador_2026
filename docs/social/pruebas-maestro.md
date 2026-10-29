> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 22. Verificación final

## 22.1 Build

```bash
./mvnw clean compile
./mvnw test -Dtest=ModularityTests
./mvnw spring-boot:run
```

## 22.2 Flujo de prueba end-to-end

```http
# 1. Crear actividad (dispara sincronización)
POST /api/v1/agenda/actividades
{ "nombre": "Test Social", ..., "idUsuarioCreador": 1, "idUsuarioCoordinador": 1 }

# 2. Ver muro global
GET /api/v1/social/muro?tab=PARA_TI&page=0&size=10

# 3. Ver detalle
GET /api/v1/social/posts/1

# 4. Comentar (raíz)
POST /api/v1/social/posts/1/comentarios
{ "contenido": "@ana.lazaro excelente actividad #salud" }

# 5. Responder al comentario
POST /api/v1/social/posts/1/comentarios
{ "contenido": "Concuerdo", "idComentarioPadre": 1 }

# 6. Reaccionar al post
POST /api/v1/social/posts/1/reacciones
{ "tipo": "LOVE" }

# 7. Reaccionar al comentario
POST /api/v1/social/comentarios/1/reacciones
{ "tipo": "HAHA" }

# 8. Compartir
POST /api/v1/social/posts/1/share
{ "contenido": "Miren esto" }

# 9. Guardar
POST /api/v1/social/posts/1/guardar

# 10. Ver quién reaccionó
GET /api/v1/social/posts/1/reacciones/usuarios?tipo=LOVE&page=0

# 11. Crear post manual
POST /api/v1/social/posts
{ "titulo": "Anuncio", "resumen": "Hoy hay reunión", "idEquipoAutor": 3 }

# 12. Buscar
GET /api/v1/social/buscar?q=reunión&tipo=POST

# 13. Reportar
POST /api/v1/social/posts/1/reportar
{ "motivo": "SPAM", "detalle": "..." }

# 14. Moderar (como admin)
GET /api/v1/social/admin/reportes?estado=PENDIENTE
PATCH /api/v1/social/admin/reportes/1
{ "estado": "ACEPTADO" }

# 15. Finalizar actividad (dispara actualización del post)
PATCH /api/v1/operacion/1/finalizar

# 16. Verificar que el post muestra FINALIZADA y que el autor sigue enriquecido
GET /api/v1/social/posts/1
```

## 22.3 Swagger

Debe mostrar grupos:
- "Social - Muro"
- "Social - Posts"
- "Social - Comentarios"
- "Social - Reacciones"
- "Social - Búsqueda y Menciones"
- "Social - Perfil y Mi Contenido"
- "Social - Moderación (Admin)"
- "Social - Analítica (Admin)"

## 22.4 `ModularityTests` debe verificar

- `social` no importa `actividades.*` fuera de `actividades.api.*`.
- `social` no importa `noticias.*` fuera de `noticias.api.*`.
- `social` no importa `personal.*` fuera de `personal.api.social.*`.
- `social` no importa `seguridad.*` fuera de `seguridad.api.social.*`.
- `actividades` y `noticias` no importan `social.*` fuera de `social.api.*`.

---

**Fin del documento maestro v1.0.**

Este documento es la referencia única del módulo. Cualquier decisión que lo modifique debe registrarse como enmienda numerada y quedar reflejada en las secciones correspondientes.