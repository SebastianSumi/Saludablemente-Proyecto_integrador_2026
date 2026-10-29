import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { and, count, desc, eq, sql } from 'drizzle-orm';
import { DRIZZLE, DrizzleDb } from '../../db/db.provider';
import {
  comentario,
  comentarioAdjunto,
  comentarioHashtag,
  mencion,
  post,
  postFijado,
  postGuardado,
  postHashtag,
  postImagen,
  postReporte,
  postVista,
  reaccionComentario,
  reaccionPost,
  usuario,
} from '../../db/schema';
import { buildSpringPage } from '../../common/dto/page.dto';
import { R2StorageService } from '../../storage/r2-storage.service';

@Injectable()
export class SocialService {
  constructor(
    @Inject(DRIZZLE) private readonly db: DrizzleDb,
    private readonly r2Storage: R2StorageService
  ) {}

  // ===================== MURO Y CONSULTAS =====================

  async muro(tab: string = 'PARA_TI', page: number = 0, size: number = 20, idUsuario?: number) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(post)
      .where(eq(post.activo, true));
    const total = Number(totalRes?.total || 0);

    const posts = await this.db
      .select()
      .from(post)
      .where(eq(post.activo, true))
      .orderBy(desc(post.fechaPublicacion))
      .limit(safeSize)
      .offset(offset);

    const postsConDetalles = await Promise.all(
      posts.map(async (p) => this.enriquecerPost(p, idUsuario))
    );

