import { Hono } from "hono";
import { jwtVerify } from "jose";

/**
 * Types for the Cloudflare Worker environment.
 * - `JWT_SECRET`   : secret used to sign/verify JWTs (HS256).
 * - `NEON`         : Hyperdrive client provided by Cloudflare binding.
 * - `R2_BUCKET`    : R2 bucket binding for file storage.
 */
export interface Env {
  JWT_SECRET: string;
  NEON: any; // Hyperdrive client (typed loosely, works via .prepare(...).all())
  R2_BUCKET: R2Bucket;
}

const app = new Hono<Env>();

// ---------------------------------------------------------------------
// Global Middlewares
// ---------------------------------------------------------------------
app.use("*", async (c, next) => {
  // CORS – allow the Angular Pages origin (adjust as needed)
  const origin = c.req.headers.get("origin") ?? "*";
  c.res.headers.set("Access-Control-Allow-Origin", origin);
  c.res.headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
  c.res.headers.set("Access-Control-Allow-Headers", "*",
    "Access-Control-Allow-Credentials", "true");
  if (c.req.method === "OPTIONS") {
    return c.text("", 204);
  }
  await next();
});

// ---------------------------------------------------------------------
// Helper – JWT auth middleware (HS256)
// ---------------------------------------------------------------------
const auth = async (c: any, next: any) => {
  const authHeader = c.req.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) {
    return c.text("Unauthorized", 401);
  }
  const token = authHeader.substring(7);
  try {
    const { payload } = await jwtVerify(
      token,
      new TextEncoder().encode(c.env.JWT_SECRET)
    );
    c.set("user", payload);
    await next();
  } catch {
    return c.text("Invalid token", 401);
  }
};

// ---------------------------------------------------------------------
// Public route – health check
// ---------------------------------------------------------------------
app.get("/api/v1/ping", (c) => c.text("pong"));

// ---------------------------------------------------------------------
// Example domain route – Areas (read‑only)
// ---------------------------------------------------------------------
app.get("/api/v1/areas", async (c) => {
  // Simple example query against the Neon Hyperdrive DB.
  // Adjust the table/columns to match the original Oracle schema.
  const sql = `SELECT id AS idArea, nombre, descripcion FROM slb_personal LIMIT 10`;
  const result = await c.env.NEON.prepare(sql).all();
  return c.json(result);
});

// ---------------------------------------------------------------------
// Protected example route – demonstrates auth usage
// ---------------------------------------------------------------------
app.get("/api/v1/secure", auth, (c) => {
  const user = c.get("user");
  return c.json({ message: "Secure endpoint", user });
});

export default {
  fetch: app.fetch,
};
