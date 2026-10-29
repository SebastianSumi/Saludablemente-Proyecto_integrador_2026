import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { UsuarioPerfil } from './perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/mi-perfil';

  obtener(): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>(this.api.buildUrl(this.resource));
  }
}
