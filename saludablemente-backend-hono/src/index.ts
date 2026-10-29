import { Hono } from "hono";
import { verifyJwt } from "../auth";
import { catalogoRouter } from "../modules/catalogo/router";

const app = new Hono();

// CORS middleware (allow Angular origin and local dev)
app.use("*", async (c, next) => {
  c.header("Access-Control-Allow-Origin", "*");
  c.header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
  c.header("Access-Control-Allow-Headers", "*");
  await next();
});

// Public health endpoint
app.get("/", (c) => c.text("Saludablemente Backend (Hono)"));

// JWT protected middleware for /api/v1/*
app.use("/api/v1/*", async (c, next) => {
  const auth = c.req.header("Authorization");
  if (!auth || !auth.startsWith("Bearer ")) {
    return c.json({ message: "Missing Authorization" }, 401);
  }
  const token = auth.substring(7);
  try {
    const payload = await verifyJwt(token);
    // expose payload to downstream handlers
    c.set("user", payload);
    await next();
  } catch (e) {
    return c.json({ message: "Invalid token" }, 401);
  }
});

// Mount domain routers under /api/v1
app.route("/api/v1/catalogo", catalogoRouter);

export default {
  async fetch(request: Request, env: Record<string, any>, ctx: ExecutionContext) {
    // Hono can work directly with request; env is accessible via globals if needed
    return app.fetch(request, env, ctx);
  },
};