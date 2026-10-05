# Casos de uso y DTOs — Pure Obsession

## Casos de uso (application/usecase)

Los casos de uso solo orquestan: buscan en el Repository, llaman a la operación del dominio y guardan. Las reglas de negocio viven en el dominio, no aquí.

### Vendedor

| # | Caso de uso | Qué hace | Repository que necesita | Operación de dominio / regla |
|---|---|---|---|---|
| 1 | RegistrarBotellaMadreUseCase | Registra una botella original con su fragancia, concentración y pirámide olfativa | BotellaMadreRepository | Fragancia.crear (nombre y familia obligatorios), PiramideOlfativa (Regla 5), Volumen (mayor a 0) y BotellaMadre.crear |
| 2 | CrearDecantUseCase | Extrae un decant de una botella madre | BotellaMadreRepository | BotellaMadre.crearDecant (Reglas 1, 2 y 6) |
| 3 | PublicarFraganciaUseCase | Publica una fragancia en el catálogo | BotellaMadreRepository | Fragancia.publicar (Reglas 4 y 5) |
| 4 | EliminarFraganciaUseCase | Elimina su inventario si no hay pedidos activos; si los hay, rechaza la eliminación | BotellaMadreRepository, PedidoRepository | Fragancia.validarEliminacion y PedidoRepository.existePedidoActivoConFragancia (Regla 8) |

> `Fragancia.desactivar()` retira la referencia del catálogo sin borrar inventario ni pedidos. La eliminación física es otra operación: se valida con `LineaPedido.fraganciaId` (distinto de `itemId`). `EliminarFraganciaUseCase` rechaza cuando existen pedidos activos y elimina todas las botellas de la fragancia cuando no existen. Los pedidos históricos conservan identificadores, descripción y precio.


### Comprador

| # | Caso de uso | Qué hace | Repository que necesita | Operación de dominio / regla |
|---|---|---|---|---|
| 5 | ConsultarCatalogoUseCase | Lista las fragancias publicadas (filtro opcional por familia; muestra la concentración) | BotellaMadreRepository | Solo lectura |
| 6 | RealizarPedidoUseCase | Crea un pedido con uno o más ítems | PedidoRepository, BotellaMadreRepository | Pedido.crear (invariantes del agregado Pedido) |
| 7 | CancelarPedidoUseCase | Cancela un pedido que aún no fue enviado | PedidoRepository | Pedido.cancelar |
| 8 | ConsultarMisPedidosUseCase | Lista los pedidos de un cliente | PedidoRepository | PedidoRepository.listarPorCliente |

## DTOs (application/dto)

Los DTOs solo transportan datos entre el exterior y los casos de uso. Ninguno trae campos que el dominio deba calcular o decidir (ids generados, precios, totales, herencia de fragancia).

### Requests

#### Request 1 — CrearDecantRequest
Mapea a: CrearDecantUseCase → BotellaMadre.crearDecant

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| botellaMadreId | UUID | Identifica de qué botella se extrae. Sin él no se sabe qué agregado cargar. |
| volumenMl | int | Volumen que se quiere extraer. El dominio valida que sea positivo, menor al original y no supere lo disponible. |

No incluye fragancia ni concentración: el decant las hereda de la botella madre (Regla 6), así que el cliente no puede alterarlas.

#### Request 2 — RealizarPedidoRequest
Mapea a: RealizarPedidoUseCase → Pedido.crear

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| clienteId | UUID | Quién compra. El Pedido guarda al cliente solo por ID. |
| items | List<ItemPedidoRequest> | Un pedido necesita al menos una línea (invariante 1). |
| items[].itemId | UUID | Decant, Perfume o Tester que se compra. |
| items[].cantidad | int | Cantidad de esa línea; debe ser mayor a 0. |

No incluye precio ni fraganciaId: ambos se resuelven a partir del artículo del catálogo al crear la línea. Esta resolución de precios es parte del diseño del caso de uso RealizarPedido, aún no programado. Si viniera del cliente, cualquiera podría manipular el total.

#### Request 3 — RegistrarBotellaMadreRequest
Mapea a: RegistrarBotellaMadreUseCase → Fragancia.crear + BotellaMadre.crear

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| nombreFragancia | String | Nombre con el que se identifica la fragancia en el catálogo. Es obligatorio (lo valida Fragancia). |
| familiaOlfativa | FamiliaOlfativa | Clasifica el perfil aromático principal. Es obligatoria (lo valida Fragancia). |
| concentracion | Concentracion | Necesaria para poder publicar después (Regla 4). Puede llegar vacía al registrar, pero no se podrá publicar sin ella. |
| notasSalida | String | Fase de salida de la pirámide olfativa (Regla 5). |
| notasCorazon | String | Fase de corazón de la pirámide olfativa (Regla 5). |
| notasFondo | String | Fase de fondo de la pirámide olfativa (Regla 5). |
| volumenMl | int | Volumen original de la botella. Debe ser mayor a 0 (lo valida Volumen). |

No incluye ids: los genera el sistema al crear las entidades. Tampoco incluye volumen disponible: al registrar, es igual al volumen original.

