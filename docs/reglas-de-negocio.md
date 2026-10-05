# Reglas de negocio — Pure Obsession

Nicho: marketplace de perfumería con presentaciones completas, testers y decants. Un decant permite probar una fragancia sin comprar la presentación completa.

| Regla innegociable | Protección en código | Evidencia |
|---|---|---|
| R1. Nunca extraer más mililitros de los disponibles. El disponible puede llegar a cero. | BotellaMadre.validarExtraccion y VolumenDisponible | BotellaMadreInvarianteTest; CrearDecantUseCaseTest |
| R2. Un decant siempre tiene volumen positivo y menor que el de la botella original. | Volumen; BotellaMadre.validarExtraccion | BotellaMadreInvarianteTest; VolumenTest |
| R3. Un tester nunca admite envoltura de regalo. | Tester.aplicarEnvolturaRegalo | TesterTest |
| R4. Una fragancia publicada siempre tiene concentración definida. Un borrador puede carecer de ella. | Fragancia.publicar | FraganciaTest |
| R5. Una fragancia publicada siempre tiene pirámide olfativa con salida, corazón y fondo. | PiramideOlfativa; Fragancia.publicar | Pruebas de pirámide/publicación |
| R6. El decant hereda la fragancia y concentración de su botella; no las recibe en el Request. | BotellaMadre.crearDecant; Decant | CrearDecantUseCaseTest |
| R7. Solo el comprador de un pedido entregado puede crear una reseña verificada de ese pedido. | Resena.crear; Pedido.permiteResena | ResenaTest; ValidacionAgregadosTest |
| R8. Nunca eliminar físicamente una fragancia ni su inventario si está referenciada por pedidos activos. Puede desactivarse para retirarla del catálogo sin borrar el historial. | Fragancia.validarEliminacion; Pedido.contieneFragancia; EliminarFraganciaUseCase | EliminarFraganciaUseCaseTest; PedidoRepositoryEnMemoriaTest |

## Políticas complementarias

- Una fragancia inactiva no puede publicarse ni usarse para nuevos decants.
- Los pedidos no mezclan monedas, no aceptan líneas nulas/duplicadas ni cantidades no positivas.
- La cancelación solo se permite antes del envío.
- El precio no puede ser nulo, negativo, NaN o infinito; la moneda es un código ISO 4217 válido normalizado.
- Todas las identidades son obligatorias. Los errores de datos/reglas del dominio usan ReglaDominioException.

Las ocho reglas están implementadas en el dominio. Cuatro casos de uso están programados; los restantes están diseñados conforme al mínimo de la rúbrica. No se afirma que exista un e-commerce completo con pagos, autenticación o pantallas.
