# Clasificación de Entidades y Value Objects

La clasificación usa tres pruebas: identidad, reemplazo y ciclo de vida.

| Concepto | Tipo | Identidad | Reemplazo | Ciclo de vida |
|---|---|---|---|---|
| Pedido | Entidad | Se identifica por `id`, no por sus líneas ni total. | Sus líneas y estado cambian sin crear otro pedido. | Nace al comprar y termina su ciclo al cancelarse o entregarse. |
| LineaPedido | Entidad | Tiene su propio `id`; dos líneas iguales pueden representar compras distintas. | Cantidad y precio se conservan como foto de la compra, no definen su identidad. | Existe solo durante la vida del pedido que la contiene. |
| Perfume | Entidad | Su `id` distingue presentaciones aunque compartan fragancia y volumen. | Puede representarse por otro objeto con el mismo `id` sin perder continuidad. | Se registra y se gestiona como artículo del catálogo. |
| Resena | Entidad | Su `id` identifica la evidencia de una compra concreta. | El comentario no es su identidad; se mantiene ligada al pedido y cliente. | Solo nace después de que su pedido sea entregado. |
| Volumen | Value Object | No tiene identidad propia; 10 ml equivale a 10 ml. | Se reemplaza por un nuevo valor al extraer de una botella. | Vive como atributo de una presentación, no se administra por separado. |
| Precio | Value Object | Dos precios con monto y moneda iguales son iguales. | Un nuevo precio sustituye al anterior. | No tiene ciclo de vida independiente de la línea o producto que describe. |
| PiramideOlfativa | Value Object | La definen sus tres fases: salida, corazón y fondo. | Se reemplaza completa si cambia la composición. | Pertenece a la fragancia y no se rastrea independientemente. |
| EstadoPedido | Value Object | El valor `CREADO`, `CONFIRMADO`, etc. es suficiente para distinguirlo. | Cada transición sustituye el estado anterior. | No existe fuera del pedido. |
