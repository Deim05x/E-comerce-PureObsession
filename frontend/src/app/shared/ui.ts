import { Component, input, output, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product } from '../core/models';
import { CartService,FavoritesService } from '../core/state.service';
import { label,money } from '../core/domain';

const paths:Record<string,string>={
  search:'m21 21-4.4-4.4M19 11a8 8 0 1 1-16 0 8 8 0 0 1 16 0',heart:'M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8',
  bag:'M6 7h12l2 14H4L6 7Zm3 0V5a3 3 0 0 1 6 0v2',user:'M20 21v-2a6 6 0 0 0-6-6h-4a6 6 0 0 0-6 6v2M16 5a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
  arrow:'M4 12h16m-6-6 6 6-6 6',chevron:'m9 5 7 7-7 7',close:'m6 6 12 12M6 18 18 6',menu:'M3 6h18M3 12h18M3 18h18',check:'m4 12 5 5L20 6',
  plus:'M12 5v14M5 12h14',minus:'M5 12h14',star:'m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.3l-5.6 2.9 1.1-6.2L3 9.6l6.2-.9L12 3Z',
  location:'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0ZM15 10a3 3 0 1 1-6 0 3 3 0 0 1 6 0',
  box:'m3 7 9-4 9 4v10l-9 4-9-4V7Zm0 0 9 4 9-4M12 11v10M7.5 5 9 4.3 9 4.4',truck:'M3 5h11v12H3V5Zm11 5h4l3 4v3h-7M9 18a2 2 0 1 1-4 0 2 2 0 0 1 4 0m11 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0',
  shield:'M12 3 3 7v6c0 5 9 9 9 9s9-4 9-9V7l-9-4Zm-5 9 3 3 7-7',logout:'M9 21H4V3h5m5 4 5 5-5 5M9 12h10',grid:'M3 3h7v7H3V3Zm11 0h7v7h-7V3ZM3 14h7v7H3v-7Zm11 0h7v7h-7v-7Z',
  bottle:'M9 3h6v4H9V3Zm-1 7h8l3 3v8H5v-8l3-3ZM8 16h8',edit:'m16 3 5 5-12 12-6 1 1-6L16 3Zm-2 2 5 5',upload:'M12 16V3m-5 5 5-5 5 5M3 16v5h18v-5',
  trash:'M3 6h18M9 6V3h6v3M6 6l1 15h10l1-15M10 10v7M14 10v7',clock:'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM12 7v5l3 2',mail:'M3 5h18v14H3V5Zm0 1 9 7 9-7',
  leaf:'M20 3c0 10-1 17-10 17C-1 20 4 7 20 3ZM5 20 16 9',filter:'M4 7h16M7 12h10M10 17h4',eye:'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Zm13 0a3 3 0 1 1-6 0 3 3 0 0 1 6 0',
};
@Component({selector:'ui-icon',standalone:true,template:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.45" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="paths[name()]||paths['bottle']"/></svg>`,styles:[`:host{display:inline-flex;width:20px;height:20px;flex-shrink:0;vertical-align:middle}svg{width:100%;height:100%}`]})
export class IconComponent {name=input('bottle');paths=paths;}
@Component({selector:'app-brand',standalone:true,imports:[RouterLink],template:`<a routerLink="/" class="brand" aria-label="Pure Obsession, inicio"><span class="monogram">P<span>/</span>O</span><span class="brand-name">Pure Obsession<small>HAUTE PARFUMERIE</small></span></a>`})
export class BrandComponent {}
@Component({selector:'product-image',standalone:true,template:`@if(src()&&!failed()){<img [src]="src()" [alt]="name()" loading="lazy" (error)="failed.set(true)"/>}@else{<div class="bottle-art" role="img" [attr.aria-label]="'Imagen no disponible para '+name()"><svg viewBox="0 0 220 250" fill="none" aria-hidden="true"><defs><linearGradient id="bottle-glass" x1="20" y1="90" x2="185" y2="225" gradientUnits="userSpaceOnUse"><stop stop-color="#5b4b31"/><stop offset=".5" stop-color="#1d1a14"/><stop offset="1" stop-color="#8c7140"/></linearGradient></defs><ellipse cx="110" cy="233" rx="68" ry="7" fill="#000" opacity=".5"/><rect x="87" y="29" width="46" height="31" rx="3" fill="#27251e" stroke="#78694a"/><path d="M91 62h38v15l27 13a9 9 0 0 1 5 8v118a9 9 0 0 1-9 9H68a9 9 0 0 1-9-9V98a9 9 0 0 1 5-8l27-13V62Z" fill="url(#bottle-glass)" stroke="#ac925a" stroke-opacity=".6"/><rect x="74" y="123" width="72" height="62" fill="#11100d" stroke="#9b824e" stroke-width=".6"/><path d="M92 145h36m-30 8h24m-19 14h14" stroke="#b89d65" stroke-width="1"/><path d="M65 105v103" stroke="#d7c28c" stroke-opacity=".2" stroke-width="2"/></svg><span>Imagen no disponible</span></div>}`,styles:[`:host{display:block;width:100%;height:100%}img{width:100%;height:100%;object-fit:contain}.bottle-art{height:100%;display:flex;flex-direction:column;align-items:center;justify-content:center}.bottle-art svg{height:85%;max-width:230px}.bottle-art span{font-size:9px;letter-spacing:.08em;color:#777269}`]})
export class ProductImageComponent {src=input<string|undefined>();name=input('Fragancia');failed=signal(false);}
@Component({selector:'product-card',standalone:true,imports:[RouterLink,IconComponent,ProductImageComponent],template:`
  <article class="product-card"><div class="product-visual"><a [routerLink]="['/producto',product().id]"><product-image [src]="product().imagenes[0]" [name]="product().nombre"/></a>
    <span class="product-tag">{{label(product().tipo)}} · {{product().volumenMl}} ml</span><button class="icon-button favorite-button" [class.selected]="favorites.ids().has(product().id)" (click)="favorites.toggle(product().id)" [attr.aria-label]="favorites.ids().has(product().id)?'Quitar de favoritos':'Añadir a favoritos'"><ui-icon name="heart"/></button></div>
    <div class="product-copy"><span class="eyebrow">{{product().marca||'COLECCIÓN OLFATIVA'}}</span><a class="product-title" [routerLink]="['/producto',product().id]">{{product().nombre}}</a><p class="small muted">{{label(product().concentracion)}} · {{label(product().familia)}}</p>
    <a class="seller-caption" [routerLink]="['/tienda',product().vendedorId]"><ui-icon name="bottle"/>{{product().vendedor}}</a><div class="product-bottom"><div><small class="muted">Precio · COP</small><strong>{{money(product().precio)}}</strong></div><button class="card-add" [disabled]="!product().stockDisponible||cart.busy()" (click)="cart.add(product())" [attr.aria-label]="'Añadir '+product().nombre+' al carrito'"><ui-icon name="bag"/></button></div>
    @if(!product().stockDisponible){<span class="small muted">Temporalmente agotado</span>}</div>
  </article>`})
export class ProductCardComponent {product=input.required<Product>();cart=inject(CartService);favorites=inject(FavoritesService);money=money;label=label;}
@Component({selector:'ui-state',standalone:true,imports:[IconComponent],template:`@if(loading()){<div class="loading-state" role="status"><span class="spinner"></span><p>Preparando tu selección…</p></div>}@else if(error()){<div class="empty-state" role="alert"><ui-icon name="bottle"/><h3>No pudimos cargar esta selección</h3><p>{{error()}}</p><button class="button outline" (click)="retry.emit()">Volver a intentar</button></div>}`})
export class StateComponent {loading=input(false);error=input('');retry=output<void>();}
