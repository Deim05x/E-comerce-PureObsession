# Lenguaje ubicuo — Pure Obsession

Nicho: perfumería con venta de presentaciones completas, testers y fraccionamiento en decants.

| Término propio | Significado y uso |
|---|---|
| Decant | Frasco de menor volumen obtenido de una botella madre; hereda su fragancia y concentración. Tiene identidad propia y queda conservado por la botella. |
| Botella madre | Inventario original del que se extraen decants. Distingue volumen original positivo y disponible que puede agotarse. |
| Familia olfativa | Clasificación del perfil aromático: FLORAL, AMADERADA, GOURMAND, CITRICA u ORIENTAL. |
| Concentración | Clasificación EAU_DE_PARFUM, EAU_DE_TOILETTE, EXTRAIT_DE_PARFUM o EAU_DE_COLOGNE. Es obligatoria al publicar, no al registrar un borrador. |
| Tester | Presentación para demostración comercializada sin envoltura de regalo. No significa producto usado o defectuoso. |
| Pirámide olfativa | Descripción de notas de salida, corazón y fondo; las tres son obligatorias cuando se construye el VO. |
| Fragancia | Referencia aromática con identidad, nombre, familia, concentración y pirámide; se distingue de la botella física. |

## Ejemplo con la API real

```java
var piramide = new PiramideOlfativa("Bergamota", "Lavanda", "Ambar");
var fragancia = Fragancia.crear(UUID.randomUUID(), "Sauvage",
        FamiliaOlfativa.AMADERADA, Concentracion.EAU_DE_PARFUM, piramide);
fragancia.publicar();
var botella = BotellaMadre.crear(UUID.randomUUID(), fragancia, new Volumen(100));
var decant = botella.crearDecant(UUID.randomUUID(), new Volumen(10));
// botella.getVolumenDisponible().mililitros() == 90
// botella.getDecants() conserva el decant creado.
```

No usar Producto/Comprador/Vendedor/Compra como sustitutos de los términos propios. Comprador y vendedor sí nombran actores de los casos de uso, pero no cuentan como los cinco términos del nicho.

Reglas completas: [reglas de negocio](docs/reglas-de-negocio.md).
