import { Injectable,inject } from '@angular/core';
import { ApiService } from '../api.service';
import { Certificate,Page,Tracking } from '../models';

@Injectable({providedIn:'root'})
export class AdminService {
  private api=inject(ApiService);
  certificates(){return this.api.get<Page<Certificate>>('/admin/certificados?estado=PENDIENTE&tamanio=100');}
  orders(){return this.api.get<Page<Tracking>>('/admin/pedidos?tamanio=100');}
  downloadCertificate(id:string){return this.api.download('/presentaciones/'+id+'/certificado');}
  reviewCertificate(id:string,aprobado:boolean){return this.api.post('/presentaciones/'+id+'/revision',{aprobado});}
  confirmPayment(id:string,referencia:string){return this.api.post('/admin/pedidos/'+id+'/confirmacion',{referencia});}
}
