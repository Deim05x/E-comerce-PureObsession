# Pure Obsession · Angular

Frontend Angular 21 con componentes standalone, formularios y rutas diferidas. Consume el backend Spring a través de `/api`; no contiene una base de productos ficticia.

## Desarrollo

Requiere Node 24.14 y npm 11 (o versiones compatibles con Angular 21).

```powershell
cd frontend
npm ci
npm start
```

Abre http://localhost:4200. Inicia también Spring Boot en el puerto 8080. `proxy.conf.json` dirige las solicitudes API a ese puerto. En producción configura el servidor para devolver index.html en las rutas Angular y reenviar `/api` al backend; nunca publiques credenciales en el frontend.

```powershell
npm test
npm run build
```

El resultado se genera en `dist/pure-obsession/browser`.

## Trabajo en equipo

- `src/app/pages/storefront.ts`, `product.ts`: catálogo y tiendas.
- `auth.ts`, `account.ts`: acceso y cuenta.
- `checkout.ts`: carrito, compra y seguimiento.
- `seller.ts`, `seller-editor.ts`: panel del vendedor.
- `admin.ts`: certificados y confirmación manual de pagos.
- `core`: contratos, sesión y API compartidos; coordinar cambios con el backend.
- `src/styles.css`: diseño adaptable negro y dorado basado en las referencias suministradas.

Los pagos simulados se muestran solo cuando `/api/opciones` informa `modoDemo=true`; el backend impone además su perfil demo. El resto utiliza confirmación manual del administrador. No hay integración bancaria.

Las pantallas de cuenta incluyen perfil, favoritos y pedidos. La administración avanzada de direcciones, cambio de clave desde el perfil y la creación de botellas madre todavía requieren ampliar la interfaz; sus servicios ya existen en el backend. Las listas de gestión muestran hasta 100 registros y requieren paginación para volúmenes mayores. Se debe verificar el recorrido completo con el backend iniciado antes de una entrega o despliegue.

## Validación de esta etapa

Compilación de producción y servidor de desarrollo verificados; tres pruebas de utilidades aprobadas. No fue posible verificar visualmente en esta sesión porque no hay navegador conectado. `npm audit` reporta 16 avisos moderados en dependencias transitivas de las herramientas de desarrollo de Angular CLI; no se aplicó la reducción incompatible a CLI 16 propuesta por npm. Revisar actualizaciones compatibles antes del despliegue.
