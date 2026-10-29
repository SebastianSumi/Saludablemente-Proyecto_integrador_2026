# Discrepancias y decisiones necesarias

No son enmiendas aprobadas. Registrar decisión numerada, responsable y evidencia antes de convertirlas en código.

**Evolución:** E03 y [contratos-cerrados-v1.md](contratos-cerrados-v1.md) implementan
las definiciones contractuales de C03/C04/C05/C06/C07 y la resolución de miembros de C19.
C18 tiene transporte y firmas definidos; su cierre funcional (entrega, enlaces,
plantillas y deduplicación) sigue en F14. Las filas siguientes conservan el diagnóstico
original, no deben interpretarse como ausencia actual de esas APIs. C08 sigue abierto.

| ID | Especificación / código o conflicto | Impacto / cierre requerido |
|---|---|---|
| C01 | Maestro Boot 4.0.7; pom real 4.1.1 | Mantener versión real; no downgrade documental |
| C02 | Maestro dev update; código dev create | No arrancar ni cambiar perfil por esta entrega; acordar provisión/migración antes de F0 |
| C03 | Maestro atribuye equipos a personal; existe teams y falta personal.api.social | Acordar dueño y contrato antes de F4; no trasladar entidades |
| C04 | APIs de actividades y noticias distintas/incompletas para social | Acordar extensiones con cada dueño antes de F3/F4/F15/F16 |
| C05 | validar inexistente; guardar retorna key, no URL; validación image/* y 10 MiB | Acordar adaptación en F6 y representación de adjuntos |
| C06 | Multipart global 15MB; objetivo social 5MB por adjunto | Aplicar límite local al implementar sin perjudicar otros módulos |
| C07 | Seguridad rechaza inactivo con 404; RN-34 exige BusinessRuleException/409 | Definir traducción local y fallback de autores, sin cambiar handler global |
| C08 | Dependencias social→actividades/noticias y llamadas inversas directas | Posibles ciclos Modulith aun usando APIs; diseñar solución con dueños y demostrar modules.verify(), no asumir que NamedInterface elimina ciclos |
| C09 | allowedDependencies del maestro lista nombres planos de NamedInterface | Verificar sintaxis contra convenciones reales y Modulith 2.1.1; no copiar sin comprobación. shared es OPEN, no se observó NamedInterface shared-storage |
| C10 | RN-40 permite post solo con imagen; POST.TITULO no nullable | Decidir representación y validación antes de F2/CRUD manual |
| C11 | RN-13 elimina raíz y respuestas pero pide mostrar padre eliminado | Acordar consulta/render de tombstone y qué respuestas siguen visibles; no filtrar todo automáticamente |
| C12 | RN-27 primera vista única; RN-30 purga historial a 180 días | Tras purga se puede contar nuevamente al mismo usuario; definir semántica histórica C1 antes de F9 |
| C13 | RN-31 alcance autor/admin; §21 P-04 lo vuelve a preguntar | Mantener RN-31 como objetivo explícito; registrar cierre formal sin hacerlo público |
| C14 | Búsqueda incluye comentarios (HU-20); selector REST enumera POST/AUTOR/HASHTAG | Falta contrato para comentarios; no inventar tipo ni endpoint |
| C15 | §21.3 exige un share por usuario/post; no figura en RN ni restricción inequívoca | Definir unicidad, borrado y contadores sin recuperar tabla SHARE del plan |
| C16 | Auto-pin oficial por tiempo y hasta tres pins/scope | Definir cupo, expiración, concurrencia y restauración; comprobar comportamiento Oracle con SCOPE_ID null |
| C17 | Menciones usuario/actividad/equipo; tokens exactos pendientes | Cerrar P-02 antes de F8; no introducir @noticia del plan secundario |
| C18 | Eventos de dominio definidos, transporte pendiente; código enviar usa idPersona | Definir puente y mapeo idUsuario↔idPersona, sin usar ID por equivalencia |
| C19 | NuevoPostDeEquipoEvent sin destinatarios en v1.0 según maestro | Registrar degradación de HU-33; no declarar notificación a equipo completa |
| C20 | Orden F3 precede F4 y F5 usa fijados sin fase separada | Hacer contratos/adaptaciones necesarios primero; mantener numeración original y documentar dependencias reales |
| C21 | Maestro tiene permisos de edición propios/equipo/admin y CRUD en servicios sin fase explícita única | Distribuir explícitamente CRUD y FijadoService en plan-ejecucion.md; confirmar permisos de eliminación por equipo |
| C22 | Maestro asume Keycloak, MinIO y RabbitMQ; el usuario ordenó retirarlos (29/10/2026) | JWT propio operativo; almacenamiento de adjuntos y subida de imágenes deshabilitados; notificaciones síncronas con DLQ en tabla. Cerrar proveedor de almacenamiento y estrategia de entrega antes de F6/F14. Detalle en limpieza-servicios-externos.md |

## Maestro frente al plan anterior

El maestro reemplaza los snapshots de autor por live lookup: no persistir AUTOR_NOMBRE/EMAIL/ROL/EQUIPO/FOTO del plan. El modelo maestro contiene 13 tablas y comparte mediante POST con TIPO_CONTENIDO=SHARE; no crear la tabla SHARE adicional del plan. Mantener REACCION_POST y REACCION_COMENTARIO separadas. El contrato de seguridad es SocialUsuarioApi v1.2, no nombres de métodos sugeridos en el plan previo. El plan secundario no puede cerrar pendientes del maestro.
