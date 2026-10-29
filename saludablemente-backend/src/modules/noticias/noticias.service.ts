import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { count, eq } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import { categoria, noticia, noticiaTeam } from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';
import { R2StorageService } from '../../storage/r2-storage.service';

@Injectable()
export class NoticiasService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly r2Storage: R2StorageService
  ) {}

  // ===================== FEED & NOTICIAS PÚBLICAS =====================

  async feed(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(noticia)
      .where(eq(noticia.activo, true));
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idNoticia: noticia.idNoticia,
        titulo: noticia.titulo,
        resumen: noticia.resumen,
        contenido: noticia.contenido,
        imagenPortada: noticia.imagenPortada,
        fechaPublicacion: noticia.fechaPublicacion,
        esOficial: noticia.esOficial,
        categoria: {
          idCategoria: categoria.idCategoria,
          nombre: categoria.nombre,
        },
      })
      .from(noticia)
      .leftJoin(categoria, eq(noticia.idCategoria, categoria.idCategoria))
      .where(eq(noticia.activo, true))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async obtenerNoticiaPorId(id: number) {
    const [n] = await this.db
      .select()
      .from(noticia)
      .where(eq(noticia.idNoticia, id))
      .limit(1);

    if (!n) throw new NotFoundException('Noticia no encontrada');
    return n;
  }

  // ===================== ADMIN NOTICIAS =====================

  async listarAdminNoticias(page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db.select({ total: count() }).from(noticia);
    const total = Number(totalRes?.total || 0);

    const items = await this.db.select().from(noticia).limit(safeSize).offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async crearNoticia(data: typeof noticia.$inferInsert) {
    const [nueva] = await this.db.insert(noticia).values(data).returning();
    return nueva;
  }

  async actualizarNoticia(id: number, data: Partial<typeof noticia.$inferInsert>) {
    await this.obtenerNoticiaPorId(id);
    const [act] = await this.db
      .update(noticia)
      .set(data)
      .where(eq(noticia.idNoticia, id))
      .returning();
    return act;
  }

  async publicarNoticia(id: number) {
    return this.actualizarNoticia(id, { activo: true });
  }

  async despublicarNoticia(id: number) {
    return this.actualizarNoticia(id, { activo: false });
  }

  async subirImagenNoticia(id: number, buffer: Buffer, mime: string) {
    await this.obtenerNoticiaPorId(id);
    const ext = mime.split('/')[1] || 'jpg';
    const key = `noticias/${id}/portada.${ext}`;

    const { url } = await this.r2Storage.subirArchivo(key, buffer, mime);

    await this.db
      .update(noticia)
      .set({ imagenPortada: url })
      .where(eq(noticia.idNoticia, id));

    return { imagenPortada: url };
  }

  // ===================== CATEGORÍAS =====================

  async listarCategorias() {
    return this.db.select().from(categoria);
  }

  async crearCategoria(nombre: string, descripcion?: string) {
    const [nueva] = await this.db
      .insert(categoria)
      .values({ nombre, descripcion })
      .returning();
    return nueva;
  }

  async actualizarCategoria(id: number, data: { nombre: string; descripcion?: string }) {
    const [act] = await this.db
      .update(categoria)
      .set(data)
      .where(eq(categoria.idCategoria, id))
      .returning();
    return act;
  }

  async eliminarCategoria(id: number) {
    await this.db.delete(categoria).where(eq(categoria.idCategoria, id));
    return { mensaje: 'Categoría eliminada' };
  }
}
