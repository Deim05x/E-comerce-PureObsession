# Entrega 1: checklist y preparación de sustentación

## Mínimos y ubicación

| Requisito | Evidencia |
|---|---|
| Nicho y al menos cinco términos propios | glosario-lenguaje-ubicuo.md: siete términos. |
| Cinco reglas propias documentadas | reglas-de-negocio.md: ocho reglas, su protección y pruebas. |
| Tres o cuatro entidades | Ocho entidades, fábricas y constructores privados, sin setters, igualdad por UUID. |
| Cinco o seis VO | Siete: tres enums y cuatro records validados. |
| Dos raíces y tres invariantes por raíz | BotellaMadre y Pedido: diagrama-agregado.md y código. |
| Excepción propia consistente | ReglaDominioException para datos y reglas del dominio. |
| Ocho pruebas, distribución mínima | PrecioTest/VolumenTest; FraganciaTest/TesterTest; BotellaMadreInvarianteTest y PedidoInvarianteTest, más regresiones. Deben ejecutarse en verde. |
| Seis a ocho casos de uso | Ocho identificados por actor, repositorio y operación. |
| Dos Repository y una implementación HashMap | BotellaMadreRepository y PedidoRepository, ambos implementados en memoria. |
| Dos Request y un Response diseñados | casos-de-uso-y-dtos.md supera el mínimo y explica campos y mapeo. |
| Diagrama con raíz, límite, dentro/fuera | Dos diagramas Mermaid en diagrama-agregado.md. |
| Capas subidas y organización Git | domain/application/infrastructure; historial real y ramas por persona según autorización comunicada por el equipo. |
| Jira/Trello | Opcional. No se crea evidencia ficticia ni se marca como existente. |

## Verificación antes de entregar

- Ejecutar `./gradlew clean test` con JDK 21 y conservar el informe generado.
- Revisar que GitHub muestre el último commit de la rama correcta.
- Poder demostrar decants de 70 y 30 ml desde una botella de 100 ml: queda cero, nunca negativo.
- Demostrar que un pedido activo bloquea la eliminación de su fragancia aunque itemId sea distinto de fraganciaId.
- Mostrar que una reseña necesita pedido entregado y comprador correcto.
- Abrir los diagramas renderizados y explicar qué no pertenece al agregado.

## Sustentación: todo el equipo debe poder responder

1. ¿Por qué Decant es entidad? Dos frascos con volumen y aroma iguales son artículos distintos: tienen UUID y continuidad propia.
2. ¿Por qué Precio es VO? Se compara por importe y moneda; no necesita identidad ni ciclo independiente.
3. ¿Por qué dos tipos de volumen? El original y lo solicitado deben ser positivos; el disponible sí puede agotarse.
4. ¿Por qué BotellaMadre es raíz? Controla inventario y creación/conservación de sus decants.
5. ¿Por qué Pedido es otra raíz? Protege líneas, total, moneda y estados; conoce inventario por identificadores, no lo modifica directamente.
6. ¿Por qué fraganciaId además de itemId? Un artículo comprado es una presentación concreta, y varias presentaciones pueden pertenecer a una fragancia.
7. ¿Qué ocurre si una operación falla? Las validaciones preceden a los cambios de inventario/colección. Las pruebas verifican que un rechazo conserve el estado.
8. ¿Está todo el e-commerce programado? No es el alcance de esta entrega: ocho casos diseñados y cuatro orquestaciones implementadas, con el dominio protegido.
9. ¿Por qué los DTOs no llevan precio elegido por el comprador? El precio debe resolverse desde catálogo y congelarse en la línea.
10. ¿Qué limita HashMap? Es memoria temporal, para ejecución secuencial; no ofrece persistencia duradera ni transacciones concurrentes.

Reparto sugerido para tres personas: nicho/clasificación; agregados/reglas; casos de uso/pruebas/Git. Ensayar rotando preguntas: la rúbrica pide que todos comprendan cualquier parte, no solo su sección. La sustentación y la calificación final no se pueden garantizar por tener los archivos completos.
