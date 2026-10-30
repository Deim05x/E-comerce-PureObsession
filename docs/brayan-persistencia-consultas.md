# Bloque de Brayan: persistencia, consultas y demo

## Ejecutar la demo

Requiere JDK 21. Desde `Ecomerce-PureObsession`:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

Swagger: `http://localhost:8080/swagger-ui/index.html`.
El perfil `demo` usa H2 en `./data/pureobsession-demo`, separado de la base H2
habitual. `DatosDemoInicializador` es el inicializador equivalente a `data.sql`:
usa las fabricas de dominio, BCrypt y las mismas reservas que el checkout.
Solo se activa con `demo`. Los perfiles H2 habitual y MariaDB no cargan semilla.

| Correo demo | Rol |
| --- | --- |
| admin@pureobsession.demo | ADMIN |
| vendedor@pureobsession.demo | VENDEDOR |
| vendedor2@pureobsession.demo | VENDEDOR |
| cliente@pureobsession.demo | CLIENTE |

Clave de estas cuentas de demostracion: `DemoPure2026!`. La base almacena el
hash BCrypt generado, no la clave. Son cuentas exclusivas del perfil demo.
La carga es atomica e idempotente: reiniciar no duplica cuentas/pedidos ni
restaura volumen que ya se haya vendido o retirado. Para repetir desde cero,
usar una URL H2 nueva y vacia mediante `--spring.datasource.url=jdbc:h2:mem:otra-demo`.

La semilla contiene tres fragancias, tres botellas de 500 ml, cuatro ofertas de
dos vendedores y cinco pedidos con estados distintos. Son productos ficticios
no anunciados como originales. Se mantiene la decision actual de inventario
central, sin incorporar propiedad de botellas por vendedor en este bloque.

## Contratos de consulta

Las rutas nuevas son publicas y conservan las rutas anteriores de lista.

| GET | Parametros | Respuesta |
| --- | --- | --- |
| `/api/catalogo/pagina` | texto, familia, pagina, tamanio | Pagina de presentaciones |
| `/api/catalogo/fragancias/pagina` | texto, familia, pagina, tamanio | Pagina de fragancias |
| `/api/catalogo/ranking` | limite | Lista de fragancias y cantidadVendida |

Ejemplo: `/api/catalogo/pagina?texto=rosa&familia=FLORAL&pagina=0&tamanio=1`.

`Pagina` devuelve `contenido`, `totalElementos`, `totalPaginas`, `pagina` y
`tamanio`. La primera pagina es 0, el tamano predeterminado es 20 y el maximo
es 100. Una pagina fuera del resultado devuelve contenido vacio y conserva
los totales; una busqueda vacia tiene cero paginas. Parametros fuera de rango
devuelven 422; enum o numero mal formado devuelve 400.

La busqueda por nombre ignora mayusculas y espacios exteriores. `%`, `_` y `!`
se buscan literalmente. El filtro de familia se aplica junto con el texto.
`ConsultaCatalogoJpaAdapter` usa JPQL, `count`, `setFirstResult` y `setMaxResults`;
no carga toda la tabla para paginar. Ni `Pagina` ni los puertos importan Spring.
El orden es nombre y UUID; presentaciones desempata tambien por su UUID.
Varias botellas de la misma fragancia no duplican la pagina de fragancias.

El ranking une pedidos con sus lineas y fragancias, agrupa por fragancia y suma
`cantidad`. Cuenta unidades de presentaciones, no volumen ni importe. Incluye
CONFIRMADO, ENVIADO y ENTREGADO; excluye CREADO y CANCELADO. Excluye fragancias
eliminadas y desempata por nombre y UUID. `limite` admite 1 a 100 (por defecto 10).
La semilla inicial devuelve **Rosa de Jardin: 5**, **Cedro Nocturno: 2**.

## Borrado logico y mappers

`BotellaJpa.eliminada` conserva la fila, los decants y las referencias historicas.
Al retirar la ultima botella se marca tambien `FraganciaJpa.eliminada`, y se
desactiva y despublica la fragancia. Retirar una de varias botellas no retira las
demas; las ofertas vinculadas a la botella retirada dejan de aparecer.
Las consultas de catalogo excluyen retiradas, inactivas y borradores en SQL.
El checkout no permite reservar inventario retirado, aunque se conozca el UUID.
Guardar un agregado obsoleto no puede reactivar una botella eliminada.
La politica existente sigue impidiendo eliminar fragancias con pedidos activos.
No se introduce eliminacion de pedidos, cuentas ni presentaciones.

`BotellaMadrePersistenceMapper`, `PedidoPersistenceMapper` y
`PresentacionPersistenceMapper` separan la traduccion de las operaciones de
persistencia. Viven junto a las entidades JPA para mantener sus campos internos
fuera de la API publica. Conservan decants, entrega, comprador invitado, hash de
consulta, vendedor, precios y estado de publicacion.

## Pruebas y recorrido de revision

```powershell
.\gradlew.bat test --no-daemon
```

Verificacion del 30 de octubre de 2026: **92 pruebas, 0 fallos, 0 errores,
0 omitidas; BUILD SUCCESSFUL**. Se conservan las 83 anteriores y se agregan 9.

- `BorradoLogicoJpaTest`: conserva filas e historial; oculta retiradas; rechaza
  reservas/restauraciones; contempla varias botellas de la misma fragancia.
- `ConsultasCatalogoJpaTest`: texto+familia, conteo sin duplicados, paginacion,
  pagina vacia, comodines literales, borradores e inactivas; ranking por estados,
  suma de varios pedidos, limite y desempate estable.
- `ConsultasCatalogoHttpTest`: rutas publicas con H2, metadatos JSON y errores
  de parametros.
- `DatosDemoInicializadorTest`: arranque con semilla, BCrypt, usuarios y ranking;
  reinicio sin duplicacion ni reposicion de stock modificado.

Para mostrar el bloque: abrir pagina de catalogo, combinar texto/familia,
consultar ranking, iniciar sesion en Swagger y consultar pedidos del cliente.
Enseñar JPQL y mappers junto a las pruebas DataJpaTest. Una compra permanece
CREADO hasta que exista un flujo de confirmacion; por eso no entra aun al ranking.

Este bloque no cambia el README compartido de Deivid ni inventa observaciones
de Entrega 1. El estado del resto del equipo esta en
[la revision actualizada](auditoria-entrega2-actualizada.md). La integracion a
main y la revision de otro integrante siguen pendientes; trabajar en una rama
no equivale a tener un PR aprobado. MariaDB no se ha verificado y su esquema
requiere migraciones antes de usar el perfil con `ddl-auto=validate`.
