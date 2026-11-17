import { Injectable,inject } from '@angular/core';
import { ApiService } from '../api.service';
import { CheckoutItem,Order,Quote,Tracking } from '../models';

@Injectable({providedIn:'root'})
export class OrderService {
  private api=inject(ApiService);
  quote(items:CheckoutItem[]){return this.api.post<Quote>('/checkout/cotizacion',{items});}
  checkout(data:unknown,headers?:Record<string,string>){return this.api.post<Order>('/checkout',data,headers);}
  mine(){return this.api.get<Order[]>('/pedidos/mios');}
  tracking(id:string,key?:string){return this.api.get<Tracking>('/pedidos/'+id+'/seguimiento',key);}
  cancel(id:string,headers?:Record<string,string>){return this.api.post('/pedidos/'+id+'/cancelacion',{},headers);}
  simulatePayment(id:string,headers?:Record<string,string>){return this.api.post('/demo/pedidos/'+id+'/pago',{},headers);}
  receive(id:string,vendedor:string,headers?:Record<string,string>){return this.api.post('/pedidos/'+id+'/envios/'+vendedor+'/recepcion',{},headers);}
}
