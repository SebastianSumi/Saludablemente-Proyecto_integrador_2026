package upeu.edu.pe.Saludabemente.shared.storage.social;

/** Clave interna separada de URL pública canónica, MIME y tamaño en bytes. */
public record AdjuntoSocialDto(String objectKey, String url, String mime, long sizeBytes) {}
