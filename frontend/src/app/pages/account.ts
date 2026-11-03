import {Component,inject,ChangeDetectorRef} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink,ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {ApiService,resource,errorMessage} from '../core/api.service';
import {SessionService,ToastService} from '../core/state.service';
import {Order,Product,Page,emptyPage} from '../core/models';
import {money,label} from '../core/domain';
import {ProductCardComponent} from '../shared/ui';
@Component({standalone:true,imports:[FormsModule,RouterLink,ProductCardComponent],template:`
<section class="container section"><p class="eyebrow">TU ESPACIO PERSONAL</p><h1>Mi cuenta</h1>
<nav class="tabs"><a routerLink="/cuenta/perfil">Perfil</a><a routerLink="/cuenta/pedidos">Mis pedidos</a><a routerLink="/cuenta/favoritos">Favoritos</a><button (click)="session.logout()">Cerrar sesión</button></nav>
@switch(params()?.get('view')){
@case('pedidos') {<h2>Mis pedidos</h2><p class="error">{{orders.error()}}</p>@for(order of orders.data();track order.id){<article class="panel"><a [routerLink]="['/pedidos',order.id]">Pedido {{order.id}}</a><p>{{label(order.estado)}} · {{money(order.total)}}</p></article>}@empty{<p>No tienes pedidos todavía.</p>}}
@case('favoritos'){<h2>Tu selección</h2><p class="error">{{favorites.error()}}</p><div class="product-grid">@for(product of favorites.data().contenido;track product.id){<product-card [product]="product"/>}@empty{<p>Aún no has guardado fragancias.</p>}</div>}
@default{<form class="panel form-stack narrow" (ngSubmit)="save()"><h2>Datos personales</h2><label>Nombre<input name="nombre" [(ngModel)]="nombre" required></label><label>Correo<input [value]="session.user()?.email" disabled></label><label>Teléfono<input name="telefono" [(ngModel)]="telefono"></label><button class="button" [disabled]="busy">Guardar cambios</button></form>}}
</section>`})
export class AccountPage{
 session=inject(SessionService);cdr=inject(ChangeDetectorRef);api=inject(ApiService);toast=inject(ToastService);params=toSignal(inject(ActivatedRoute).paramMap);
 nombre=this.session.user()?.nombre??'';telefono=this.session.user()?.telefono??'';busy=false;money=money;label=label;
 orders=resource<Order[]>(()=>this.params()?.get('view')==='pedidos'?'/pedidos/mios':null,[]);
 favorites=resource<Page<Product>>(()=>this.params()?.get('view')==='favoritos'?'/favoritos':null,emptyPage<Product>());
 async save(){this.busy=true;try{const user=await this.api.put<any>('/perfil',{nombre:this.nombre,telefono:this.telefono,nacimiento:this.session.user()?.nacimiento??null});this.session.saveUser(user);this.toast.show('Perfil actualizado.');}catch(e){this.toast.show(errorMessage(e),true);}finally{this.busy=false;this.cdr.markForCheck();}}
}
