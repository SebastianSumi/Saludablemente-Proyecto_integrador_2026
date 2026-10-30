/** Entrada exclusiva de revisión visual. No se importa desde main.ts ni desde rutas productivas. */
import { Component, provideZonelessChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, RouterOutlet } from '@angular/router';
import { HttpErrorResponse, HttpResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { provideTaiga, TuiRoot } from '@taiga-ui/core';
import { provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { AuthService } from '../../../core/auth/auth-service';
import { SOCIAL_ROUTES } from '../social.routes';
import { BACKEND } from './backend-fixtures';

echarts.use([BarChart,GridComponent,LegendComponent,TooltipComponent,CanvasRenderer]);
const fixtures=JSON.parse(JSON.stringify(BACKEND).replaceAll('https://example.invalid/equipo.png','/img/logo-icon.svg')) as typeof BACKEND;
@Component({selector:'app-root',imports:[TuiRoot,RouterOutlet],template:`<tui-root><div style="padding:8px 16px;background:var(--sal-hover);color:var(--sal-ink-soft);text-align:center;font-size:12px">Vista aislada de prueba · Fixtures de contratos · Sin conexión al backend</div><router-outlet/></tui-root>`})
class Preview {}
bootstrapApplication(Preview,{providers:[provideZonelessChangeDetection(),provideTaiga(),provideEchartsCore({echarts}),provideRouter([{path:'social',children:SOCIAL_ROUTES},{path:'',pathMatch:'full',redirectTo:'social'}]),{provide:AuthService,useValue:{tieneRol:()=>true,tieneAlgunRol:()=>true}},provideHttpClient(withInterceptors([(request,next)=>{
  const path=new URL(request.url,location.origin).pathname;
  if(!path.includes('/api/v1/'))return next(request);
  let body:unknown;
  if(path.endsWith('/muro')||/\/muro\/(equipos|actividades)\/\d+$/.test(path))body=fixtures.muro;
  else if(path.endsWith('/hashtags/tendencia'))body=fixtures.metricas.hashtagsTendencia;
  else if(/\/equipos\/\d+\/perfil$/.test(path))body=fixtures.muro.pinned[0].equipo;
  else if(path.endsWith('/equipos/mi-equipo'))body={id:301,nombre:'Bienestar',activo:true};
  else if(/\/posts\/\d+\/comentarios\/\d+$/.test(path))body=fixtures.comentarios.content[0];
  else if(/\/posts\/\d+\/comentarios$/.test(path))body=fixtures.comentarios;
  else if(/\/posts\/\d+$/.test(path))body=fixtures.detalleAutor;
  else if(path.endsWith('/mi-contenido'))body=fixtures.guardados;
  else if(/\/usuarios\/\d+\/actividad$/.test(path))body=fixtures.perfil;
  else if(path.endsWith('/buscar'))body={...fixtures.busquedaComentarios,tipo:request.params.get('tipo')??'COMENTARIO'};
  else if(path.endsWith('/admin/reportes'))body=fixtures.reportes;
  else if(path.endsWith('/admin/metricas'))body=fixtures.metricas;
  else if(path.endsWith('/menciones/buscar'))body=[];
  else if(/\/reacciones\/usuarios$/.test(path))body={...fixtures.comentarios,content:[]};
  else if(/\/reacciones$/.test(path))body=fixtures.detalle.reacciones;
  else return throwError(()=>new HttpErrorResponse({status:404,error:{message:'Operación fuera del preview de solo lectura'}}));
  return of(new HttpResponse({status:200,body}));
}]))]}).catch(error=>console.error(error));
