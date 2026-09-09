# Glosario del Lenguaje Ubicuo - PureObsession

## Conceptos Centrales

### Decant

**Definición:** Porción de una fragancia original que ha sido extraída y envasada cuidadosamente en un frasco de menor volumen (ej. 5ml, 10ml) para su venta individual, permitiendo a los clientes probar el aroma sin adquirir la botella completa.

**Precondiciones:** El volumen a extraer para crear el decant debe ser menor o igual al volumen remanente disponible en la botella original (botella madre).

**No usar:** Muestra, Fracción, Sub-producto.

**Ejemplo de uso en código:**

```java
public Decant extraerDecant(BotellaOriginal botella, int mililitrosSolicitados) {
    botella.descontarVolumen(mililitrosSolicitados);
    return new Decant(botella.getFragancia(), mililitrosSolicitados);
}
```

### Familia Olfativa

**Definición:** Categoría principal a la que pertenece el perfil aromático de una fragancia (ej. Amaderada, Cítrica, Floral, Gourmand). Es el criterio principal de clasificación en el catálogo para guiar la búsqueda del usuario.

**Sinónimos aceptados:** Categoría olfativa.

**No usar:** Categoría, Tipo de producto, Etiqueta.

**Ejemplo de uso en código:**

```java
public List<Fragancia> filtrarCatalogo(FamiliaOlfativa familia) {
    return repositorioFragancias.buscarPorFamilia(familia);
}
```

### Concentracion

**Definición:** El nivel de pureza y porcentaje de aceites esenciales presentes en la composición de la fragancia, lo cual determina su longevidad y proyección (ej. Eau de Toilette, Eau de Parfum, Extrait de Parfum).

**Precondiciones:** Toda fragancia registrada en el sistema debe tener una concentración asignada obligatoriamente para calcular su precio base.

**Ejemplo de uso en código:**

```java
if (fragancia.getConcentracion() == Concentracion.EXTRAIT_DE_PARFUM) {
    fragancia.aplicarEtiquetaPremium();
}
```

### Tester

**Definición:** Botella de fragancia original provista por la casa fabricante en empaque genérico (caja de cartón blanca o café) concebida inicialmente para demostración. En la tienda se comercializa a un precio reducido respecto a la presentación regular, conteniendo exactamente el mismo líquido.

**Precondiciones:** Un artículo catalogado como Tester no puede ser seleccionado por el cliente para la opción de "Envoltura de regalo".

**No usar:** Muestra, Defectuoso, Usado, Saldo.

**Ejemplo de uso en código:**

```java
public void agregarAlCarrito(Fragancia fragancia, boolean solicitarEnvoltura) {
    if (fragancia.esTester() && solicitarEnvoltura) {
        throw new RegaloInvalidoException("Un tester no incluye caja de lujo, no puede envolverse para regalo.");
    }
    carrito.agregar(fragancia);
}
```

### Piramide Olfativa

**Definición:** Estructura evolutiva de los aromas de la fragancia a lo largo del tiempo, dividida estrictamente en tres fases: Notas de Salida (primeros 15 min), Notas de Corazón (desarrollo) y Notas de Fondo (secado y fijación).

**No usar:** Descripción del olor, Ingredientes, Composición.

**Ejemplo de uso en código:**

```java
PiramideOlfativa piramide = new PiramideOlfativa();
piramide.agregarNota(FaseOlfativa.FONDO, new Nota("Sándalo"));
fragancia.asignarPiramide(piramide);
```

## Anti-patrones (Términos a EVITAR en nuestro proyecto)

| No usar | Usar |
|---|---|
| Categoria / TipoItem | FamiliaOlfativa |
| Variante / Opcion | Concentracion |
| Muestra | Decant / Tester |
| AtributosAdicionales | PiramideOlfativa |
