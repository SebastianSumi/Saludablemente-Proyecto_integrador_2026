import path from 'node:path';
import {spawnSync} from 'node:child_process';
import {findAndroidSdk} from './android-sdk.mjs';
const executable=process.platform==='win32'?'adb.exe':'adb';
const sdk=findAndroidSdk([`platform-tools/${executable}`]);
if(!sdk) throw new Error('No se encontró ADB. Configura ANDROID_HOME con tu SDK.');
const adb=path.join(sdk,'platform-tools',executable);
const list=spawnSync(adb,['devices'],{encoding:'utf8'});
if(list.status!==0) throw new Error('ADB no respondió. Comprueba Android Studio.');
const devices=list.stdout.split(/\r?\n/).map(line=>line.trim().split(/\s+/)).filter(parts=>parts[1]==='device').map(parts=>parts[0]);
const serial=process.argv[2] || (devices.length===1?devices[0]:undefined);
if(!serial || !devices.includes(serial)) throw new Error('Inicia un único emulador o indica su serial: npm run android:connect -- emulator-5554');
const result=spawnSync(adb,['-s',serial,'reverse','tcp:8080','tcp:8080'],{stdio:'inherit'});
if(result.status!==0) process.exit(result.status ?? 1);
console.log('Backend local conectado: localhost:8080 del dispositivo al puerto 8080 de la PC.');

