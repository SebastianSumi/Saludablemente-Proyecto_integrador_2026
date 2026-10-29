-- ============================================================
-- Crear TODOS los esquemas (usuarios Oracle) — Saludablemente ERP
-- Ejecutar como SYSTEM en FREEPDB1.
-- SALUDABLEMENTE_APP debe existir antes de ejecutar este script.
-- Las tablas serán creadas por Hibernate con ddl-auto: create.
-- ============================================================

GRANT DBA TO SALUDABLEMENTE_APP;

-- ============================================================
-- 1. SLB_PERSONAL — Módulo Personal
-- ============================================================
CREATE USER SLB_PERSONAL IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_PERSONAL;

GRANT UNLIMITED TABLESPACE TO SLB_PERSONAL;

-- ============================================================
-- 2. SLB_CATALOGO — Módulo Catálogo
-- ============================================================
CREATE USER SLB_CATALOGO IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_CATALOGO;

GRANT UNLIMITED TABLESPACE TO SLB_CATALOGO;

-- ============================================================
-- 3. SLB_NUTRICIONAL — Módulo Nutricional
-- ============================================================
CREATE USER SLB_NUTRICIONAL IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_NUTRICIONAL;

GRANT UNLIMITED TABLESPACE TO SLB_NUTRICIONAL;

-- ============================================================
-- 4. SLB_APTITUD_FISICA — Módulo Aptitud Física
-- ============================================================
CREATE USER SLB_APTITUD_FISICA IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_APTITUD_FISICA;

GRANT UNLIMITED TABLESPACE TO SLB_APTITUD_FISICA;

-- ============================================================
-- 5. SLB_ALERTAS — Módulo Alertas Clínicas
-- ============================================================
CREATE USER SLB_ALERTAS IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_ALERTAS;

GRANT UNLIMITED TABLESPACE TO SLB_ALERTAS;

-- ============================================================
-- 6. SLB_ACTIVIDADES — Módulo Actividades
-- ============================================================
CREATE USER SLB_ACTIVIDADES IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_ACTIVIDADES;

GRANT UNLIMITED TABLESPACE TO SLB_ACTIVIDADES;

-- ============================================================
-- 7. SLB_ASISTENCIAS — Módulo Asistencias
-- ============================================================
CREATE USER SLB_ASISTENCIAS IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_ASISTENCIAS;

GRANT UNLIMITED TABLESPACE TO SLB_ASISTENCIAS;

-- ============================================================
-- 8. SLB_METAS — Módulo Metas
-- ============================================================
CREATE USER SLB_METAS IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_METAS;

GRANT UNLIMITED TABLESPACE TO SLB_METAS;

-- ============================================================
-- 9. SLB_NOTICIAS — Módulo Noticias
-- ============================================================
CREATE USER SLB_NOTICIAS IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_NOTICIAS;

GRANT UNLIMITED TABLESPACE TO SLB_NOTICIAS;

-- ============================================================
-- 10. SLB_NOTIFICACION — Módulo Notificaciones
-- ============================================================
CREATE USER SLB_NOTIFICACION IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_NOTIFICACION;

GRANT UNLIMITED TABLESPACE TO SLB_NOTIFICACION;

-- ============================================================
-- 11. SLB_SEGURIDAD — Módulo Seguridad
-- ============================================================
CREATE USER SLB_SEGURIDAD IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_SEGURIDAD;

GRANT UNLIMITED TABLESPACE TO SLB_SEGURIDAD;

-- ============================================================
-- 12. SLB_RECOMENDACIONES_IA — Módulo Recomendaciones IA
-- ============================================================
CREATE USER SLB_RECOMENDACIONES_IA IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_RECOMENDACIONES_IA;

GRANT UNLIMITED TABLESPACE TO SLB_RECOMENDACIONES_IA;

-- ============================================================
-- 13. SLB_SOCIAL — Módulo Social
-- ============================================================
CREATE USER SLB_SOCIAL IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SLB_SOCIAL;

GRANT UNLIMITED TABLESPACE TO SLB_SOCIAL;

-- ============================================================
-- 14. SBL_TEAM — Módulo Teams
-- ============================================================
CREATE USER SBL_TEAM IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION,
      CREATE TABLE,
      CREATE VIEW,
      CREATE SEQUENCE,
      CREATE TRIGGER,
      CREATE PROCEDURE
    TO SBL_TEAM;

GRANT UNLIMITED TABLESPACE TO SBL_TEAM;

-- ============================================================
-- Verificación
-- ============================================================
SELECT username FROM dba_users
WHERE username LIKE 'SLB\_%' ESCAPE '\'
   OR username = 'SBL_TEAM'
ORDER BY username;
