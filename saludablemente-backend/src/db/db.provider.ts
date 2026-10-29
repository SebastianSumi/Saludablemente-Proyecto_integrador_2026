import { drizzle, PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';
import * as schema from './schema';

export const DRIZZLE = Symbol('DRIZZLE_CONNECTION');

export type DrizzleDb = PostgresJsDatabase<typeof schema>;

export const dbProvider = {
  provide: DRIZZLE,
  useFactory: (): DrizzleDb => {
    const connectionString = `postgres://${process.env.DB_USER || 'postgres'}:${encodeURIComponent(
      process.env.DB_PASSWORD || 'postgres'
    )}@${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || 5432}/${
      process.env.DB_NAME || 'saludablemente_db'
    }`;

    const client = postgres(connectionString, {
      max: 20,
      idle_timeout: 30,
      connect_timeout: 10,
    });

    return drizzle(client, { schema });
  },
};
