# Pure Obsession

## Evolución actual: arquitectura hexagonal

El trabajo actual incorpora puertos de entrada/salida, adaptadores REST y JPA, H2 local, registro de clientes/vendedores, sesiones, presentaciones con precios propios y revisión de certificados.

- [Arquitectura, reglas y alcance actual](docs/arquitectura-hexagonal.md).
- [Ejecutar la API, configuración y ejemplos](docs/api-local.md).

Desde `Ecomerce-PureObsession`: `.\gradlew.bat test --no-daemon` para verificar y `.\gradlew.bat bootRun` para arrancar. Requiere JDK 21.

Los apartados siguientes conservan la documentación de la entrega 1 y su alcance histórico. Para la implementación actual, consultar los enlaces anteriores.

## Pruebas avanzadas

Esta entrega incorpora pruebas de persistencia, seguridad, integración y errores HTTP sobre el backend real. Todas se encuentran bajo `Ecomerce-PureObsession/src/test/java` y se ejecutan con H2; no requieren MariaDB ni servicios externos.

| Punto solicitado | Evidencia implementada |
| --- | --- |
| 0. Repositorio (mínimo 2) | `PedidoRepositoryPruebasAvanzadasTest` guarda/recupera un pedido y verifica el ranking de fragancias con `JOIN`, `GROUP BY` y orden descendente. |
| 1. Endpoint seguro | `PruebasHttpAvanzadasTest` valida 401 anónimo, 403 para VENDEDOR y creación autorizada para ADMIN con CSRF. |
| 2. Slice JPA | `PedidoRepositoryDataJpaPruebaAvanzadaTest` usa `@DataJpaTest` e importa `PedidoJpaAdapter`. |
| 3. Integración | `PruebasHttpAvanzadasTest` registra una botella desde HTTP y la recupera mediante el repositorio real. |
| 4. Errores de controller | `InventarioControllerErroresTest` cubre 404, 422 y petición inválida sin invocar el caso de uso. |

### 1. Seguridad de endpoints

La clase `PruebasHttpAvanzadasTest` arranca la aplicación completa y usa la cadena de seguridad real.
Se eligió `POST /api/inventario/botellas`, porque la configuración la restringe al rol ADMIN.
Sin sesión, `GET /api/auth/yo` devuelve 401: el servidor no tiene una identidad autenticada.
Con sesión VENDEDOR y CSRF válido, el POST devuelve 403: conoce al usuario, pero el rol no alcanza.
Con sesión ADMIN, el mismo POST llega al controller y devuelve 201 porque crea una botella madre.
El 201 es el éxito REST correcto para crear un recurso; la guía lo resume como un éxito 200.
Los postprocesadores `user(...).roles(...)` y `csrf()` simulan la sesión y el token que Spring espera.

### 2. `@DataJpaTest` y `@SpringBootTest`

`@DataJpaTest` carga el slice de persistencia y una H2 temporal, por eso inicia más rápido que toda la aplicación.
En Spring Boot 4 se importa desde `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`.
El slice no registra automáticamente el adaptador propio, así que el test declara `@Import(PedidoJpaAdapter.class)`.
La prueba consulta pedidos activos por fragancia y confirma que cancelar el último pedido activo cambia el resultado.
El ranking de fragancias se prueba contra H2 con una consulta `JOIN` y `GROUP BY`, verificando cantidad y orden.
`@SpringBootTest` sí carga controller, seguridad, casos de uso y persistencia; por eso detecta errores entre capas.
Las pruebas transaccionales revierten sus datos al terminar para que un caso no contamine al siguiente.

### 3. Integración

La prueba de integración envía un POST válido para registrar una botella madre como ADMIN.
La solicitud pasa por el controller, las validaciones, el caso de uso, el adaptador JPA y la base H2.
Después de recibir 201, el test consulta `BotellaMadreRepository` real y verifica nombre y volumen persistidos.
No hay `@MockitoBean` en este flujo, por lo que ningún componente de negocio o persistencia está simulado.
Es más lenta que una unitaria, pero puede descubrir problemas de mapeo, transacción o configuración que un mock oculta.
Los casos de borde se mantienen en pruebas unitarias y de controller para conservar la suite rápida.
`InventarioVentaTestConfig` existe únicamente en `src/test`; permite iniciar el contexto mientras se integra el adaptador transaccional definitivo de inventario.

