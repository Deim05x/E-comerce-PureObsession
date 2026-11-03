export function money(amount:number):string {
  return new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',minimumFractionDigits:0,maximumFractionDigits:2}).format(Number.isFinite(amount)?amount:0);
}
export function label(value:string|null|undefined):string {
  const labels:Record<string,string>={PERFUME:'Botella regular',DECANT:'Decant',TESTER:'Tester',AMADERADA:'Amaderada',CITRICA:'Cítrica',FLORAL:'Floral',ORIENTAL:'Oriental',GOURMAND:'Gourmand',EAU_DE_PARFUM:'Eau de parfum',EAU_DE_TOILETTE:'Eau de toilette',PARFUM:'Parfum',EXTRAIT:'Extrait',NICHO:'Nicho',ARABE:'Árabe',DISENADOR:'Diseñador',CREADO:'Pendiente de pago',CONFIRMADO:'Confirmado',ENVIADO:'En camino',ENTREGADO:'Entregado',CANCELADO:'Cancelado',PENDIENTE:'Pendiente',APROBADO:'Aprobado',RECHAZADO:'Rechazado',ESTANDAR:'Estándar',REGALO:'Regalo',CLIENTE:'Cliente',VENDEDOR:'Vendedor',ADMIN:'Administración'};
  return value ? labels[value]??value.replaceAll('_',' ').toLowerCase() : 'Sin información';
}
export function initials(name:string):string {return name.trim().split(/\s+/).slice(0,2).map(x=>x.charAt(0)).join('').toUpperCase()||'PO';}
export function shortId(id:string):string {return id.slice(0,8).toUpperCase();}
export function date(value:string|null|undefined):string {return value?new Intl.DateTimeFormat('es-CO',{dateStyle:'medium'}).format(new Date(value)):'—';}
export function safeReturn(path:string|null):string {return path?.startsWith('/')&&!path.startsWith('//')&&!path.startsWith('/acceso') ? path : '/';}
export interface LocalLine {producto:import('./models').Product;cantidad:number;}
export function sanitizeLocalCart(value:unknown):LocalLine[] {
  if(!Array.isArray(value))return [];
  const seen=new Set<string>();
  return value.filter((x):x is LocalLine=>!!x&&typeof x==='object'&&typeof x.producto?.id==='string'&&typeof x.producto?.nombre==='string'&&Number.isFinite(x.producto?.precio)&&x.producto.precio>=0&&Number.isInteger(x.cantidad)&&x.cantidad>0&&x.cantidad<=1000&&!seen.has(x.producto.id)&&!!seen.add(x.producto.id)).slice(0,50);
}
