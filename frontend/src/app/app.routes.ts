import { inject } from '@angular/core';
import { Routes,CanActivateFn,Router } from '@angular/router';
import { SessionService } from './core/state.service';

const accountGuard:CanActivateFn=(_,state)=>inject(SessionService).user()?true:inject(Router).createUrlTree(['/acceso'],{queryParams:{volver:state.url}});
const roleGuard:CanActivateFn=(route,state)=>{
  const session=inject(SessionService),router=inject(Router),user=session.user();
  if(!user)return router.createUrlTree(['/acceso'],{queryParams:{volver:state.url}});
  return user.rol===route.data['role']?true:router.createUrlTree(['/cuenta/perfil']);
};
export const routes:Routes=[
  {path:'',loadComponent:()=>import('./pages/storefront').then(m=>m.HomePage)},
  {path:'catalogo',loadComponent:()=>import('./pages/storefront').then(m=>m.CatalogPage)},
  {path:'producto/:id',loadComponent:()=>import('./pages/product').then(m=>m.ProductPage)},
  {path:'tiendas',loadComponent:()=>import('./pages/storefront').then(m=>m.ShopsPage)},
  {path:'tienda/:id',loadComponent:()=>import('./pages/storefront').then(m=>m.ShopPage)},
  {path:'acceso',loadComponent:()=>import('./pages/auth').then(m=>m.AuthPage),data:{view:'login'}},
  {path:'registro',loadComponent:()=>import('./pages/auth').then(m=>m.AuthPage),data:{view:'register'}},
  {path:'recuperar',loadComponent:()=>import('./pages/auth').then(m=>m.AuthPage),data:{view:'recover'}},
  {path:'restablecer',loadComponent:()=>import('./pages/auth').then(m=>m.AuthPage),data:{view:'reset'}},
  {path:'carrito',loadComponent:()=>import('./pages/checkout').then(m=>m.CartPage)},
  {path:'checkout',loadComponent:()=>import('./pages/checkout').then(m=>m.CheckoutPage)},
  {path:'pedidos/:id',loadComponent:()=>import('./pages/checkout').then(m=>m.OrderPage)},
  {path:'cuenta/:view',canActivate:[accountGuard],loadComponent:()=>import('./pages/account').then(m=>m.AccountPage)},
  {path:'cuenta',redirectTo:'cuenta/perfil',pathMatch:'full'},
  {path:'vendedor',canActivate:[roleGuard],data:{role:'VENDEDOR'},loadComponent:()=>import('./pages/seller').then(m=>m.SellerLayout),children:[
    {path:'',loadComponent:()=>import('./pages/seller').then(m=>m.SellerDashboard)},
    {path:'productos',loadComponent:()=>import('./pages/seller').then(m=>m.SellerProducts)},
    {path:'publicar',loadComponent:()=>import('./pages/seller-editor').then(m=>m.SellerEditor)},
    {path:'productos/:id',loadComponent:()=>import('./pages/seller-editor').then(m=>m.SellerEditor)},
    {path:'inventario',loadComponent:()=>import('./pages/seller').then(m=>m.SellerInventory)},
    {path:'ventas',loadComponent:()=>import('./pages/seller').then(m=>m.SellerSales)},
    {path:'tienda',loadComponent:()=>import('./pages/seller').then(m=>m.SellerShop)},
  ]},
  {path:'admin',canActivate:[roleGuard],data:{role:'ADMIN'},loadComponent:()=>import('./pages/admin').then(m=>m.AdminPage)},
  {path:'ayuda',loadComponent:()=>import('./pages/storefront').then(m=>m.HelpPage)},
  {path:'**',loadComponent:()=>import('./pages/storefront').then(m=>m.NotFoundPage)},
];
