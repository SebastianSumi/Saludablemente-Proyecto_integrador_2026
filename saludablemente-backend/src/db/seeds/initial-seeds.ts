import * as bcrypt from 'bcrypt';
import { eq } from 'drizzle-orm';
import { DrizzleDb } from '../db.provider';
import {
  catAreaTrabajo,
  catPeriodo,
  equipo,
  persona,
  rol,
  usuario,
  usuarioRol,
} from '../schema';

export async function seedDatabase(db: DrizzleDb) {
  console.log('=== Iniciando Seeds de Base de Datos (PostgreSQL Nativo) ===');

  // 1. Roles de Seguridad
  console.log('--- Verificando Roles ---');
  const existingRoles = await db.select().from(rol);
  let rolMap = new Map<string, number>();

  if (existingRoles.length === 0) {
    const rolesToInsert = [
      { nombre: 'Administrador', descripcion: 'Acceso total al sistema, gestión de usuarios y configuración' },
      { nombre: 'Evaluador', descripcion: 'Registro de evaluaciones nutricionales y pruebas físicas' },
      { nombre: 'Coordinador', descripcion: 'Supervisión de evaluaciones y reportes del área' },
      { nombre: 'Colaborador', descripcion: 'Acceso de solo lectura a su propio perfil y evaluaciones' },
    ];
    const insertedRoles = await db.insert(rol).values(rolesToInsert).returning();
    insertedRoles.forEach((r) => rolMap.set(r.nombre, r.idRol));
    console.log(`[+] Creados ${insertedRoles.length} roles base`);
  } else {
    existingRoles.forEach((r) => rolMap.set(r.nombre, r.idRol));
    console.log(`[~] Ya existen ${existingRoles.length} roles`);
  }

  // 2. Catálogo: Áreas de Trabajo
  console.log('--- Verificando Áreas de Trabajo ---');
  const existingAreas = await db.select().from(catAreaTrabajo);
  let areaMap = new Map<string, number>();

  if (existingAreas.length === 0) {
    const areasToInsert = [
      { nombre: 'CRAI', descripcion: 'Centro de Recursos para el Aprendizaje y la Investigacion', activo: true },
      { nombre: 'Limpieza', descripcion: 'Personal de limpieza y mantenimiento', activo: true },
      { nombre: 'Logistica', descripcion: 'Area de logistica y almacen', activo: true },
      { nombre: 'DTI-Redes', descripcion: 'Direccion de Tecnologias de la Informacion - Redes', activo: true },
      { nombre: 'Caja', descripcion: 'Area de caja y atencion al publico', activo: true },
      { nombre: 'Servicio de Alimentacion', descripcion: 'Comedor y servicio de alimentacion', activo: true },
    ];
    const insertedAreas = await db.insert(catAreaTrabajo).values(areasToInsert).returning();
    insertedAreas.forEach((a) => areaMap.set(a.nombre, a.idArea));
    console.log(`[+] Creadas ${insertedAreas.length} áreas de trabajo`);
  } else {
    existingAreas.forEach((a) => areaMap.set(a.nombre, a.idArea));
    console.log(`[~] Ya existen ${existingAreas.length} áreas de trabajo`);
  }

  // 3. Catálogo: Periodos
  console.log('--- Verificando Periodos ---');
  const existingPeriodos = await db.select().from(catPeriodo);
  if (existingPeriodos.length === 0) {
    const periodosToInsert = [
      {
        codigo: '2026-1',
        descripcion: 'Primer Semestre 2026',
        fechaInicio: '2026-03-01',
        fechaFin: '2026-07-31',
        estadoPeriodo: 'VIGENTE',
      },
      {
        codigo: '2026-2',
        descripcion: 'Segundo Semestre 2026',
        fechaInicio: '2026-08-01',
        fechaFin: '2026-12-20',
        estadoPeriodo: 'PLANIFICADO',
      },
      {
        codigo: '2025-2',
        descripcion: 'Segundo Semestre 2025',
        fechaInicio: '2025-08-01',
        fechaFin: '2025-12-20',
        estadoPeriodo: 'CERRADO',
      },
    ];
    await db.insert(catPeriodo).values(periodosToInsert);
    console.log('[+] Creados 3 periodos (2026-1 vigente)');
  } else {
    console.log(`[~] Ya existen ${existingPeriodos.length} periodos`);
  }

  // 4. Equipos (Teams)
  console.log('--- Verificando Equipos ---');
  const existingTeams = await db.select().from(equipo);
  let teamMap = new Map<string, number>();

  if (existingTeams.length === 0) {
    const teamsToInsert = [
      { nombre: 'Chirimoya', descripcion: 'Equipo de prueba - area administrativa', activo: true, color: '#4CAF50' },
      { nombre: 'Nutri Bebes', descripcion: 'Equipo de prueba - servicio de alimentacion', activo: true, color: '#2196F3' },
      { nombre: 'Nutria Salvaje', descripcion: 'Equipo de prueba - logistica y limpieza', activo: true, color: '#FF9800' },
    ];
    const insertedTeams = await db.insert(equipo).values(teamsToInsert).returning();
    insertedTeams.forEach((t) => teamMap.set(t.nombre, t.idEquipo));
    console.log(`[+] Creados ${insertedTeams.length} equipos`);
  } else {
    existingTeams.forEach((t) => teamMap.set(t.nombre, t.idEquipo));
    console.log(`[~] Ya existen ${existingTeams.length} equipos`);
  }

  // 5. Personas
  console.log('--- Verificando Personas ---');
  const existingPersonas = await db.select().from(persona);
  let personaDocMap = new Map<string, any>();

  if (existingPersonas.length === 0) {
    const personasToInsert = [
      {
        nombres: 'Ana',
        apellidoPaterno: 'Lazaro',
        apellidoMaterno: 'Matallana',
        documentoIdentidad: '11111111',
        celular: '951357327',
        fechaNacimiento: '1980-03-16',
        sexo: 'F',
        idArea: areaMap.get('CRAI') || null,
        tallaPolo: 'S',
        estadoCivil: 'Soltera',
        nivelEducativo: 'Universitario',
        idEquipo: teamMap.get('Chirimoya') || null,
        email: 'ana.lazaro@upeu.edu.pe',
        activo: true,
      },
      {
        nombres: 'Eulalia',
        apellidoPaterno: 'Soncco',
        apellidoMaterno: 'Ortiz',
        documentoIdentidad: '22222222',
        celular: '982627095',
        fechaNacimiento: '1975-08-19',
        sexo: 'F',
        idArea: areaMap.get('Limpieza') || null,
        tallaPolo: 'M',
        estadoCivil: 'Casada',
        nivelEducativo: 'Secundaria',
        idEquipo: teamMap.get('Chirimoya') || null,
        email: 'eulalia.soncco@upeu.edu.pe',
        activo: true,
      },
      {
        nombres: 'Barbara',
        apellidoPaterno: 'Bernedo',
        apellidoMaterno: 'Benavente',
        documentoIdentidad: '33333333',
        celular: '945520053',
        fechaNacimiento: '1968-12-04',
        sexo: 'F',
        idArea: areaMap.get('Limpieza') || null,
        tallaPolo: 'M',
        estadoCivil: 'Viuda',
        nivelEducativo: 'Secundaria',
        idEquipo: teamMap.get('Chirimoya') || null,
        email: 'barbara.bernedo@upeu.edu.pe',
        activo: true,
      },
      {
        nombres: 'Freyde Job',
        apellidoPaterno: 'Mayhuiri',
        apellidoMaterno: 'Chincheromi',
        documentoIdentidad: '44444444',
        celular: '914420182',
        fechaNacimiento: '1985-02-09',
        sexo: 'M',
        idArea: areaMap.get('DTI-Redes') || null,
        tallaPolo: 'M',
        estadoCivil: 'Soltero',
        nivelEducativo: 'Universitario',
        idEquipo: teamMap.get('Nutri Bebes') || null,
        email: 'freyde.mayhuiri@upeu.edu.pe',
        activo: true,
      },
    ];

    const insertedPersonas = await db.insert(persona).values(personasToInsert).returning();
    insertedPersonas.forEach((p) => personaDocMap.set(p.documentoIdentidad, p));
    console.log(`[+] Creadas ${insertedPersonas.length} personas iniciales`);
  } else {
    existingPersonas.forEach((p) => personaDocMap.set(p.documentoIdentidad, p));
    console.log(`[~] Ya existen ${existingPersonas.length} personas`);
  }

  // 6. Usuarios y Asignación de Roles
  console.log('--- Verificando Usuario Administrador y Cuentas ---');
  const existingUsers = await db.select().from(usuario);

  if (existingUsers.length === 0) {
    const passwordHash = await bcrypt.hash('Temporal123!', 10);
    const now = new Date();

    const anaPersona = personaDocMap.get('11111111');
    if (anaPersona) {
      const [adminUser] = await db
        .insert(usuario)
        .values({
          email: 'ana.lazaro@upeu.edu.pe',
          username: 'ana.lazaro',
          passwordHash,
          activo: true,
          fechaCreacion: now,
          idPersonaSnapshot: anaPersona.idPersona,
          documentoIdentidadSnapshot: anaPersona.documentoIdentidad,
          nombresSnapshot: anaPersona.nombres,
          apellidoPaternoSnapshot: anaPersona.apellidoPaterno,
          apellidoMaternoSnapshot: anaPersona.apellidoMaterno || null,
          areaTrabajoSnapshot: 'CRAI',
          equipoSnapshot: 'Chirimoya',
          activoPersonaSnapshot: true,
          snapshotFecha: now,
        })
        .returning();

      const adminRolId = rolMap.get('Administrador');
      if (adminRolId) {
        await db.insert(usuarioRol).values({
          idUsuario: adminUser.idUsuario,
          idRol: adminRolId,
        });
      }
      console.log(`[+] Usuario Administrador creado: ana.lazaro@upeu.edu.pe (clave: Temporal123!)`);
    }
  } else {
    console.log(`[~] Ya existen ${existingUsers.length} usuarios`);
  }

  console.log('=== Seed inicial completado con éxito ===');
}
