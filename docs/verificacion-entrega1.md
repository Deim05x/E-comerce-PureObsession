# Verificación de la entrega 1

Fecha: 5 de octubre de 2026. Revisión sobre Deivid-Canan-Branch, partiendo de efda5ea.

Se compiló el proyecto completo y se ejecutó la suite con JDK 21 y Gradle 9.7.1 (la misma versión declarada por el wrapper), mediante `gradle --no-daemon clean test`. Resultado: **BUILD SUCCESSFUL**.

**42 pruebas ejecutadas; 0 fallos; 0 errores; 0 omitidas.**

| Clase | Pruebas | Fallos | Errores | Omitidas |
|---|---:|---:|---:|---:|
| ConsultarCatalogoUseCaseTest | 2 | 0 | 0 | 0 |
| CrearDecantUseCaseTest | 4 | 0 | 0 | 0 |
| EliminarFraganciaUseCaseTest | 2 | 0 | 0 | 0 |
| RegistrarBotellaMadreUseCaseTest | 2 | 0 | 0 | 0 |
| BotellaMadreInvarianteTest | 2 | 0 | 0 | 0 |
| FraganciaTest | 5 | 0 | 0 | 0 |
| PedidoInvarianteTest | 5 | 0 | 0 | 0 |
| ResenaTest | 2 | 0 | 0 | 0 |
| TesterTest | 2 | 0 | 0 | 0 |
| ValidacionAgregadosTest | 7 | 0 | 0 | 0 |
| PrecioTest | 2 | 0 | 0 | 0 |
| ValidacionValoresTest | 3 | 0 | 0 | 0 |
| VolumenTest | 2 | 0 | 0 | 0 |
| PedidoRepositoryEnMemoriaTest | 2 | 0 | 0 | 0 |

## Qué demuestra

- Cumplimiento de la distribución mínima: VO, entidades y al menos dos invariantes por cada raíz.
- Creación de decants con persistencia en su botella, herencia aromática y rechazo sin alterar inventario.
- Agotamiento exacto del volumen disponible, diferenciándolo del volumen solicitado positivo.
- Eliminación bloqueada por pedidos activos de la fragancia y conservación de historial tras cancelar.
- Restricciones de publicación, testers, reseñas, moneda y estados.
- Implementación HashMap de pedidos con consulta por cliente, artículo y fragancia.

Las pruebas no certifican concurrencia, UI, pagos ni persistencia duradera: no forman parte de esta entrega. La sustentación depende de que el equipo comprenda y explique el modelo.

Para repetir en el equipo del estudiante: entrar a Ecomerce-PureObsession y ejecutar `./gradlew clean test` (Git Bash) o `.\gradlew.bat clean test` (PowerShell), con JDK 21. El informe HTML se genera en `build/reports/tests/test/index.html`.
