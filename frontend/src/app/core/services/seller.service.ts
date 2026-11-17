import { Injectable,inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../api.service';
import { Bottle,Page,Product,Sale,Shop } from '../models';

@Injectable({providedIn:'root'})
export class SellerService {
  private api=inject(ApiService);
  dashboard(){return this.api.get<Partial<Record<string,number>>>('/vendedor/dashboard');}
  products(){return this.api.get<Page<Product>>('/vendedor/productos?tamanio=100');}
  product(id:string){return firstValueFrom(this.api.get<Product>('/vendedor/productos/'+id));}
  bottles(){return this.api.get<Bottle[]>('/vendedor/botellas');}
  sales(){return this.api.get<Page<Sale>>('/vendedor/ventas?tamanio=100');}
  shop(){return this.api.get<Shop>('/vendedor/tienda');}
  createProduct(data:unknown){return this.api.post<Product>('/vendedor/productos',data);}
  updateProduct(id:string,data:unknown){return this.api.put<Product>('/vendedor/productos/'+id,data);}
  updateShop(data:Shop){return this.api.put<Shop>('/vendedor/tienda',data);}
  registerShipment(id:string,transportadora:string,guia:string){return this.api.post('/vendedor/ventas/'+id+'/envio',{transportadora,guia});}
  uploadImage(id:string,file:File){const form=new FormData();form.append('archivo',file);return this.api.post('/vendedor/productos/'+id+'/imagenes',form);}
  uploadCertificate(id:string,file:File){const form=new FormData();form.append('archivo',file);return this.api.post('/presentaciones/'+id+'/certificado',form);}
  publish(id:string){return this.api.post('/presentaciones/'+id+'/publicacion');}
  unpublish(id:string){return this.api.delete('/vendedor/productos/'+id+'/publicacion');}
}
