import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import ts from 'typescript';
import { of, tap } from 'rxjs';
const source = fs.readFileSync(new URL('../src/app/core/auth/auth-service.ts', import.meta.url), 'utf8').replace(/^import .*;\r?\n/gm, '');
const code = ts.transpileModule(source, {compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022,experimentalDecorators:true}}).outputText;
function instance(response, stored) {
  const data = new Map(stored ? [['slb.auth.session', JSON.stringify(stored)]] : []);
  const storage = {getItem:k=>data.get(k),setItem:(k,v)=>data.set(k,v),removeItem:k=>data.delete(k)};
  const calls=[];
  const http={post:(url,body)=>{calls.push({url,body});return of(response)}};
  const api={buildUrl:p=>'http://localhost:8080'+p};
  const router={navigate:()=>{}};
  const signal=v=>Object.assign(()=>v,{set:n=>{v=n}});
  const factory=new Function('exports','inject','signal','Injectable','tap','localStorage','normalizarRoles','tieneAlgunRol','clearExports', 'const HttpClient={}, ApiService={}, Router={}; const ports=[HttpClient,ApiService,Router]; const deps=inject; inject=t=>deps(ports.indexOf(t)); '+code+';return exports.AuthService;');
  const Class=factory({},i=>[http,api,router][i],signal,()=>x=>x,tap,storage,x=>x,(a,b)=>b.some(r=>a.includes(r)),async()=>{});
  return {auth:new Class(),calls,data};
}
const session={token:'backend-jwt',email:'test@example.com',nombre:'Ana',roles:['Administrador'],expiraEn:Math.floor(Date.now()/1000)+300};
test('login usa el contrato JWT propio y conserva sesión y roles',()=>{
  const {auth,calls,data}=instance(session);
  const credentials={identificador:'ana',password:'test-password'};
  auth.login(credentials).subscribe();
  assert.deepEqual(calls,[{url:'http://localhost:8080/api/v1/auth/login',body:credentials}]);
  assert.equal(auth.token(),session.token);
  assert.equal(auth.tieneRol('Administrador'),true);
  assert.equal(JSON.parse(data.get('slb.auth.session')).token,session.token);
});
test('restaura sesión vigente y descarta JWT vencido',()=>{
  assert.equal(instance(session,session).auth.token(),session.token);
  const {auth,data}=instance(session,{...session,expiraEn:1});
  assert.equal(auth.token(),null);
  assert.equal(data.size,0);
});
test('logout llama al backend y elimina la sesión local',()=>{
  const {auth,calls,data}=instance(session,session);
  auth.cerrarSesion();
  assert.equal(calls[0].url,'http://localhost:8080/api/v1/auth/logout');
  assert.equal(auth.token(),null);
  assert.equal(data.size,0);
});
