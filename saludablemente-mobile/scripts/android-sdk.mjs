import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';

export function findAndroidSdk(requiredPaths) {
  const properties = new URL('../android/local.properties', import.meta.url);
  const local = fs.existsSync(properties)
    ? fs.readFileSync(properties, 'utf8').match(/^sdk\.dir\s*=\s*(.+)$/m)?.[1].trim().replace(/\\(.)/g, '$1')
    : undefined;
  const candidates = [process.env.ANDROID_HOME, process.env.ANDROID_SDK_ROOT, local,
    process.env.LOCALAPPDATA && path.join(process.env.LOCALAPPDATA, 'Android/Sdk'),
    path.join(os.homedir(), 'AppData/Local/Android/Sdk'),
    path.join(os.homedir(), 'Library/Android/sdk'), path.join(os.homedir(), 'Android/Sdk')];
  const sdk = candidates.filter(Boolean).find(dir => requiredPaths.every(item => fs.existsSync(path.join(dir, item))));
  if (!sdk) throw new Error(`No se encontró un SDK Android con: ${requiredPaths.join(', ')}. Configura ANDROID_HOME o android/local.properties.`);
  return sdk;
}
