# Pure Obsession

## Evolución actual: arquitectura hexagonal

El trabajo actual incorpora puertos de entrada/salida, adaptadores REST y JPA, H2 local, registro de clientes/vendedores, sesiones, presentaciones con precios propios y revisión de certificados.

- [Arquitectura, reglas y alcance actual](docs/arquitectura-hexagonal.md).
- [Ejecutar la API, configuración y ejemplos](docs/api-local.md).

Desde `Ecomerce-PureObsession`: `.\gradlew.bat test --no-daemon` para verificar y `.\gradlew.bat bootRun` para arrancar. Requiere JDK 21.

Los apartados siguientes conservan la documentación de la entrega 1 y su alcance histórico. Para la implementación actual, consultar los enlaces anteriores.

## Pruebas avanzadas

### 1. Seguridad de endpoints

La seguridad se prueba con el contexto completo para ejecutar la cadena real de Spring Security.
Una petición anónima a una ruta privada responde 401 porque no tiene identidad autenticada.
La operación de inventario responde 403 cuando la sesión tiene el rol VENDEDOR.
Con ADMIN y un token CSRF válido, la misma solicitud recibe 201 y llega al caso de uso real.
El postprocesador de usuario y roles representa la sesión que recibe cada petición.
    Así se distingue la autenticación de la autorización basada en roles.

### 2. DataJpaTest y SpringBootTest

DataJpaTest carga JPA y H2, por eso sirve para revisar el adaptador de pedidos rápidamente.
El adaptador se importa de forma explícita porque un slice no registra toda la infraestructura.
Cada prueba del slice se ejecuta en transacción y sus datos se revierten al terminar.
SpringBootTest también carga controladores, seguridad y casos de uso, por lo que tarda más.
Esa carga completa permite detectar errores de conexión entre capas que el slice no muestra.
Usamos el slice para persistencia y el contexto completo para los flujos HTTP importantes.

### 3. Integración

La prueba de integración registra una botella por HTTP y cruza controller, caso de uso, JPA y H2.
No usa Mockito: después de la respuesta consulta el repositorio real para confirmar el guardado.
La transacción de prueba evita que el escenario deje datos para la siguiente ejecución.
Es más lenta que una unitaria, pero descubre fallos de mapeo y configuración reales.
No conviene usarla para todos los bordes porque esos casos son más rápidos con pruebas unitarias.
El soporte de inventario de test solo permite iniciar el contexto mientras llega su adaptador transaccional.

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
