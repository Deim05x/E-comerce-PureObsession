import { Injectable,inject } from '@angular/core';
import { ApiService } from '../api.service';
import { Address,Page,Product,User } from '../models';

@Injectable({providedIn:'root'})
export class AccountService {
  private api=inject(ApiService);
  addresses(){return this.api.get<Address[]>('/perfil/direcciones');}
  favorites(){return this.api.get<Page<Product>>('/favoritos');}
  updateProfile(data:{nombre:string;telefono:string;nacimiento:string|null}){return this.api.put<User>('/perfil',data);}
}