### Errores y protección de persistencia

`InventarioControllerErroresTest` usa `@WebMvcTest` con filtros desactivados para concentrarse en la capa HTTP.
Cuando el caso de uso informa un recurso inexistente, `RecursoNoEncontradoException` se convierte en 404 mediante `ErroresController`.
Cuando se rompe una regla de negocio, `ReglaDominioException` conserva la respuesta 422 del proyecto.
Una solicitud vacía responde 400 por validación y se verifica que el caso de uso nunca se invocó.
`PresentacionesUseCaseNoGuardarTest` confirma con `verify(..., never())` que un recurso inexistente no llama a `guardar`.

### Ejecutar y evidenciar

Desde la carpeta `Ecomerce-PureObsession`, ejecutar:

```powershell
.\gradlew.bat test
```

El resultado esperado es `BUILD SUCCESSFUL`. El reporte para revisar o capturar está en `Ecomerce-PureObsession/build/reports/tests/test/index.html`.

# Entrega 1 (histórico)

Marketplace de perfumería: botellas madre, decants, perfumes y testers. Esta entrega demuestra modelado de dominio, agregados e invariantes, no un comercio electrónico terminado.

## Guía de revisión

1. [Nicho y lenguaje ubicuo](glosario-lenguaje-ubicuo.md).
2. [Ocho reglas de negocio y su evidencia](docs/reglas-de-negocio.md).
3. [Clasificación de todos los conceptos con las tres pruebas](docs/clasificacion-entidades-y-value-objects.md).
4. [Dos agregados, diagramas, límites e invariantes](docs/diagrama-agregado.md).
5. [Ocho casos de uso y DTOs justificados](docs/casos-de-uso-y-dtos.md).
6. [Checklist y sustentación](docs/entrega1-checklist.md).
7. [Verificación ejecutada y resultados](docs/verificacion-entrega1.md).

## Ejecutar las pruebas

Requisito: **JDK 21**, JAVA_HOME apuntando a ese JDK y conexión para descargar Gradle/dependencias la primera vez.

Desde la raíz del repositorio, en Git Bash/Linux/macOS:

```bash
cd Ecomerce-PureObsession
./gradlew clean test
```

En PowerShell:

```powershell
cd Ecomerce-PureObsession
.\gradlew.bat clean test
```

El resultado esperado es `BUILD SUCCESSFUL`. Informe HTML: `Ecomerce-PureObsession/build/reports/tests/test/index.html`. Las pruebas de entrega 1 no necesitan MariaDB ni levantar el contexto Spring. No confundir `test` con arrancar la aplicación web, cuya infraestructura es posterior.

## Estructura

- `domain/entity`: entidades, raíces e invariantes.
- `domain/valueObject`: records y enums. Se conserva el nombre de paquete existente para evitar cambios de imports innecesarios.
- `domain/repository`: contratos de persistencia de las dos raíces.
- `application/dto` y `application/usecase`: transporte y orquestación.
- `infrastructure/persistence`: implementaciones HashMap, sin persistencia entre ejecuciones.
- `src/test`: pruebas de dominio, casos de uso y repositorios, sin Spring/JPA dentro del dominio.

## Cambios de API en esta revisión

- `CrearDecantUseCase` ahora recibe BotellaMadreRepository y expone `ejecutar(CrearDecantRequest)`; se eliminó la confirmación ficticia sin inventario.
- `LineaPedido.crear` incorpora `fraganciaId` después de `itemId`. Las llamadas y pruebas del repositorio están actualizadas.
- `getVolumenDisponible()` devuelve VolumenDisponible, con el mismo accessor `mililitros()`, y permite inventario agotado. Volumen sigue siendo estrictamente positivo.
- Decants se conservan dentro de BotellaMadre. No se necesita un tercer repositorio.

## Organización y alcance

Se conserva el historial real, sin modificar fechas ni autores anteriores. Según el documento de estado aportado por el equipo, la docente autorizó ramas por persona. La rama de revisión es `Deivid-Canan-Branch`; cualquier integración a main se realiza después de revisar las pruebas.

Las implementaciones en memoria están pensadas para demostraciones secuenciales. Autenticación, UI, pagos, concurrencia y base de datos no son requisitos de entrega 1. Los casos de uso solo diseñados están identificados como tales.
