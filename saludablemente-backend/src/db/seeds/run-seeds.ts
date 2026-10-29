import * as dotenv from 'dotenv';
import postgres from 'postgres';
import { drizzle } from 'drizzle-orm/postgres-js';
import * as schema from '../schema';
import { seedDatabase } from './initial-seeds';

dotenv.config();

async function main() {
  const connectionString = `postgres://${process.env.DB_USER || 'postgres'}:${encodeURIComponent(
    process.env.DB_PASSWORD || 'postgres'
  )}@${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || 5432}/${
    process.env.DB_NAME || 'saludablemente_db'
  }`;

  console.log('Conectando a PostgreSQL:', connectionString.replace(/:[^:@]+@/, ':****@'));

  const client = postgres(connectionString, { max: 1 });
  const db = drizzle(client, { schema });

  try {
    await seedDatabase(db);
    console.log('Seed ejecutado satisfactoriamente.');
  } catch (err) {
    console.error('Error al ejecutar seed:', err);
    process.exit(1);
  } finally {
    await client.end();
  }
}

main();
