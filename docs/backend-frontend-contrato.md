# Backend para conectar el frontend

Contrato actualizado en noviembre de 2026. Este documento describe la ampliacion del
marketplace; las rutas de la Entrega 2 permanecen compatibles. Los importes JSON
siguen siendo numeros, ahora calculados/persistidos con BigDecimal de dos decimales.
Las rutas nuevas de tienda usan `/api/productos`, no la lista resumida antigua.

## Arranque

Desde `Ecomerce-PureObsession`, con JDK 21:

```powershell
.\gradlew.bat test --no-daemon
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

Swagger: `http://localhost:8080/swagger-ui/index.html`.
Base de la API: `http://localhost:8080/api`.
El primer arranque descarga la dependencia de correo SMTP ademas de las anteriores.
Los cuatro usuarios demo conservan la clave `DemoPure2026!`:
`admin@pureobsession.demo`, `vendedor@pureobsession.demo`,
`vendedor2@pureobsession.demo`, `cliente@pureobsession.demo`.

El perfil demo usa una H2 separada. Fuera de demo no se cargan cuentas de muestra
ni existe el controlador de pago simulado. No hay conexion a PSE, tarjetas o Nequi:
el frontend debe indicar pago de demostracion o pago manual, segun el perfil.

## Autenticacion, errores y CORS

- `POST /auth/registro`: nombre, cedula, email, clave, rol CLIENTE o VENDEDOR.
- `POST /auth/entrar`: email, clave. Respuesta: token, tipo Bearer y expiraEn.
- `GET /auth/yo`: cuenta basica. `GET /perfil`: perfil completo del usuario actual.
- Cabecera para peticiones autenticadas: `Authorization: Bearer <token>`.
- Al expirar el JWT, iniciar sesion de nuevo. Cerrar sesion en el frontend elimina
  su token. Cambiar/restablecer la clave invalida los tokens anteriores en el servidor.
- CORS permite localhost:5173 y localhost:3000; configurar `FRONTEND_ORIGENES`
  con los origenes reales separados por coma. No usa cookies ni allowCredentials.
- Errores: 400 formato/campos; 401 token; 403 rol/propiedad; 404 inexistente;
  409 duplicado/conflicto; 422 regla de negocio; 413 archivo grande;
  503 servicio de correo no disponible. JSON: mensaje y campos.

## Datos para los disenos

Se reviso la carpeta compartida de Drive: `stitch_pureobssesion_luxury_perfume_marketplace`.
Se contrastaron los HTML de publicar fragancia, checkout, perfil, tienda y ficha
de botella, junto con los nombres de las pantallas de carrito, favoritos y vendedor.
Los precios, puntuaciones y badges del HTML son ejemplos visuales; el frontend
debe usar los datos reales de la API. No inventar ventas, verificacion ni reseñas.

| Pantalla | API |
| --- | --- |
| Inicio y catalogo | GET /productos; GET /catalogo/ranking; GET /opciones |
| Ficha y formatos | GET /productos/{id}; GET /productos?fraganciaId=...&vendedorId=... |
| Tienda publica | GET /tiendas; GET /tiendas/{id}; GET /tiendas/{id}/reputacion |
| Favoritos | GET /favoritos; PUT y DELETE /favoritos/{productoId} |
| Carrito | GET /carrito; PUT/DELETE /carrito/items/{productoId}; DELETE /carrito |
| Checkout | POST /checkout/cotizacion; POST /checkout |
| Mis pedidos | GET /pedidos/mios; GET /pedidos/{id}/seguimiento |
| Perfil y direcciones | GET/PUT /perfil; GET/POST /perfil/direcciones; PUT/DELETE /perfil/direcciones/{id} |
| Recuperar clave | POST /auth/recuperacion; POST /auth/restablecer |
| Dashboard vendedor | GET /vendedor/dashboard |
| Mis productos | GET/POST /vendedor/productos; GET/PUT /vendedor/productos/{id} |
| Inventario vendedor | GET/POST /vendedor/botellas; PUT /vendedor/productos/{id}/stock |
| Ventas y despacho | GET /vendedor/ventas; POST /vendedor/ventas/{pedidoId}/envio |
| Mi tienda | GET/PUT /vendedor/tienda |
| Administracion | GET /admin/pedidos; GET /admin/certificados |

