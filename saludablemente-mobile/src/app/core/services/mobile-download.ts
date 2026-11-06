import { Capacitor } from '@capacitor/core';
import { Directory, Filesystem } from '@capacitor/filesystem';
import { Share } from '@capacitor/share';

const EXPORTS = 'mobile-exports';
export async function downloadBlob(blob: Blob, filename: string): Promise<void> {
  if (!Capacitor.isNativePlatform()) {
    const url = URL.createObjectURL(blob), anchor = document.createElement('a');
    anchor.href = url; anchor.download = filename; anchor.click();
    setTimeout(() => URL.revokeObjectURL(url), 4000);
    return;
  }
  const safe = filename.replace(/[^\w.\-áéíóúñ]/gi, '_').slice(-120) || 'documento';
  const data = await new Promise<string>((resolve,reject) => {
    const reader=new FileReader();
    reader.onerror=()=>reject(new Error('No se pudo leer el archivo.'));
    reader.onload=()=>resolve(String(reader.result).split(',')[1]);
    reader.readAsDataURL(blob);
  });
  const path=`${EXPORTS}/${crypto.randomUUID()}/${safe}`;
  const file=await Filesystem.writeFile({path,directory:Directory.Cache,data,recursive:true});
  try { await Share.share({title:filename,files:[file.uri],dialogTitle:'Abrir o guardar documento'}); }
  catch (error) { if (!(error instanceof Error) || error.message !== 'Share canceled') throw error; }
}

/** Solo los archivos temporales creados por esta app; se limpian al salir de la sesión. */
export async function clearExports(): Promise<void> {
  if (!Capacitor.isNativePlatform()) return;
  await Filesystem.rmdir({path:EXPORTS,directory:Directory.Cache,recursive:true}).catch(()=>undefined);
}
