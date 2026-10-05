# Casos de uso y DTOs — Pure Obsession

## Casos de uso (application/usecase)

Los casos de uso solo orquestan: buscan en el Repository, llaman a la operación del dominio y guardan. Las reglas de negocio viven en el dominio, no aquí.

### Vendedor

| # | Caso de uso | Qué hace | Repository que necesita | Operación de dominio / regla |
|---|---|---|---|---|
| 1 | RegistrarBotellaMadreUseCase | Registra una botella original con su fragancia, concentración y pirámide olfativa | BotellaMadreRepository | Fragancia.crear (nombre y familia obligatorios), PiramideOlfativa (Regla 5), Volumen (mayor a 0) y BotellaMadre.crear |
| 2 | CrearDecantUseCase | Extrae un decant de una botella madre | BotellaMadreRepository | Decant.crearDesdeBotellaMadre (Reglas 1, 2 y 6) |
| 3 | PublicarFraganciaUseCase | Publica una fragancia en el catálogo | BotellaMadreRepository | Fragancia.publicar (Reglas 4 y 5) |
| 4 | EliminarFraganciaUseCase | Elimina físicamente la fragancia si no está en pedidos activos; si lo está, la desactiva | BotellaMadreRepository, PedidoRepository | PedidoRepository.existePedidoActivoConItem (Regla 8) |

> `Fragancia.desactivar()` retira la fragancia del catálogo y evita que vuelva a publicarse. La eliminación física sigue pendiente de un mapeo explícito entre `LineaPedido.itemId` y su `Fragancia`: actualmente el pedido solo conoce el identificador del artículo (Decant, Perfume o Tester), no el de la fragancia.

### Comprador

| # | Caso de uso | Qué hace | Repository que necesita | Operación de dominio / regla |
|---|---|---|---|---|
| 5 | ConsultarCatalogoUseCase | Lista las fragancias publicadas (filtros por familia, concentración) | BotellaMadreRepository | Solo lectura |
| 6 | RealizarPedidoUseCase | Crea un pedido con uno o más ítems | PedidoRepository, BotellaMadreRepository | Pedido.crear (invariantes del agregado Pedido) |
| 7 | CancelarPedidoUseCase | Cancela un pedido que aún no fue enviado | PedidoRepository | Pedido.cancelar |
| 8 | ConsultarMisPedidosUseCase | Lista los pedidos de un cliente | PedidoRepository | PedidoRepository.listarPorCliente |

## DTOs (application/dto)

Los DTOs solo transportan datos entre el exterior y los casos de uso. Ninguno trae campos que el dominio deba calcular o decidir (ids generados, precios, totales, herencia de fragancia).

### Requests

#### Request 1 — CrearDecantRequest
Mapea a: CrearDecantUseCase → Decant.crearDesdeBotellaMadre

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

No incluye precio: el precio se toma del catálogo al crear la línea. Si viniera del cliente, cualquiera podría manipular el total.

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
