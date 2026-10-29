import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon, TuiInput, TuiLabel, TuiTextfield } from '@taiga-ui/core';
import { EvaluacionService } from '../evaluacion/evaluacion-service';
import { EvaluacionAgregado } from '../evaluacion/evaluacion.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    TuiButton,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiTextfield,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private readonly evaluacionService = inject(EvaluacionService);

  protected readonly agregado = signal<EvaluacionAgregado | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected filtroPeriodo = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.evaluacionService.agregados(this.filtroPeriodo || undefined).subscribe({
      next: (data) => this.agregado.set(data),
      error: () => {
        this.error.set('No se pudieron cargar los agregados nutricionales.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected formatear(n: number | null): string {
    return n === null ? '—' : n.toFixed(2);
  }
}
