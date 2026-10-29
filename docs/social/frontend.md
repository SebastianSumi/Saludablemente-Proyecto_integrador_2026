# Frontend Angular — integración planificada

El maestro no contiene un diseño Angular, rutas de pantalla, interfaces TypeScript ni una fase exclusiva de frontend. Este archivo es una propuesta de integración derivada de las HU; no un contrato ya aprobado ni implementado.

## Código existente

Angular 22, Taiga UI 5, componentes cargados con loadComponent, layout bajo AuthGuard y permisos con RoleGuard. app.config.ts configura HttpClient, interceptores Keycloak/trace/loading y ejecución zoneless. Reutilizar esa estructura, el entorno y los componentes compartidos; no recrear login, cliente Keycloak ni configuración global.

No se encontraron referencias social en src en la inspección. No se añaden rutas en esta entrega.

## Pantallas propuestas y trazabilidad

| Pantalla/componente propuesto | HU | Integración |
|---|---|---|
| Muro con tabs, filtros, pins y tarjetas | 01–04,13,15,21–25 | F5; feed cronológico y pins de scope |
| Detalle, hilos raíz/respuesta y adjuntos | 05–08,14 | F6; texto eliminado según decisión C11 |
| Selector siete reacciones y lista paginada | 09–11,16 | F7 |
| Editor personal/equipo, edición y borrado | 26,34–36 | F5 y F9; permisos backend como autoridad |
| Autocomplete y hashtags navegables | 21,27 | F8; tokens pendientes |
| Compartir, guardar y alcance restringido | 12,19,32 | F9 |
| Búsqueda unificada | 20 | F10; contrato de comentarios pendiente |
| Perfil público y mi contenido | 17–18 | F11; guardados privados |
| Reporte y moderación admin | 28–30 | F12; reutilizar guard de roles |
| Métricas admin | 31 | F13; ECharts existente si resulta adecuado |
| Navegación desde notificaciones | 33 | F14; deep links por definir |

Posible ubicación futura: src/app/features/social y servicios/modelos propios, sujeta a convenciones del equipo. Las URLs de pantallas no se fijan aquí. Tipos y paginación se tomarán de contratos REST confirmados, sin asumir estructura de Page, códigos de éxito o payloads faltantes. Mantener null de avatar con fallback, carga, error, vacío, accesibilidad por teclado y confirmaciones de borrado. Renderizar contenido como texto seguro; menciones y hashtags con navegación según contrato acordado.

Adjuntos: flujo de dos pasos del maestro (subida y luego comentario con referencias); máximo cuatro, tipos permitidos y tamaño; no subir de nuevo imágenes de actividades/noticias. Cerrar C05 antes de modelar el servicio.

## Actualización E04 — identidad visual de equipos

Al construir respuestas/pantallas Social, consumir SocialEquipoPerfilDto: imagenUrl,
color, slogan, referenciaBiblica, textoVersiculo y líder con identidad visual resuelta
en vivo. Usar el perfil en la cabecera del equipo y nombre/imagen en las publicaciones
como equipo, sin convertir todos los datos del perfil en campos repetidos de cada post.
Sin imagen: fallback; sin líder: no asignado; líder ausente/inactivo: placeholder.
No deducir permisos por el líder mostrado: autorización del backend sigue E03.
El contrato ya expone los datos; las respuestas REST y pantallas Social siguen pendientes.
