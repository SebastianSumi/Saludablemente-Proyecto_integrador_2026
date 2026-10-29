import { Injectable, Logger } from '@nestjs/common';
import { S3Client, PutObjectCommand, DeleteObjectCommand, HeadObjectCommand } from '@aws-sdk/client-s3';

@Injectable()
export class R2StorageService {
  private readonly logger = new Logger(R2StorageService.name);
  private s3Client: S3Client | null = null;
  private readonly bucketName: string;
  private readonly publicUrl: string;

  constructor() {
    const accountId = process.env.R2_ACCOUNT_ID;
    const accessKeyId = process.env.R2_ACCESS_KEY_ID;
    const secretAccessKey = process.env.R2_SECRET_ACCESS_KEY;
    this.bucketName = process.env.R2_BUCKET_NAME || 'saludablemente-assets';
    this.publicUrl = process.env.R2_PUBLIC_URL || '';

    if (accountId && accessKeyId && secretAccessKey) {
      this.s3Client = new S3Client({
        region: 'auto',
        endpoint: `https://${accountId}.r2.cloudflarestorage.com`,
        credentials: {
          accessKeyId,
          secretAccessKey,
        },
      });
      this.logger.log(`Cloudflare R2 inicializado con éxito. Bucket: ${this.bucketName}`);
    } else {
      this.logger.warn(
        'Cloudflare R2 no está configurado (faltan R2_ACCOUNT_ID, R2_ACCESS_KEY_ID o R2_SECRET_ACCESS_KEY). Subida de archivos deshabilitada temporalmente.'
      );
    }
  }

  public estaConfigurado(): boolean {
    return this.s3Client !== null;
  }

  /**
   * Sube un archivo a Cloudflare R2 y retorna su clave (key) y URL pública.
   */
  async subirArchivo(
    key: string,
    buffer: Buffer,
    contentType: string
  ): Promise<{ key: string; url: string }> {
    if (!this.s3Client) {
      throw new Error('Cloudflare R2 no está configurado. Configure las credenciales en .env.');
    }

    const command = new PutObjectCommand({
      Bucket: this.bucketName,
      Key: key,
      Body: buffer,
      ContentType: contentType,
    });

    await this.s3Client.send(command);

    return {
      key,
      url: this.obtenerUrlPublica(key),
    };
  }

  /**
   * Elimina un archivo de Cloudflare R2 por su clave.
   */
  async eliminarArchivo(key: string): Promise<void> {
    if (!this.s3Client) {
      this.logger.warn(`Intento de eliminar archivo '${key}' pero R2 no está configurado.`);
      return;
    }

    const command = new DeleteObjectCommand({
      Bucket: this.bucketName,
      Key: key,
    });

    await this.s3Client.send(command);
  }

  /**
   * Obtiene la URL pública directa para un asset en R2.
   */
  obtenerUrlPublica(key: string): string {
    if (!this.publicUrl) {
      return `/api/v1/storage/${key}`;
    }
    const base = this.publicUrl.endsWith('/') ? this.publicUrl.slice(0, -1) : this.publicUrl;
    const cleanKey = key.startsWith('/') ? key.slice(1) : key;
    return `${base}/${cleanKey}`;
  }
}
