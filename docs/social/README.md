# Transferencia de contexto — Saludablemente / social

Fecha de inspección: 23/10/2026. Alcance de esta entrega: documentación, inventario y planificación. No autoriza iniciar F0 ni ejecutar SQL.

Repositorio indicado por el usuario y registrado en Codex como **Saludablemente SOCIAL**:
`D:\ING SISTEMAS\LP2\PROYECTO 2.0\CODIGO\LOCAL`.
Contiene `SaludabementeERP` y `saludablemente-frontend`. Codex lo registra sin repositorio Git; no se presupone branch ni commit.

## Orden de lectura

1. `../../AGENTS.md`: límites para trabajar.
2. `estado.md` e `inventario.md`: qué está comprobado.
3. `requerimientos.md`: alcance, las 36 HU y las 40 RN completas.
4. `arquitectura.md` y `modelo-oracle.md`: límites, entidades y Oracle SLB_SOCIAL.
5. `contratos.md`, `contratos-verificados.md` y `apis.md`: objetivo frente a contratos reales.
6. `discrepancias.md`: conflictos que no deben resolverse por suposición.
7. `frontend.md`, `fases.md`, `plan-ejecucion.md`, `pruebas.md`.

## Prioridad y procedencia

- Fuente normativa: `fuentes/documento-maestro-v1.0.md`, recuperada del adjunto «Texto pegado(1).txt» de la conversación `6ada603e-9a64-83e8-9c50-29677acecdf5`.
- Fuente secundaria: `fuentes/plan-anterior.md`, adjunto «Texto pegado.txt» de esa conversación.
- Fuente del estado implementado: código local indicado por el usuario. `LOCAL.zip` no se recuperó de la conversación; no se afirma equivalencia binaria entre ese ZIP y esta carpeta.
- La carpeta `Proyecto_integrador_2026-demo` de Descargas se descartó al confirmar el proyecto correcto. Ningún hallazgo de esa copia forma parte de esta transferencia.

Los documentos temáticos transcriben las secciones del maestro, sin reemplazar pendientes por contratos inventados. La fuente íntegra permite verificar contexto y numeración. Las propuestas adicionales se identifican como tales. «APROBADO» en el maestro describe el estado de planificación, no demuestra que una implementación cumpla el contrato.

## Primer encargo para otra sesión

Contratos implementados posteriormente: [contratos-cerrados-v1.md](contratos-cerrados-v1.md).
Consultar su alcance y evidencia antes de reutilizar las firmas históricas del maestro.

Plan operativo solicitado posteriormente: [plan-paso-a-paso.md](plan-paso-a-paso.md),
con alcances backend/frontend, subfases, criterios de aceptación y trazabilidad HU/RN.
Ejemplos JSON y peticiones de prueba: [datos-prueba.json](datos-prueba.json).
Son planificación y fixtures de referencia, no evidencia de pruebas ejecutadas.

Leer AGENTS.md y docs/social. Revalidar inventario y cerrar las decisiones de discrepancias.md que bloquean la fase solicitada. Implementar únicamente la fase que el usuario autorice. Reportar archivos, HU/RN cubiertas, pruebas ejecutadas y pendientes. No declarar el módulo terminado por tener documentación.
