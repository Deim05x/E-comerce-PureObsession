# Clasificación del dominio: las tres pruebas

Una entidad conserva su identidad aunque cambien sus datos. Un Value Object se identifica por su valor, se reemplaza como unidad y no tiene ciclo de vida independiente.

| Concepto | Clasificación | Identidad | Reemplazo | Ciclo de vida |
|---|---|---|---|---|
| BotellaMadre | Entidad y raíz | UUID distingue dos botellas de la misma fragancia. | Extraer volumen conserva la misma botella. | Se registra, se fracciona y puede agotarse o retirarse. |
| Fragancia | Entidad | UUID distingue referencias aromáticas aunque coincidan nombre y familia. | Publicar o desactivar conserva la identidad. | Nace como borrador, se publica y puede desactivarse. |
| Decant | Entidad interna de BotellaMadre | UUID distingue frascos de igual volumen. | Otro objeto con igual volumen no sustituye al frasco identificado. | Se crea por extracción y queda conservado por su botella. |
| Perfume | Entidad | UUID identifica el artículo de presentación completa. | Aplicar envoltura no crea otro artículo. | Se registra y puede prepararse para regalo. |
| Tester | Entidad | UUID distingue artículos de demostración. | Dos testers iguales en aroma no son el mismo artículo. | Se registra y se comercializa sin envoltura de regalo. |
| Pedido | Entidad y raíz | UUID identifica la compra, no su total. | Agregar líneas en CREADO o avanzar de estado mantiene el pedido. | CREADO, CONFIRMADO, ENVIADO, ENTREGADO; cancelación antes del envío. |
| LineaPedido | Entidad interna de Pedido | UUID identifica la línea; itemId y fraganciaId son referencias diferentes. | Su foto de precio y descripción es inmutable, pero no define identidad. | Se crea para un pedido y permanece en su historial. |
| Resena | Entidad | UUID identifica la reseña; pedidoId y clienteId acreditan procedencia. | Un texto igual no convierte dos reseñas en la misma entidad. | Solo nace para el comprador de un pedido entregado. |
| Concentracion | VO / enum | El valor del enum basta; no hay UUID. | Otro valor sustituye al anterior. | Es una clasificación de la fragancia, no un objeto administrado. |
| FamiliaOlfativa | VO / enum | FLORAL equivale a FLORAL. | Se compara y sustituye por valor. | Vive como característica de una fragancia. |
| EstadoPedido | VO / enum | CREADO equivale a CREADO. | Una transición reemplaza el valor, no el pedido. | No vive independientemente del pedido. |
| Volumen | VO / record | Dos medidas positivas iguales son equivalentes. | Una nueva medida reemplaza a otra. | Representa volumen original o solicitado; no inventario agotado. |
| VolumenDisponible | VO / record | Dos cantidades disponibles iguales son equivalentes. | Cada extracción sustituye la cantidad. | Atributo del inventario; admite cero y prohíbe negativos. |
| Precio | VO / record | Importe y moneda normalizada determinan igualdad. | Se reemplaza completo; las líneas conservan la foto de compra. | No tiene identidad ni gestión independiente. |
| PiramideOlfativa | VO / record | Salida, corazón y fondo determinan igualdad. | Se reemplaza como una composición completa. | Pertenece a una fragancia y exige las tres fases. |

## Decisiones relevantes

- Inmutabilidad no convierte automáticamente una entidad en VO: LineaPedido y Resena tienen identidad propia.
- Los records validan al construir. Los enums restringen los valores posibles; una referencia enum obligatoria se valida en la entidad que la usa.
- Las ocho entidades tienen constructor privado, fábrica, ausencia de setters e igualdad por UUID no nulo.
- Fragancia es información aromática, BotellaMadre es inventario físico: no son sinónimos.
