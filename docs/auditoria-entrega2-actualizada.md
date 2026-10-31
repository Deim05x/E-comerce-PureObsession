# Contraste de la auditoria de Entrega 2

Revision del 30 de octubre de 2026 sobre `origin/main` en `15300c0`.
El PDF de auditoria del 26 de octubre revisaba `069344f`; sus pendientes no
equivalen automaticamente al estado actual. Este documento registra evidencia
del repositorio, no una calificacion ni retroalimentacion inventada de la docente.

## Juan: JWT y Swagger

- Implementados: `EmisorTokens`, login con `TokenResponse`, firma HS256,
  `SessionCreationPolicy.STATELESS`, conversion de roles, BCrypt y propiedades
  `app.jwt.secreto` / `app.jwt.expiracion`.
- `OpenApiConfig` declara `bearerAuth`; las rutas de Swagger son publicas.
- `FlujoHexagonalTest` prueba tokens reales, ausencia de sesion, expiracion y
  firma ajena. Las pruebas HTTP avanzadas cubren permisos.
- Cierre tecnico principal incorporado. Queda comprobar manualmente Authorize
  durante la demo y documentar el uso vigente en el README compartido.

## Deivid: casos de uso, API y cierre

- Implementados: `CrearPresentacionUseCase`, `PublicarPresentacionUseCase`,
  `GestionarCertificadoUseCase`, `ConsultarPresentacionesUseCase`, mapper REST
  `PresentacionWebMapper` y politica de dominio `PoliticaCreacionPresentacion`.
- Compra, consulta y cancelacion tienen casos de uso, endpoints y pruebas,
  incluyendo pedidos de invitado. Las reservas son transaccionales.
- Parcial: `CuentasUseCase` sigue agrupando registro y consulta; quedan DTOs
  de transporte en otros casos y la clase anterior `PresentacionesUseCase`.
- Pendiente documental: README todavia habla de sesiones/CSRF y menciona
  `InventarioVentaTestConfig`; faltan usuarios demo, tabla de correcciones con
  retroalimentacion real y recorrido final Swagger/Postman. No se puede dar
  por completada la entrega de video ni la revision de otro integrante.

## Brayan: persistencia y consultas

Pendientes confirmados y resueltos en este bloque: mappers JPA dedicados, borrado
logico, pagina con texto/familia en BD, ranking de produccion y semilla demo.
Se desarrollaron en commits separados en `Brayan-Cordoba-Branch`, sin incluir
configuracion personal de VS Code ni bases de datos locales. La semilla es un
inicializador equivalente a data.sql, optativo con el perfil `demo`.

- `4ab8ed0`: mappers de BotellaMadre, Pedido y Presentacion.
- `b33385a`: borrado logico, exclusion de ofertas retiradas y pruebas H2.
- `486b41d`: Pagina neutral, JPQL con texto/familia, ranking y rutas publicas.
- `195281e`: cuentas BCrypt, dos vendedores, catalogo y pedidos por estados;
  prueba de reinicio sin duplicacion ni restauracion de stock.

La configuracion JWT solo se ajusto para permitir GET de las nuevas rutas
publicas del catalogo. No se modifico build.gradle ni application.properties.
La [guia del bloque](brayan-persistencia-consultas.md) documenta los contratos
y las credenciales exclusivas de demo para que Deivid cierre el README.

## Verificacion del punto de partida

`gradlew.bat test --no-daemon --offline --console=plain`: **83 pruebas, 0 fallos,
0 errores, BUILD SUCCESSFUL**. H2; no se verifico MariaDB.

El cierre del README compartido y las correcciones oficiales de Entrega 1
corresponden a Deivid y al equipo; requieren la retroalimentacion real.

## Verificacion final del bloque

`gradlew.bat test --no-daemon --offline --console=plain`: **92 pruebas, 0 fallos,
0 errores, 0 omitidas, BUILD SUCCESSFUL**. Incluye las 83 pruebas existentes
y 9 nuevas, con DataJpaTest, HTTP publico y arranque del perfil demo sobre H2.
`git diff --check` sin errores. No hay eliminacion fisica de recursos de negocio
en `src/main/java`, ni imports de Spring/JPA en el dominio.

Falta la revision humana de otro integrante y la integracion del bloque a main.
No se afirma que el README global, la tabla oficial de correcciones o el video
esten terminados. MariaDB sigue pendiente de migraciones y verificacion.
