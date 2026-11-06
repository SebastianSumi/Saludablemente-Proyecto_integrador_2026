import fs from 'node:fs';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { findAndroidSdk } from './android-sdk.mjs';

const candidates=[process.env.JAVA_HOME];
for(const base of ['C:/Program Files/Java','C:/Program Files/Eclipse Adoptium']) {
  if(fs.existsSync(base)) for(const name of fs.readdirSync(base)) if(/jdk.?21/i.test(name)) candidates.push(path.join(base,name));
}
const javaHome=candidates.filter(Boolean).find(dir=>{
  const java=path.join(dir,'bin',process.platform==='win32'?'java.exe':'java');
  const check=spawnSync(java,['-version'],{encoding:'utf8'});
  return check.status===0 && /version "21\./.test(check.stderr);
});
if(!javaHome) throw new Error('Configura JAVA_HOME con JDK 21 antes de compilar Android.');
const androidHome=findAndroidSdk(['platforms/android-36', 'build-tools/35.0.0', 'licenses']);
// Gradle prioriza local.properties: mantenerlo alineado con el SDK validado.
const propertiesPath = path.resolve('android/local.properties');
const properties = fs.existsSync(propertiesPath) ? fs.readFileSync(propertiesPath, 'utf8') : '';
const sdkLine = `sdk.dir=${androidHome.replaceAll('\\', '/')}`;
fs.writeFileSync(propertiesPath, /^sdk\.dir\s*=/m.test(properties)
  ? properties.replace(/^sdk\.dir\s*=.*$/m, sdkLine) : `${properties}\n${sdkLine}\n`);
console.log(`JDK: ${javaHome}\nAndroid SDK: ${androidHome}`);
const env={...process.env,JAVA_HOME:javaHome,ANDROID_HOME:androidHome,ANDROID_SDK_ROOT:androidHome,GRADLE_USER_HOME:path.resolve('.gradle-cache')};
const result=process.platform==='win32'
  ? spawnSync('cmd.exe',['/d','/c','gradlew.bat assembleDebug --console=plain'],{cwd:'android',env,stdio:'inherit'})
  : spawnSync('./gradlew',['assembleDebug','--console=plain'],{cwd:'android',env,stdio:'inherit'});
process.exit(result.status ?? 1);