    return {
      pagina: buildSpringPage(postsConDetalles, total, safePage, safeSize),
      fijados: [],
    };
  }

  async muroEquipo(idEquipo: number, page: number = 0, size: number = 20, idUsuario?: number) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(post)
      .where(and(eq(post.idEquipoAutor, idEquipo), eq(post.activo, true)));
    const total = Number(totalRes?.total || 0);

    const posts = await this.db
      .select()
      .from(post)
      .where(and(eq(post.idEquipoAutor, idEquipo), eq(post.activo, true)))
      .orderBy(desc(post.fechaPublicacion))
      .limit(safeSize)
      .offset(offset);

    const postsConDetalles = await Promise.all(
      posts.map(async (p) => this.enriquecerPost(p, idUsuario))
    );

    return {
      pagina: buildSpringPage(postsConDetalles, total, safePage, safeSize),
      fijados: [],
    };
  }

  // ===================== DETALLE DE POST =====================

  async obtenerPostDetalle(idPost: number, idUsuario?: number) {
    const [p] = await this.db
      .select()
      .from(post)
      .where(eq(post.idPost, idPost))
      .limit(1);

    if (!p) throw new NotFoundException('Post no encontrado');

    return this.enriquecerPost(p, idUsuario);
  }

  private async enriquecerPost(p: typeof post.$inferSelect, idUsuario?: number) {
    // 1. Autor
    const [autor] = await this.db
      .select({
        id: usuario.idUsuario,
        email: usuario.email,
        username: usuario.username,
        nombres: usuario.nombresSnapshot,
        apellidoPaterno: usuario.apellidoPaternoSnapshot,
      })
      .from(usuario)
      .where(eq(usuario.idUsuario, p.autorId))
      .limit(1);

    // 2. Imágenes
    const imagenes = await this.db
      .select()
      .from(postImagen)
      .where(eq(postImagen.idPost, p.idPost));

    // 3. Hashtags
    const hashtags = await this.db
      .select({ tag: postHashtag.tag })
      .from(postHashtag)
      .where(eq(postHashtag.idPost, p.idPost));

    // 4. Conteo de reacciones
    const reacciones = await this.db
      .select({
        tipo: reaccionPost.tipo,
        total: count(),
      })
      .from(reaccionPost)
      .where(eq(reaccionPost.idPost, p.idPost))
      .groupBy(reaccionPost.tipo);

    // 5. Conteo de comentarios
    const [totalComentarios] = await this.db
      .select({ total: count() })
      .from(comentario)
      .where(and(eq(comentario.idPost, p.idPost), eq(comentario.activo, true)));

    // 6. Mi reacción y guardado
    let miReaccion = null;
    let guardado = false;

    if (idUsuario) {
      const [reac] = await this.db
        .select({ tipo: reaccionPost.tipo })
        .from(reaccionPost)
        .where(and(eq(reaccionPost.idPost, p.idPost), eq(reaccionPost.idUsuario, idUsuario)))
        .limit(1);
      miReaccion = reac?.tipo || null;

      const [guard] = await this.db
        .select()
        .from(postGuardado)
        .where(and(eq(postGuardado.idPost, p.idPost), eq(postGuardado.idUsuario, idUsuario)))
        .limit(1);
      guardado = !!guard;
    }

    return {
      ...p,
      autor: autor
        ? {
            idUsuario: autor.id,
            nombreCompleto: `${autor.nombres} ${autor.apellidoPaterno}`,
            username: autor.username,
          }
        : null,
      imagenes: imagenes.map((img) => img.url),
      hashtags: hashtags.map((h) => h.tag),
      reacciones,
      totalComentarios: Number(totalComentarios?.total || 0),
      miReaccion,
      guardado,
    };
  }

  async registrarVista(idPost: number, idUsuario: number) {
    const [vista] = await this.db
      .select()
      .from(postVista)
      .where(and(eq(postVista.idPost, idPost), eq(postVista.idUsuario, idUsuario)))
      .limit(1);

    if (vista) {
      await this.db
        .update(postVista)
        .set({ fechaUltimaVista: new Date() })
        .where(eq(postVista.idPostVista, vista.idPostVista));
    } else {
      await this.db.insert(postVista).values({
        idPost,
        idUsuario,
        fechaPrimeraVista: new Date(),
        fechaUltimaVista: new Date(),
      });
      await this.db
        .update(post)
        .set({ alcanceTotal: sql`${post.alcanceTotal} + 1` })
        .where(eq(post.idPost, idPost));
    }
  }

  // ===================== CREAR / EDITAR / BORRAR POST =====================

  async crearPost(
    data: {
      titulo: string;
      resumen?: string;
      tipoContenido?: string;
      idEquipoAutor?: number;
      refId?: number;
      imagenes?: string[];
      hashtags?: string[];
    },
    idUsuario: number
  ) {
    const [nuevo] = await this.db
      .insert(post)
      .values({
        titulo: data.titulo,
        resumen: data.resumen,
        tipoContenido: data.tipoContenido || 'MANUAL',
        idEquipoAutor: data.idEquipoAutor,
        refId: data.refId,
        autorId: idUsuario,
        fechaPublicacion: new Date(),
        activo: true,
      })
      .returning();

    if (data.imagenes && data.imagenes.length > 0) {
      for (let i = 0; i < data.imagenes.length; i++) {
        await this.db.insert(postImagen).values({
          idPost: nuevo.idPost,
          url: data.imagenes[i],
          orden: i + 1,
        });
      }
    }

    if (data.hashtags && data.hashtags.length > 0) {
      for (const tag of data.hashtags) {
        await this.db.insert(postHashtag).values({
          idPost: nuevo.idPost,
          tag: tag.toLowerCase(),
        });
      }
    }

    return this.obtenerPostDetalle(nuevo.idPost, idUsuario);
  }

  async editarPost(idPost: number, data: { titulo?: string; resumen?: string }, idUsuario: number) {
    const [p] = await this.db
      .select()
      .from(post)
      .where(eq(post.idPost, idPost))
      .limit(1);

    if (!p) throw new NotFoundException('Post no encontrado');
    if (p.autorId !== idUsuario) throw new ForbiddenException('No puedes editar este post');

    await this.db
      .update(post)
      .set({
        ...data,
        fechaActualizacion: new Date(),
      })
      .where(eq(post.idPost, idPost));

    return this.obtenerPostDetalle(idPost, idUsuario);
  }

  async eliminarPost(idPost: number, idUsuario: number, esAdmin: boolean) {
    const [p] = await this.db
      .select()
      .from(post)
      .where(eq(post.idPost, idPost))
      .limit(1);

    if (!p) throw new NotFoundException('Post no encontrado');
    if (p.autorId !== idUsuario && !esAdmin) {
      throw new ForbiddenException('No tienes permiso para eliminar este post');
    }

    await this.db.update(post).set({ activo: false }).where(eq(post.idPost, idPost));
    return { mensaje: 'Post eliminado' };
  }

  // ===================== GUARDADOS Y COMPARTIR =====================

  async guardarPost(idPost: number, idUsuario: number, guardar: boolean) {
    if (guardar) {
      await this.db
        .insert(postGuardado)
        .values({ idPost, idUsuario, fechaGuardado: new Date() })
        .onConflictDoNothing();
    } else {
      await this.db
        .delete(postGuardado)
        .where(and(eq(postGuardado.idPost, idPost), eq(postGuardado.idUsuario, idUsuario)));
    }
  }

  async compartirPost(idPost: number, idUsuario: number) {
    const original = await this.obtenerPostDetalle(idPost, idUsuario);

    const [share] = await this.db
      .insert(post)
      .values({
        titulo: `Compartido: ${original.titulo}`,
        tipoContenido: 'SHARE',
        origenPostId: idPost,
        autorId: idUsuario,
        fechaPublicacion: new Date(),
        activo: true,
      })
      .returning();

    await this.db
      .update(post)
      .set({ sharesTotal: sql`${post.sharesTotal} + 1` })
      .where(eq(post.idPost, idPost));

    return share;
  }

  // ===================== COMENTARIOS =====================

  async listarComentarios(idPost: number, page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(comentario)
      .where(and(eq(comentario.idPost, idPost), eq(comentario.activo, true)));
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select({
        idComentario: comentario.idComentario,
        contenido: comentario.contenido,
        editado: comentario.editado,
        fechaComentario: comentario.fechaComentario,
        idComentarioPadre: comentario.idComentarioPadre,
        autor: {
          idUsuario: usuario.idUsuario,
          nombres: usuario.nombresSnapshot,
          apellidoPaterno: usuario.apellidoPaternoSnapshot,
          username: usuario.username,
        },
      })
      .from(comentario)
      .innerJoin(usuario, eq(comentario.idUsuarioAutor, usuario.idUsuario))
      .where(and(eq(comentario.idPost, idPost), eq(comentario.activo, true)))
      .orderBy(comentario.fechaComentario)
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }

  async crearComentario(
    idPost: number,
    contenido: string,
    idUsuario: number,
    idComentarioPadre?: number
  ) {
    const [nuevo] = await this.db
      .insert(comentario)
      .values({
        idPost,
        contenido,
        idUsuarioAutor: idUsuario,
        idComentarioPadre,
        fechaComentario: new Date(),
        activo: true,
        editado: false,
      })
      .returning();

    return nuevo;
  }

  async editarComentario(idComentario: number, contenido: string, idUsuario: number) {
    const [c] = await this.db
      .select()
      .from(comentario)
      .where(eq(comentario.idComentario, idComentario))
      .limit(1);

    if (!c) throw new NotFoundException('Comentario no encontrado');
    if (c.idUsuarioAutor !== idUsuario) throw new ForbiddenException('No puedes editar este comentario');

    const [act] = await this.db
      .update(comentario)
      .set({
        contenido,
        editado: true,
        fechaEdicion: new Date(),
      })
      .where(eq(comentario.idComentario, idComentario))
      .returning();

    return act;
  }

  async eliminarComentario(idComentario: number, idUsuario: number, esAdmin: boolean) {
    const [c] = await this.db
      .select()
      .from(comentario)
      .where(eq(comentario.idComentario, idComentario))
      .limit(1);

    if (!c) throw new NotFoundException('Comentario no encontrado');
    if (c.idUsuarioAutor !== idUsuario && !esAdmin) {
      throw new ForbiddenException('No tienes permiso para borrar este comentario');
    }

    await this.db
      .update(comentario)
      .set({ activo: false })
      .where(eq(comentario.idComentario, idComentario));

    return { mensaje: 'Comentario eliminado' };
  }

  // ===================== REACCIONES =====================

  async reaccionarPost(idPost: number, tipo: string, idUsuario: number) {
    // Si ya existe la misma reacción, no hace nada; si es diferente, actualiza
    const [existente] = await this.db
      .select()
      .from(reaccionPost)
      .where(and(eq(reaccionPost.idPost, idPost), eq(reaccionPost.idUsuario, idUsuario)))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(reaccionPost)
        .set({ tipo, fechaReaccion: new Date() })
        .where(and(eq(reaccionPost.idPost, idPost), eq(reaccionPost.idUsuario, idUsuario)))
        .returning();
      return act;
    } else {
      const [nueva] = await this.db
        .insert(reaccionPost)
        .values({ idPost, idUsuario, tipo, fechaReaccion: new Date() })
        .returning();
      return nueva;
    }
  }

  async quitarReaccionPost(idPost: number, idUsuario: number) {
    await this.db
      .delete(reaccionPost)
      .where(and(eq(reaccionPost.idPost, idPost), eq(reaccionPost.idUsuario, idUsuario)));
    return { mensaje: 'Reacción eliminada' };
  }

  async reaccionarComentario(idComentario: number, tipo: string, idUsuario: number) {
    const [existente] = await this.db
      .select()
      .from(reaccionComentario)
      .where(and(eq(reaccionComentario.idComentario, idComentario), eq(reaccionComentario.idUsuario, idUsuario)))
      .limit(1);

    if (existente) {
      const [act] = await this.db
        .update(reaccionComentario)
        .set({ tipo, fechaReaccion: new Date() })
        .where(and(eq(reaccionComentario.idComentario, idComentario), eq(reaccionComentario.idUsuario, idUsuario)))
        .returning();
      return act;
    } else {
      const [nueva] = await this.db
        .insert(reaccionComentario)
        .values({ idComentario, idUsuario, tipo, fechaReaccion: new Date() })
        .returning();
      return nueva;
    }
  }

  async quitarReaccionComentario(idComentario: number, idUsuario: number) {
    await this.db
      .delete(reaccionComentario)
      .where(and(eq(reaccionComentario.idComentario, idComentario), eq(reaccionComentario.idUsuario, idUsuario)));
    return { mensaje: 'Reacción eliminada' };
  }

  // ===================== REPORTES Y MODERACIÓN =====================

  async reportar(data: {
    idPost?: number;
    idComentario?: number;
    motivo: string;
    detalle?: string;
    idUsuario: number;
  }) {
    const [reporte] = await this.db
      .insert(postReporte)
      .values({
        idPost: data.idPost,
        idComentario: data.idComentario,
        idUsuarioReporta: data.idUsuario,
        motivo: data.motivo,
        detalle: data.detalle,
        estado: 'PENDIENTE',
        fechaReporte: new Date(),
      })
      .returning();

    return reporte;
  }

  async listarReportesAdmin(estado: string = 'PENDIENTE', page: number = 0, size: number = 20) {
    const safePage = Math.max(0, page);
    const safeSize = Math.max(1, size);
    const offset = safePage * safeSize;

    const [totalRes] = await this.db
      .select({ total: count() })
      .from(postReporte)
      .where(eq(postReporte.estado, estado));
    const total = Number(totalRes?.total || 0);

    const items = await this.db
      .select()
      .from(postReporte)
      .where(eq(postReporte.estado, estado))
      .orderBy(desc(postReporte.fechaReporte))
      .limit(safeSize)
      .offset(offset);

    return buildSpringPage(items, total, safePage, safeSize);
  }
}
