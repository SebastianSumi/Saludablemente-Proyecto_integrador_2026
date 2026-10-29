# Roles y vistas — Saludablemente frontend

> ESTADO ACTUAL: sin restricciones por rol. Todos los usuarios autenticados acceden
> a todo (`AuthGuard` solo exige login). `RoleGuard`, `HasRoleDirective` y los
> `data.roles` quedan documentados para reactivar roles cuando se defina.

Fuente de roles: claim `roles` del JWT propio, expuesto por `AuthService.obtenerRoles()`.

## Admin — `features/admin/...` (datos sensibles, implementado)
Todo lo actual: catálogos, personal (DNI, celular, email terceros), nutricional completo,
aptitud, alertas clínicas y reglas CRUD, dashboards agregados, perfil 360°.

Rutas: `admin` implícito en paths actuales (`personal/*`, `nutricional/*`, `alertaclinica/*`,
`catalogos/*`, `mi-perfil`). `data.roles: ['ADMIN','COORDINADOR','SUPERVISOR']`
(nombres canónicos del backend; por defecto se deja abierto y se cierra por rol).

## Colaborador — `features/colaborador/...` (documentado, shells)
- `/colaborador` → `ColInicio` (mi panel, sin datos de terceros)
- `/colaborador/mi-perfil` → `ColMiPerfil` (solo propios)
- `/colaborador/mis-evaluaciones` → `ColMisEvaluaciones` (solo propias)
- `/colaborador/mis-alertas` → `ColMisAlertas` (solo propias)

`data.roles: ['COLABORADOR']`. Campos prohibidos en colaborador: DNI/celular/email de
terceros, reglas CRUD, dashboards agregados, 360° de otros usuarios.

## Compartido
- `/` inicio según rol (layout filtra con `*appHasRole`).
- `/no-autorizado` → `NoAutorizado`.
- `/mi-perfil` (admin 360°) se mantiene; el ligero de colaborador usará endpoint propio
  sin datos sensibles cuando backend lo exponga (backend intocable por ahora).
