# Ejecutar y probar la API local

Requisito: JDK 21. Desde `Ecomerce-PureObsession`, ejecutar:

```powershell
.\gradlew.bat bootRun
```

La API escucha por defecto en `http://localhost:8080`. H2 se configura automáticamente y la consola web de H2 no se expone.

Para crear el primer administrador, definir antes del arranque `APP_ADMIN_EMAIL`, `APP_ADMIN_CLAVE` y `APP_ADMIN_CEDULA`. Utilizar datos y una clave propios; no guardarlos en Git. El administrador solo se crea si el correo no existe. Quitar esas variables después del primer arranque. Si no se configuran, el registro de clientes y vendedores sigue disponible, pero no existe un administrador con contraseña predeterminada.

## Sesiones y CSRF

Usar Postman u otro cliente que conserve cookies. Primero llamar a `GET /api/auth/csrf`. La respuesta entrega `headerName` y `token`; enviar ese encabezado en cada POST o DELETE, incluido registro y login, conservando la cookie de sesión. Después de entrar, volver a obtener el token CSRF porque se renueva la sesión. El cierre de sesión es POST, también con token.

## Endpoints

| Método y ruta | Acceso | Función |
|---|---|---|
| GET `/api/auth/csrf` | Público | Obtener token CSRF |
| POST `/api/auth/registro` | Público con CSRF | Registrar cliente o vendedor |
| POST `/api/auth/entrar` | Público con CSRF | Iniciar sesión con email y clave |
| GET `/api/auth/yo` | Sesión | Consultar la cuenta actual |
| POST `/api/auth/salir` | Sesión y CSRF | Cerrar sesión |
| POST `/api/inventario/botellas` | ADMIN | Registrar botella y fragancia |
| POST `/api/inventario/decants` | ADMIN | Extraer un decant y descontar ml |
| DELETE `/api/inventario/fragancias/{id}` | ADMIN | Eliminar inventario si no hay pedidos activos |
| POST `/api/presentaciones` | VENDEDOR | Crear presentación y precio |
| POST `/api/presentaciones/{id}/certificado` | Vendedor propietario | Multipart con parte `archivo` |
| GET `/api/presentaciones/{id}/certificado` | Propietario o ADMIN | Descargar documento y consultar encabezado `X-Estado-Certificado` |
| POST `/api/presentaciones/{id}/revision` | ADMIN | Aprobar o rechazar certificado |
| POST `/api/presentaciones/{id}/publicacion` | Vendedor propietario | Publicar presentación y fragancia |
| POST `/api/presentaciones/{id}/empaque/validacion` | Sesión | Validar empaque elegido |
| GET `/api/catalogo` | Público | Presentaciones publicadas con precio |
| GET `/api/catalogo/fragancias?familia=FLORAL` | Público | Consulta de fragancias |
| POST `/api/pedidos` | Sesión y CSRF | Crea un pedido y reserva el volumen de las presentaciones seleccionadas |
| GET `/api/pedidos/mios` | Sesión | Lista los pedidos de la cuenta actual |
| GET `/api/pedidos/{id}` | Sesión | Consulta un pedido autorizado |
| POST `/api/pedidos/{id}/cancelacion` | Sesión y CSRF | Cancela un pedido `CREADO` y libera su reserva de inventario |

Todas las mutaciones requieren CSRF. Los errores de formato/campos devuelven 400, credenciales inválidas 401, permisos o CSRF 403, duplicados detectados por BD 409, reglas de negocio 422 y archivos demasiado grandes 413.

## Ejemplos de requests

Registro (la clave siguiente es ilustrativa y no provisiona una cuenta por sí sola):

```json
{"nombre":"Persona de prueba","cedula":"1234567890","email":"persona@example.com","clave":"CambiarEstaClave2026","rol":"VENDEDOR"}
```

Entrada:

```json
{"email":"persona@example.com","clave":"CambiarEstaClave2026"}
```

Botella, desde la sesión ADMIN:

```json
{"nombreFragancia":"Aroma floral","familiaOlfativa":"FLORAL","concentracion":"EAU_DE_PARFUM","notasSalida":"Bergamota","notasCorazon":"Rosa","notasFondo":"Cedro","volumenMl":200}
```

La respuesta incluye `botellaMadreId` y `fraganciaId`. Usarlos en una presentación desde la sesión del vendedor:

```json
{"fraganciaId":"UUID-DE-LA-RESPUESTA","botellaMadreId":"UUID-DE-LA-RESPUESTA","tipo":"DECANT","volumenMl":50,"precio":95000,"origen":"IMPORTADO","paisOrigen":"FR","original":true}
```

Subir certificado, revisarlo desde la sesión ADMIN con `{"aprobado":true}` y publicar desde la sesión del vendedor. La aprobación requiere inspeccionar el documento; no basta con recibirlo. Para validar empaque usar `{"tipo":"REGALO"}` o `{"tipo":"ESTANDAR"}`. Una presentación TESTER rechaza REGALO.

## Preparación futura de MariaDB

Establecer `SPRING_PROFILES_ACTIVE=mariadb`, `DB_URL=jdbc:mariadb://...`, `DB_USERNAME` y `DB_PASSWORD` solo después de crear/migrar el esquema. El perfil está preparado, pero esta entrega se verifica con H2.
