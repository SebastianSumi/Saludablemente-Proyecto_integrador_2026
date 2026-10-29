import {
  provideTaiga,
  tuiDateFormatProvider,
  tuiValidationErrorsProvider,
} from '@taiga-ui/core';
import {
  tuiInputDateOptionsProvider,
  tuiInputDateTimeOptionsProvider,
  tuiInputTimeOptionsProvider,
} from '@taiga-ui/kit';
import {
  FechaHoraTransformer,
  HoraTransformer,
  IsoDateTransformer,
} from './core/forms/fecha-transformers';
import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  withInMemoryScrolling,
  withViewTransitions,
} from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

import { routes } from './app.routes';
import { traceIdInterceptor } from './core/interceptors/trace-id-interceptor';
import { authTokenInterceptor } from './core/interceptors/auth-token-interceptor';
import { loadingBarInterceptor } from './core/interceptors/loading-bar-interceptor';

echarts.use([
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  CanvasRenderer,
]);

/** Ruta completa (con acentos) a partir de un snapshot del router. */
function rutaDe(snap: ActivatedRouteSnapshot): string {
  const partes: string[] = [];
  for (let n: ActivatedRouteSnapshot | null = snap; n; n = n.firstChild) {
    for (const s of n.url) partes.push(s.path);
  }
  return '/' + partes.join('/');
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(
      routes,
      // Al navegar, la página nueva empieza arriba (y al volver atrás se
      // restaura la posición). Necesario para que el morph del ícono tenga
      // su destino (el hero) dentro del viewport.
      withInMemoryScrolling({
        scrollPositionRestoration: 'enabled',
        anchorScrolling: 'enabled',
      }),
      // Transición suave (fade + subida) entre el centro de administración y
      // sus módulos. Fase de prueba: solo admin ↔ /admin/modulo/*.
      withViewTransitions({
        skipInitialTransition: true,
        onViewTransitionCreated: ({ transition, from, to }) => {
          const desde = rutaDe(from);
          const hacia = rutaDe(to);
          const origenAdmin =
            desde === '/admin' || desde.startsWith('/admin/modulo/');
          const destinoModulo =
            hacia === '/admin' || hacia.startsWith('/admin/modulo/');
          // Alertas clínicas ↔ sus páginas (bandeja, tablero, reglas): crossfade;
          // en las reglas además el glifo hace morph.
          const moduloAlertas = (ruta: string) => ruta === '/admin/modulo/alertas';
          const alertaClinica = (ruta: string) => ruta.startsWith('/alertaclinica/');
          const cruceAlertas =
            (moduloAlertas(desde) && alertaClinica(hacia)) ||
            (alertaClinica(desde) && moduloAlertas(hacia));
          if (!((origenAdmin && destinoModulo) || cruceAlertas)) {
            transition.skipTransition();
          }
        },
      })
    ),
    provideHttpClient(
      withInterceptors([
        traceIdInterceptor,
        authTokenInterceptor,
        loadingBarInterceptor,
      ])
    ),
    provideEchartsCore({ echarts }),
    provideTaiga(),
    tuiValidationErrorsProvider({
      required: 'Este campo es obligatorio.',
      email: 'Correo electrónico inválido.',
      minlength: 'Muy corto.',
      maxlength: 'Muy largo.',
      pattern: 'Formato inválido.',
      min: 'Valor muy bajo.',
      max: 'Valor muy alto.',
    }),

    // ==== Fechas ISO (YMD con guiones, igual que el backend) ====
    // Los formularios siguen con strings; los pickers Taiga los muestran
    // con calendario gracias a estos transformers.
    tuiDateFormatProvider({ mode: 'yyyy/mm/dd', separator: '-' }),
    tuiInputDateOptionsProvider({ valueTransformer: new IsoDateTransformer() }),
    tuiInputTimeOptionsProvider({ valueTransformer: new HoraTransformer() }),
    tuiInputDateTimeOptionsProvider({ valueTransformer: new FechaHoraTransformer() }),
  ],
};
