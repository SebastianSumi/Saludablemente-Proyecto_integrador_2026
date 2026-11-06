import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';

/** Confirmación visual; el padre conserva validación, archivo y subida. */
@Component({
  selector: 'app-actividad-subida-dialog',
  imports: [FormsModule, TuiButton, TuiButtonLoading, TuiIcon, TuiInput, TuiLabel, TuiTextfield],
  templateUrl: './actividad-subida-dialog.html',
  styleUrl: './actividad-subida-dialog.css',
})
export class ActividadSubidaDialog {
  readonly preview = input<string | null>(null);
  readonly fileName = input<string | undefined>();
  readonly sizeLabel = input('');
  readonly description = input('');
  readonly error = input<string | null>(null);
  readonly loading = input(false);
  readonly descriptionChange = output<string>();
  readonly cancel = output<void>();
  readonly confirm = output<void>();
}
