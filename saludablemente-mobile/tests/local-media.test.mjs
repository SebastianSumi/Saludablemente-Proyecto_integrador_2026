import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import ts from 'typescript';
const source=fs.readFileSync(new URL('../src/app/core/interceptors/local-media.ts',import.meta.url),'utf8');
const code=ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.ESNext,target:ts.ScriptTarget.ES2022}}).outputText;
const {localMedia}=await import('data:text/javascript;base64,'+Buffer.from(code).toString('base64'));
test('traduce únicamente medios MinIO locales, conservando rutas y query',()=>{
  const input={fotoUrl:'http://localhost:9000/bucket/imagen.png?token=x',items:[{imagenUrl:'http://127.0.0.1:9000/logo.jpg'}]};
  const output=localMedia(input,'http://10.0.2.2:9000');
  assert.equal(output.fotoUrl,'http://10.0.2.2:9000/bucket/imagen.png?token=x');
  assert.equal(output.items[0].imagenUrl,'http://10.0.2.2:9000/logo.jpg');
  assert.equal(input.fotoUrl,'http://localhost:9000/bucket/imagen.png?token=x');
});
test('no reescribe texto, orígenes ajenos ni binarios',()=>{
  const input={resumen:'http://localhost:9000/texto',fotoUrl:'https://externo.example/image.jpg',url:'http://localhost:8080/api/v1/a'};
  assert.deepEqual(localMedia(input,'http://10.0.2.2:9000'),input);
  const blob=new Blob(['pdf']);assert.equal(localMedia(blob,'http://10.0.2.2:9000'),blob);
});
