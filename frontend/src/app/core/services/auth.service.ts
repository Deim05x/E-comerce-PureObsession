import { Injectable,inject } from '@angular/core';
import { ApiService } from '../api.service';
import { User } from '../models';

export interface LoginResponse {token:string;expiraEn:string;}
export interface RegisterRequest {nombre:string;cedula:string;email:string;clave:string;rol:'CLIENTE'|'VENDEDOR';}

@Injectable({providedIn:'root'})
export class AuthService {
  private api=inject(ApiService);
  login(email:string,clave:string){return this.api.post<LoginResponse>('/auth/entrar',{email,clave});}
  register(request:RegisterRequest){return this.api.post<void>('/auth/registro',request);}
  recover(email:string){return this.api.post<{tokenDemo?:string}>('/auth/recuperacion',{email});}
  reset(token:string|null,nueva:string){return this.api.post<void>('/auth/restablecer',{token,nueva});}
  profile(token?:string){return this.api.get<User>('/perfil',undefined,token?{Authorization:`Bearer ${token}`}:{ });}
}