#### Request 4 — ConsultarCatalogoRequest
Mapea a: ConsultarCatalogoUseCase

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| familiaOlfativa | FamiliaOlfativa | Filtro opcional para mostrar solo la familia solicitada. Si es nulo, se listan todas las fragancias publicadas y activas. |

### Responses

#### Response 1 — PedidoDetalleResponse
Mapea desde: Pedido (resultado de RealizarPedidoUseCase y ConsultarMisPedidosUseCase)

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| pedidoId | UUID | Para que el cliente pueda referirse al pedido después. |
| estado | String | Muestra en qué punto del ciclo está (CREADO, CONFIRMADO, ENVIADO...). |
| total | double | Total calculado por el dominio, solo lectura. |
| moneda | String | Necesaria para interpretar el total. |
| lineas[].itemId | UUID | Qué ítem es. |
| lineas[].descripcion | String | Foto de la fragancia al momento de la compra. |
| lineas[].cantidad | int | Cantidad comprada. |
| lineas[].precioUnitario | double | Precio congelado al comprar. |
| lineas[].subtotal | double | Precio por cantidad de esa línea. |

#### Response 2 — BotellaMadreResponse
Mapea desde: BotellaMadre (resultado de RegistrarBotellaMadreUseCase)

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| botellaMadreId | UUID | El vendedor lo necesita para crear decants de esta botella después. |
| nombreFragancia | String | Confirma qué fragancia quedó registrada. |
| familiaOlfativa | FamiliaOlfativa | Confirma la clasificación guardada. |
| concentracion | Concentracion | Confirma la concentración guardada (puede ser nula si aún no se definió). |
| volumenDisponibleMl | int | Muestra cuánto se puede extraer; al registrar coincide con el volumen original. |

#### Response 3 — FraganciaCatalogoResponse
Mapea desde: Fragancia (resultado de ConsultarCatalogoUseCase)

| Campo | Tipo | Por qué es necesario |
|---|---|---|
| fraganciaId | UUID | Identifica la fragancia consultada. |
| nombre | String | Nombre visible en el catálogo. |
| familiaOlfativa | FamiliaOlfativa | Clasificación usada por el filtro. |
| concentracion | Concentracion | Característica de la fragancia publicada. |


## Estado real de implementación

| Caso de uso | Estado de entrega 1 |
|---|---|
| RegistrarBotellaMadre | Programado con DTOs, repositorio y pruebas. |
| CrearDecant | Programado: carga raíz, delega extracción, conserva decant y guarda raíz. |
| PublicarFragancia | Diseñado; operación de dominio `Fragancia.publicar()` programada. |
| EliminarFragancia | Programado con consulta de pedidos activos por fragancia y pruebas. |
| ConsultarCatalogo | Programado con filtro por familia y DTOs; solo publicadas/activas, sin duplicados por identidad. |
| RealizarPedido | Diseñado; raíz Pedido programada y probada. Falta la orquestación del catálogo/precio. |
| CancelarPedido | Diseñado; operación Pedido.cancelar programada y probada. |
| ConsultarMisPedidos | Diseñado; consulta de repositorio programada y probada. |

### Request 5 — EliminarFraganciaRequest

Mapea a EliminarFraganciaUseCase → Fragancia.validarEliminacion.

| Campo | Tipo | Justificación |
|---|---|---|
| fraganciaId | UUID | Identifica la referencia aromática, para consultar sus pedidos activos y localizar todas sus botellas. |

### Response 4 — DecantResponse

Mapea desde el decant y su raíz, tras CrearDecantUseCase.

| Campo | Tipo | Justificación |
|---|---|---|
| decantId | UUID | Identifica el frasco creado. |
| botellaMadreId | UUID | Permite localizar el agregado que lo conserva. |
| fraganciaId | UUID | Identifica la fragancia heredada; útil para relacionar pedidos. |
| nombreFragancia | String | Confirma al usuario qué aroma se fraccionó. |
| concentracion | Concentracion | Expone el valor heredado, nunca elegido por el cliente. |
| volumenMl | int | Confirma la cantidad del decant. |
| volumenDisponibleMl | int | Informa el inventario posterior, incluso cero. |

### Response 5 — EliminarFraganciaResponse

| Campo | Tipo | Justificación |
|---|---|---|
| fraganciaId | UUID | Confirma qué referencia se retiró. |
| botellasEliminadas | int | Confirma cuántos registros de inventario se eliminaron. |

## Mapeo de una línea de pedido

`LineaPedido.crear(id, itemId, fraganciaId, descripcion, precio, cantidad)` necesita tanto la identidad del artículo como la de su fragancia. En una futura API se resuelven desde inventario: el cliente no decide qué fragancia ni precio corresponden al artículo. Los tests crean estas líneas directamente para probar el dominio sin implementar todo el checkout.

Los DTOs no contienen validación de negocio. Las reglas de volumen, publicación, precio, estados y eliminación están en entidades/VO; los casos de uso validan la presencia del Request, cargan, delegan y guardan.
