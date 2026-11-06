import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { Rol } from './models';

@Injectable({ providedIn: 'root' })
export class RolService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  listar(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.api.buildUrl('/api/v1/roles'));
  }
}
