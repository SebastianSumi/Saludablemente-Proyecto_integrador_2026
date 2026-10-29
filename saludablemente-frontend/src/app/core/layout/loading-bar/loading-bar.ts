import { Component, inject } from '@angular/core';
import { LoadingBarService } from '../../services/loading-bar-service';
import { ScrollProgressDirective } from '../../../shared/animations/scroll-progress.directive';

/**
 * Barra superior global. Mientras hay cargas (navegación o HTTP) muestra el
 * progreso de {@link LoadingBarService}; en reposo refleja el avance de scroll
 * (vía [appScrollProgress]). El estilo vive en src/styles.css (.loading-bar).
 */
@Component({
  selector: 'app-loading-bar',
  standalone: true,
  imports: [ScrollProgressDirective],
  template: `
    <div
      class="loading-bar"
      [class.cargando]="barra.visible()"
      [style.--progreso]="barra.progreso()"
      appScrollProgress
    ></div>
  `,
})
export class LoadingBarComponent {
  protected readonly barra = inject(LoadingBarService);
}
