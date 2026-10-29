import { Hono } from 'hono'
import { cors } from 'hono/cors'
import { jwtAuth } from './middleware/auth.js'
import catalogoRoutes from './routes/catalogo.js'

const app = new Hono()

// CORS – allow Angular dev & Cloudflare Pages
app.use('*', cors({
  origin: ['http://localhost:4200', /https:\/\/.*\.trycloudflare\.com$/],
  allowMethods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowHeaders: ['*'],
  exposeHeaders: ['X-Trace-ID'],
  credentials: true,
}))

// JWT guard – protects all routes under /api/v1 except login
app.use('/api/v1/*', async (c, next) => {
  // skip login route
  if (c.req.path === '/api/v1/auth/login') return await next()
  return await jwtAuth(c, next)
})

// Mount domain routes
app.route('/api/v1/catalogo', catalogoRoutes)

// Global error handling (simple JSON response)
app.onError((err, c) => {
  console.error('Unhandled error', err)
  const status = err.status || 500
  const body = { message: err.message || 'Internal Server Error' }
  return c.json(body, status)
})

export default app;