### Catalogo y producto

`GET /productos` admite texto, familia, tipo, categoria, minimo, maximo,
vendedorId, fraganciaId, orden, pagina y tamanio. Orden: nombre, precio_asc o
precio_desc. Pagina empieza en 0; tamanio predeterminado 20 y maximo 100.
Respuesta: contenido, totalElementos, totalPaginas, pagina y tamanio.

Cada producto incluye nombre, marca, descripcion, categoria, vendedorId y
nombre del vendedor, ciudad, tipo, volumenMl, precio, moneda, stockDisponible,
familia, concentracion, notasSalida/Corazon/Fondo, imagenes, original, publicada,
certificado, calificacion y resenas. Una oferta agotada puede seguir visible con
stock cero; desactivar Agregar al carrito. Las URL de imagen son relativas a la
base del servidor, por ejemplo `/api/imagenes/{id}`.

`GET /opciones` enumera las opciones validas para formularios. Las categorias
son NICHO, DISENADOR y ARABE. Una variante se identifica por su propio UUID:
no enviar el UUID de la fragancia como producto de checkout.

### Publicacion por vendedor

Una ficha nueva puede crearse con `POST /vendedor/productos`:

```json
{
  "nombre": "Fragancia de muestra",
  "marca": "Casa de muestra",
  "descripcion": "Descripcion del perfume",
  "categoria": "NICHO",
  "familia": "FLORAL",
  "concentracion": "EAU_DE_PARFUM",
  "notasSalida": "Bergamota",
  "notasCorazon": "Jazmin",
  "notasFondo": "Almizcle",
  "tipo": "PERFUME",
  "volumenMl": 100,
  "precio": 150000.25,
  "stock": 5,
  "original": false,
  "origen": "NACIONAL",
  "paisOrigen": "CO"
}
```

La respuesta es un borrador. Para publicar usar la ruta existente
`POST /presentaciones/{id}/publicacion`. Si original=true, antes debe cargarse y
aprobarse su certificado. Carga: POST /presentaciones/{id}/certificado multipart
con campo archivo. Revision ADMIN: POST /presentaciones/{id}/revision con
`{"aprobado":true}`. Lista de pendientes: GET /admin/certificados?estado=PENDIENTE.
No se considera autenticidad verificada una mera foto o un booleano del formulario.

Para varias presentaciones de la misma fragancia, reutilizar fraganciaId en
las siguientes ofertas. Tambien puede crearse primero una base con
POST /vendedor/fragancias (nombre, familia, concentracion y las tres notas).
Para decants: crear una botella propia con POST /vendedor/botellas
`{"fraganciaId":"UUID","volumenMl":500}`, y crear la oferta tipo DECANT
con botellaMadreId y un volumen menor que la botella. El stock del decant se
calcula desde los ml; no se puede editar como unidades.

PERFUME y TESTER nuevos usan unidades independientes, sin botella madre.
PUT /vendedor/productos/{id}/stock recibe `{"disponible":5}`: reemplaza las
unidades disponibles, no las ya reservadas/vendidas. Un vendedor no puede usar
botellas ni editar ofertas de otro vendedor. El inventario antiguo sin propietario
permanece administrado de forma central para conservar la compatibilidad;
los nuevos flujos del frontend usan exclusivamente las rutas /vendedor.

