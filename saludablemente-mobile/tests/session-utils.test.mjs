import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import ts from 'typescript';

const source = fs.readFileSync(new URL('../src/app/core/auth/session-utils.ts', import.meta.url),'utf8');
const code = ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.ESNext,target:ts.ScriptTarget.ES2022}}).outputText;
const {safeReturnUrl,isApiRequest,decodeClaims} = await import('data:text/javascript;base64,'+Buffer.from(code).toString('base64'));

test('el retorno de login rechaza destinos externos y conserva rutas internas',()=>{
  for (const input of [undefined,'https://evil.example','//evil.example','/\\evil.example','javascript:alert(1)']) assert.equal(safeReturnUrl(input),'/');
  assert.equal(safeReturnUrl('/actividades/42?tab=detalle'),'/actividades/42?tab=detalle');
});
test('Bearer se limita al origen y ruta API configurados',()=>{
  const base='http://10.0.2.2:8080';
  assert.equal(isApiRequest(base+'/api/v1/personas',base),true);
  for (const target of ['http://10.0.2.2:9000/api/v1/a','https://evil.example/api/v1/a','http://10.0.2.2:8080.evil.example/api/v1/a',base+'/assets/logo.svg',base+'/api-maliciosa/']) assert.equal(isApiRequest(target,base),false);
  assert.equal(isApiRequest(base+'/api/v1/a',''),false);
});
test('el contexto de la API también limita rutas al agregar un prefijo',()=>{
  assert.equal(isApiRequest('https://example.org/erp/api/v1/a','https://example.org/erp'),true);
  assert.equal(isApiRequest('https://example.org/api/v1/a','https://example.org/erp'),false);
});
test('claims conservan Unicode y un token malformado no concede datos',()=>{
  const claims={name:'María Núñez',realm_access:{roles:['Colaborador']},exp:2000000000};
  const token='header.'+Buffer.from(JSON.stringify(claims)).toString('base64url')+'.signature';
  assert.deepEqual(decodeClaims(token),claims);
  for(const value of ['','invalid','h.not-json.s']) assert.deepEqual(decodeClaims(value),{});
});
