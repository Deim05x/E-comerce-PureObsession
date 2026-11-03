export interface Page<T> { contenido:T[];totalElementos:number;totalPaginas:number;pagina:number;tamanio:number; }
export interface User {id:string;nombre:string;email:string;rol:'CLIENTE'|'VENDEDOR'|'ADMIN';telefono?:string;nacimiento?:string;}
export interface Product {
  id:string;fraganciaId:string;vendedorId:string;nombre:string;marca:string;descripcion:string;categoria:string|null;
  vendedor:string;ciudad:string;tipo:'PERFUME'|'DECANT'|'TESTER';volumenMl:number;precio:number;moneda:string;
  original:boolean;publicada:boolean;familia:string;concentracion:string;notasSalida:string;notasCorazon:string;notasFondo:string;
  stockDisponible:number;imagenes:string[];calificacion:number;resenas:number;certificado:string|null;
}
export interface Shop {id:string;nombre:string;descripcion:string;ciudad:string;telefono:string;logoUrl:string|null;}
export interface Reputation {calificacion:number;resenas:number;ventas:number;productos:number;}
export interface CartLine {producto:Product;cantidad:number;subtotal:number;}
export interface Cart {items:CartLine[];subtotal:number;moneda:string;}
export interface Address {id:string;etiqueta:string;receptor:string;telefono:string;departamento:string;ciudad:string;direccion:string;indicaciones:string;predeterminada:boolean;}
export interface OrderLine {presentacionId:string;fraganciaId:string;vendedorId:string;descripcion:string;cantidad:number;precioUnitario:number;empaque:string;}
export interface Delivery {nombre:string;correo:string;telefono:string;direccion:string;}
export interface Order {id:string;clienteId:string|null;entrega:Delivery;estado:string;total:number;moneda:string;lineas:OrderLine[];claveConsulta:string|null;subtotal:number;envio:number;regalo:number;}
export interface Shipment {vendedorId:string;estado:string;transportadora:string|null;guia:string|null;enviadoEn:string|null;entregadoEn:string|null;}
export interface Tracking {pedido:Order;creadoEn:string;reservaVenceEn:string;motivoCancelacion:string|null;pago:{referencia:string;importe:number;simulado:boolean;registradoEn:string}|null;envios:Shipment[];}
export interface Sale {pedidoId:string;estado:string;entrega:Delivery;creadoEn:string;reservaVenceEn:string;subtotal:number;items:OrderLine[];envio:Shipment|null;}
export interface Bottle {id:string;fraganciaId:string;nombre:string;originalMl:number;disponibleMl:number;}
export interface Review {id:string;autor:string;puntuacion:number;comentario:string;creadaEn:string;}
export interface Options {familias:string[];concentraciones:string[];presentaciones:string[];origenes:string[];empaques:string[];categorias:string[];modoDemo?:boolean;}
export interface Quote {subtotal:number;envio:number;regalo:number;total:number;moneda:string;vendedores:number;}
export interface CheckoutItem {presentacionId:string;cantidad:number;empaque:'ESTANDAR'|'REGALO';}
export interface Certificate {presentacionId:string;nombre:string;vendedorId:string;estado:string;tipoContenido:string;}
export const emptyPage = <T>():Page<T> => ({contenido:[],totalElementos:0,totalPaginas:0,pagina:0,tamanio:20});