En una base demo nueva las botellas de muestra ya pertenecen a sus vendedores.
Una base demo antigua conserva su inventario previo sin transferencias automaticas.
Para ensayar todo el flujo nuevo desde cero sin borrar archivos existentes, arrancar:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=demo --spring.datasource.url=jdbc:h2:mem:demo-frontend"
```

PUT /vendedor/productos/{id}: marca, descripcion, categoria, precio.
Para retirar: DELETE /vendedor/productos/{id}/publicacion. No destruye historial.
Imagenes: POST /vendedor/productos/{id}/imagenes, multipart archivo. Maximo seis,
PNG/JPEG de hasta 5 MB y 4096 x 4096. DELETE en /imagenes/{fotoId} bajo el mismo
producto las retira logicamente. El propietario puede ver las fotos del borrador
enviando su JWT; solo se sirven anonimamente si el producto esta publicado.
WEBP/15 MB del boceto no estan habilitados: el frontend debe reflejar el limite real.

### Carrito, cotizacion y pedido

PUT /carrito/items/{id} recibe `{"cantidad":2}`. Las selecciones y favoritos son
privados y persistentes. El carrito no reserva stock. Sus precios se recalculan;
la disponibilidad se valida de nuevo en checkout. El invitado puede mantener un
carrito local y enviar sus items directamente; nunca enviar precios del navegador.

POST /checkout/cotizacion:

```json
{"items":[{"presentacionId":"UUID","cantidad":2,"empaque":"ESTANDAR"}]}
```

Devuelve subtotal, envio, regalo, total, moneda y vendedores. Se cobra la tarifa
configurada de envio por vendedor y la de regalo por unidad. Valores por defecto:
cero (sin cargos adicionales). Configurar ENVIO_POR_VENDEDOR y REGALO_POR_UNIDAD
si el equipo acuerda una tarifa. Los testers no admiten REGALO. La cotizacion no
garantiza stock hasta crear el pedido y no consulta una transportadora externa.

POST /checkout:

```json
{
  "nombre": "Cliente invitado",
  "correo": "cliente@ejemplo.com",
  "telefono": "3001234567",
  "direccion": "Calle 1, Barrio Centro, Armenia, Quindio. Apto 2",
  "items": [{"presentacionId":"UUID","cantidad":2,"empaque":"ESTANDAR"}]
}
```

Con JWT se toman nombre/correo de la cuenta. La direccion es un snapshot: el
frontend compone sus datos de entrega o una direccion guardada, sin cambiar
pedidos historicos al editar el perfil. La respuesta incluye total, subtotal,
envio, regalo, lineas con empaque, estado y los UUID reales.

Para usuarios autenticados enviar Idempotency-Key (crypto.randomUUID() en el
frontend). Repetir la misma peticion y clave devuelve el mismo pedido sin volver
a reservar; cambiar datos con esa clave da 409. Usar una clave nueva para una
compra nueva. Invitados no usan esa cabecera: su claveConsulta se revela solo al
crear el pedido y debe conservarse para consultar/cancelar/pagar demo.

Tras creacion exitosa, vaciar el carrito y mostrar confirmacion. Las reservas
vencen a los 30 minutos. Un proceso cada minuto cancela pedidos CREADOS vencidos
y libera sus existencias una sola vez. GET /pedidos/{id}/seguimiento muestra el
vencimiento, el motivo de cancelacion, pago y envios. Para invitados requiere
X-Consulta-Pedido. El vencimiento tambien impide confirmar un pago tardio.

### Pago, ventas y envios

- Solo demo: POST /demo/pedidos/{id}/pago, con JWT del comprador o clave de invitado.
  La respuesta marca pago.simulado=true. Reintentar no registra otro pago.
- Manual: ADMIN revisa el pago fuera de la app y hace POST
  /admin/pedidos/{id}/confirmacion con `{"referencia":"RECIBO-UNICO"}`.
  El servidor fija el importe; rechaza referencias repetidas en otro pedido.
- Se crea un envio por vendedor. Cada vendedor consulta /vendedor/ventas y
  recibe exclusivamente sus lineas, subtotal y datos necesarios de entrega.
- POST /vendedor/ventas/{pedidoId}/envio con transportadora y guia registra
  solamente el envio del vendedor autenticado.
- El comprador confirma recepcion con POST
  /pedidos/{pedidoId}/envios/{vendedorId}/recepcion. Invitados usan su clave.
- Pedido global ENVIADO cuando todos despacharon; ENTREGADO cuando todos
  los envios fueron recibidos. Antes puede haber envios entregados y otros pendientes.
- Cancelacion existente: POST /pedidos/{id}/cancelacion solo para CREADO.
  Una devolucion despues de pagar requiere gestion manual, no se simula un reembolso.

Las reseñas se crean/actualizan con PUT /productos/{id}/resena, puntuacion de 1
a 5 y comentario de hasta 2000 caracteres. Requieren una compra ENTREGADA de
ese producto y cuenta registrada. GET /productos/{id}/resenas devuelve las 100
mas recientes. Reputacion de tienda y dashboard se calculan con datos reales.

### Perfil, direcciones y recuperacion

PUT /perfil: nombre, telefono, nacimiento (AAAA-MM-DD opcional). El email y rol
no se editan por esta ruta. PUT /perfil/clave: actual y nueva; obliga a volver a
iniciar sesion. La clave conserva el limite BCrypt de 10 caracteres a 72 bytes.

Direcciones: etiqueta, receptor, telefono, departamento, ciudad, direccion,
indicaciones y predeterminada. Maximo diez; solo una predeterminada. Al borrar
la principal se elige otra. No se pueden editar direcciones de otra cuenta.

POST /auth/recuperacion con email responde un mensaje generico. Solo en demo
incluye tokenDemo para ensayar el formulario de restablecimiento. Fuera de demo
manda un enlace por SMTP y nunca devuelve el token. El token se guarda como
hash, vence en 15 minutos y es de un solo uso; se limita a uno nuevo por minuto.
POST /auth/restablecer: token y nueva. Invalida JWT anteriores. Sin SMTP fuera
de demo devuelve 503, sin fingir envio ni revelar si la cuenta existe.

## Configuracion y limites de despliegue

Configurar en el entorno del proceso (Spring no carga un archivo .env por si solo):

- JWT_SECRETO: secreto persistente de al menos 32 bytes fuera de la demo local.
- FRONTEND_ORIGENES: lista exacta de origenes; RECUPERACION_URL: URL del formulario.
- CORREO_DESDE y SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD; habilitar SMTP auth/TLS
  con SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH y SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE.
- ENVIO_POR_VENDEDOR / REGALO_POR_UNIDAD: importes COP con dos decimales.

El entorno verificado para desarrollar el frontend es H2. Sus perfiles locales
actualizan el esquema; respaldar archivos H2 antes de actualizar una base con
datos importantes. MariaDB sigue usando ddl-auto=validate: el despliegue requiere
aplicar y verificar migraciones de las tablas nuevas, timestamps, propiedad de
botellas y conversion de linea_pedido.precio a DECIMAL(14,2). No se ejecutaron
migraciones destructivas ni pruebas contra la base del usuario.

Para publicar comercialmente faltan configuracion del proveedor SMTP, HTTPS,
limites de trafico en el gateway, copias de seguridad y verificacion del despliegue
sobre su base final. Pagos reales, calculo de tarifa por transportadora, facturacion
tributaria y reembolsos automaticos son integraciones externas que no estan
implementadas. Esto no impide desarrollar y ensayar el frontend del MVP acordado.

## Evidencia de verificacion

La suite ampliada contiene 112 pruebas (92 anteriores y 20 nuevas), incluyendo
stock concurrente, rollback de reservas, idempotencia, dos vendedores, permisos,
recuperacion/revocacion de JWT, CORS, imagenes y contratos fuera del perfil demo.
Las pruebas de semilla se ejecutaron de nuevo despues de separar las botellas demo.

Ademas se arranco la aplicacion con Tomcat en 18085 y una H2 en memoria. Se
verificaron Swagger 200, OpenAPI, inventario propio, creacion y publicacion de un
perfume, checkout repetido sin duplicacion, pago demo, despacho y recepcion final
ENTREGADO. El importe de prueba fue 246913.56 COP. Se apago el servidor temporal
al terminar; no se uso la base local del usuario.
