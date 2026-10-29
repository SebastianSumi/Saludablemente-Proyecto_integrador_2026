export interface VisorImagenItem {
  src: string;
  titulo?: string;
  subtitulo?: string;
}

export interface VisorImagenDatos {
  imagenes: VisorImagenItem[];
  indice?: number;
}
