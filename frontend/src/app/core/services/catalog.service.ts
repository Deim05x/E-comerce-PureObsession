import { Injectable,inject } from '@angular/core';
import { ApiService } from '../api.service';
import { Page,Product,Shop,Reputation,Review } from '../models';

export type CatalogFilters=Record<string,string|number|null|undefined>;

@Injectable({providedIn:'root'})
export class CatalogService {
  private api=inject(ApiService);
  products(filters:CatalogFilters={}){
    const query=new URLSearchParams();
    Object.entries(filters).forEach(([key,value])=>{if(value!==null&&value!==undefined&&value!=='')query.set(key,String(value));});
    return this.api.get<Page<Product>>('/productos'+(query.size?'?'+query.toString():''));
  }
  product(id:string){return this.api.get<Product>('/productos/'+id);}
  shops(pagina=0,tamanio=12){return this.api.get<Page<Shop>>(`/tiendas?pagina=${pagina}&tamanio=${tamanio}`);}
  shop(id:string){return this.api.get<Shop>('/tiendas/'+id);}
  reputation(id:string){return this.api.get<Reputation>('/tiendas/'+id+'/reputacion');}
  reviews(id:string){return this.api.get<Review[]>('/productos/'+id+'/resenas');}
  saveReview(id:string,puntuacion:number,comentario:string){return this.api.put('/productos/'+id+'/resena',{puntuacion,comentario});}
}
