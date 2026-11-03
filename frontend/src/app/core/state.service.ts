import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from './api.service';
import { User,Product,Cart,Page,Options } from './models';
import { sanitizeLocalCart,LocalLine } from './domain';

@Injectable({providedIn:'root'})
export class ToastService {
  messages=signal<{id:number;text:string;error:boolean}[]>([]);private sequence=0;
  show(text:string,error=false){const id=++this.sequence;this.messages.update(x=>[...x,{id,text,error}]);setTimeout(()=>this.dismiss(id),5500);}
  dismiss(id:number){this.messages.update(x=>x.filter(m=>m.id!==id));}
}
@Injectable({providedIn:'root'})
export class SessionService {
  private api=inject(ApiService);private router=inject(Router);
  user=signal<User|null>(null);options=signal<Options|null>(null);
  loggedIn=computed(()=>!!this.user());
  constructor(){
    try {const saved=sessionStorage.getItem('po-user');if(saved&&Date.parse(sessionStorage.getItem('po-expiry')??'')>Date.now())this.user.set(JSON.parse(saved));else this.clear();}catch{this.clear();}
    window.addEventListener('po-session-expired',()=>{if(this.user()){this.clear();void this.router.navigate(['/acceso'],{queryParams:{volver:this.router.url}});}});
  }
  async initialize(){
    try{this.options.set(await firstValueFrom(this.api.get<Options>('/opciones')));}catch{/* La pagina muestra su propio estado de conexion. */}
    if(this.user())try{const user=await firstValueFrom(this.api.get<User>('/perfil'));this.saveUser(user);}catch{this.clear();}
  }
  async login(email:string,clave:string){
    const response=await this.api.post<{token:string;expiraEn:string}>('/auth/entrar',{email,clave});
    sessionStorage.setItem('po-token',response.token);sessionStorage.setItem('po-expiry',response.expiraEn);
    const user=await firstValueFrom(this.api.get<User>('/perfil'));this.saveUser(user);return user;
  }
  saveUser(user:User){this.user.set(user);sessionStorage.setItem('po-user',JSON.stringify(user));}
  clear(){this.user.set(null);sessionStorage.removeItem('po-token');sessionStorage.removeItem('po-user');sessionStorage.removeItem('po-expiry');}
  logout(){this.clear();void this.router.navigate(['/']);}
}
@Injectable({providedIn:'root'})
export class CartService {
  private api=inject(ApiService);private session=inject(SessionService);private toast=inject(ToastService);
  cart=signal<Cart>({items:[],subtotal:0,moneda:'COP'});busy=signal(false);
  count=computed(()=>this.cart().items.reduce((n,l)=>n+l.cantidad,0));
  constructor(){effect(()=>{const user=this.session.user();if(user)void this.loadAndMerge();else this.local();});}
  private read():LocalLine[]{try{return sanitizeLocalCart(JSON.parse(localStorage.getItem('po-cart')??'[]'));}catch{return [];}}
  private local(){const items=this.read().map(l=>({...l,subtotal:l.producto.precio*l.cantidad}));this.cart.set({items,subtotal:items.reduce((n,l)=>n+l.subtotal,0),moneda:'COP'});}
  private write(lines:LocalLine[]){localStorage.setItem('po-cart',JSON.stringify(lines));this.local();}
  async reload(){if(this.session.user())this.cart.set(await firstValueFrom(this.api.get<Cart>('/carrito')));else this.local();}
  private async loadAndMerge(){
    try{
      let current=await firstValueFrom(this.api.get<Cart>('/carrito'));const remaining:LocalLine[]=[];
      for(const line of this.read())try{const existing=current.items.find(l=>l.producto.id===line.producto.id)?.cantidad??0;await this.api.put('/carrito/items/'+line.producto.id,{cantidad:Math.min(1000,existing+line.cantidad)});}catch{remaining.push(line);}
      localStorage.setItem('po-cart',JSON.stringify(remaining));await this.reload();
      if(remaining.length)this.toast.show('Algunos productos no pudieron añadirse a tu carrito. Revisa su disponibilidad.',true);
    }catch{/* No impedir navegar si el servicio no esta disponible. */}
  }
  async add(product:Product,cantidad=1){
    if(this.busy())return;this.busy.set(true);
    try{
      const previous=this.cart().items.find(l=>l.producto.id===product.id)?.cantidad??0;
      if(previous+cantidad>product.stockDisponible)throw new Error('La cantidad supera las existencias disponibles.');
      if(this.session.user()){await this.api.put('/carrito/items/'+product.id,{cantidad:previous+cantidad});await this.reload();}
      else {const lines=this.read();const found=lines.find(l=>l.producto.id===product.id);if(found)found.cantidad+=cantidad;else lines.push({producto:product,cantidad});if(lines.length>50)throw new Error('Tu carrito admite hasta 50 productos.');this.write(lines);}
      this.toast.show('Fragancia añadida a tu carrito.');
    }catch(e){this.toast.show(errorMessage(e),true);}finally{this.busy.set(false);}
  }
  async quantity(id:string,cantidad:number){
    this.busy.set(true);try{
      if(this.session.user()){if(cantidad===0)await this.api.delete('/carrito/items/'+id);else await this.api.put('/carrito/items/'+id,{cantidad});await this.reload();}
      else {const lines=this.read().filter(l=>l.producto.id!==id||cantidad>0);const l=lines.find(l=>l.producto.id===id);if(l){if(cantidad>l.producto.stockDisponible)throw new Error('No hay suficientes existencias.');l.cantidad=cantidad;}this.write(lines);}
    }catch(e){this.toast.show(errorMessage(e),true);}finally{this.busy.set(false);}
  }
  async clear(){if(this.session.user())await this.api.delete('/carrito');localStorage.removeItem('po-cart');await this.reload();}
}
@Injectable({providedIn:'root'})
export class FavoritesService {
  private api=inject(ApiService);private session=inject(SessionService);private router=inject(Router);private toast=inject(ToastService);
  ids=signal(new Set<string>());
  constructor(){effect(()=>{if(this.session.user())this.api.get<Page<Product>>('/favoritos?tamanio=100').subscribe({next:p=>this.ids.set(new Set(p.contenido.map(x=>x.id))),error:()=>this.ids.set(new Set())});else this.ids.set(new Set());});}
  async toggle(id:string){
    if(!this.session.user()){await this.router.navigate(['/acceso'],{queryParams:{volver:this.router.url}});return;}
    try{if(this.ids().has(id)){await this.api.delete('/favoritos/'+id);this.ids.update(s=>{const n=new Set(s);n.delete(id);return n;});}else{await this.api.put('/favoritos/'+id);this.ids.update(s=>new Set(s).add(id));}}
    catch(e){this.toast.show(errorMessage(e),true);}
  }
}
