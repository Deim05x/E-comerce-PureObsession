import {Component,inject,ChangeDetectorRef} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink,RouterLinkActive,ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {resource,errorMessage} from '../core/api.service';
import {SessionService,ToastService} from '../core/state.service';
import {AccountService} from '../core/services/account.service';
import {OrderService} from '../core/services/order.service';
import {Order,Product,Page,emptyPage} from '../core/models';
import {money,label} from '../core/domain';
import {IconComponent,ProductCardComponent,StateComponent} from '../shared/ui';
@Component({standalone:true,imports:[FormsModule,RouterLink,RouterLinkActive,IconComponent,ProductCardComponent,StateComponent],template:`
<section class="container section account-page"><header class="page-heading"><div><p class="eyebrow">TU ESPACIO PERSONAL</p><h1>Mi cuenta</h1><p>Gestiona tus datos, pedidos y fragancias favoritas desde un solo lugar.</p></div><span class="page-seal"><ui-icon name="shield"/> Acceso privado</span></header>
<nav class="tabs account-tabs" aria-label="Secciones de mi cuenta"><a routerLink="/cuenta/perfil" routerLinkActive="active"><ui-icon name="user"/>Perfil</a><a routerLink="/cuenta/pedidos" routerLinkActive="active"><ui-icon name="box"/>Mis pedidos</a><a routerLink="/cuenta/favoritos" routerLinkActive="active"><ui-icon name="heart"/>Favoritos</a><button (click)="session.logout()"><ui-icon name="logout"/>Cerrar sesión</button></nav>
@switch(params()?.get('view')){
@case('pedidos') {<div class="section-heading compact-heading"><div><span class="eyebrow">TU HISTORIAL OLFATIVO</span><h2>Mis pedidos</h2></div></div><ui-state [loading]="orders.loading()" [error]="orders.error()" (retry)="orders.reload()"/>@if(!orders.loading()&&!orders.error()){@for(order of orders.data();track order.id){<article class="panel account-order"><div><span class="eyebrow">PEDIDO</span><a class="serif account-order-title" [routerLink]="['/pedidos',order.id]">{{order.id}}</a></div><div class="account-order-meta"><span>{{label(order.estado)}}</span><strong>{{money(order.total)}}</strong></div><a class="icon-button" [routerLink]="['/pedidos',order.id]" aria-label="Ver detalle del pedido"><ui-icon name="arrow"/></a></article>}@empty{<div class="empty-state"><ui-icon name="box"/><h3>Aún no hay pedidos</h3><p>Cuando encuentres tu próxima esencia, podrás seguir su recorrido aquí.</p><a routerLink="/catalogo" class="button outline">Explorar fragancias</a></div>}}}
@case('favoritos'){<div class="section-heading compact-heading"><div><span class="eyebrow">TU CURADURÍA PERSONAL</span><h2>Fragancias favoritas</h2></div></div><ui-state [loading]="favorites.loading()" [error]="favorites.error()" (retry)="favorites.reload()"/>@if(!favorites.loading()&&!favorites.error()){@if(favorites.data().contenido.length){<div class="product-grid">@for(product of favorites.data().contenido;track product.id){<product-card [product]="product"/>}</div>}@else{<div class="empty-state"><ui-icon name="heart"/><h3>Tu selección está esperando</h3><p>Guarda las fragancias que quieras volver a encontrar.</p><a routerLink="/catalogo" class="button outline">Descubrir la colección</a></div>}}}
@default{<form class="panel form-stack narrow profile-card" (ngSubmit)="save()"><div class="panel-heading"><span class="step-number">01</span><div><span class="eyebrow">IDENTIDAD DE LA CUENTA</span><h2>Datos personales</h2></div></div><label>Nombre<input name="nombre" [(ngModel)]="nombre" required></label><label>Correo electrónico<input [value]="session.user()?.email" disabled></label><label>Teléfono<input name="telefono" [(ngModel)]="telefono" placeholder="Tu número de contacto"></label><button class="button gold" [disabled]="busy">{{busy?'Guardando…':'Guardar cambios'}} <ui-icon name="arrow"/></button></form>}}
</section>`})
export class AccountPage{
 session=inject(SessionService);cdr=inject(ChangeDetectorRef);account=inject(AccountService);ordersApi=inject(OrderService);toast=inject(ToastService);params=toSignal(inject(ActivatedRoute).paramMap);
 nombre=this.session.user()?.nombre??'';telefono=this.session.user()?.telefono??'';busy=false;money=money;label=label;
 orders=resource<Order[]>(()=>this.params()?.get('view')==='pedidos'?this.ordersApi.mine():null,[]);
 favorites=resource<Page<Product>>(()=>this.params()?.get('view')==='favoritos'?this.account.favorites():null,emptyPage<Product>());
 async save(){this.busy=true;try{const user=await this.account.updateProfile({nombre:this.nombre,telefono:this.telefono,nacimiento:this.session.user()?.nacimiento??null});this.session.saveUser(user);this.toast.show('Perfil actualizado.');}catch(e){this.toast.show(errorMessage(e),true);}finally{this.busy=false;this.cdr.markForCheck();}}
}
