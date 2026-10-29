import { TuiRoot } from '@taiga-ui/core';
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet, TuiRoot],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('saludablemente-frontend');
}
