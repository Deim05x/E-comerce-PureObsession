# Agregado Botella Madre

```mermaid
classDiagram
    class BotellaMadre {
        -UUID id
        -Fragancia fragancia
        -Volumen volumenOriginal
        -Volumen volumenDisponible
        +crear(UUID, Fragancia, Volumen) BotellaMadre
        +extraerParaDecant(Volumen) void
    }

    class Fragancia {
        -UUID id
        -String nombre
        -FamiliaOlfativa familiaOlfativa
        -Concentracion concentracion
        -PiramideOlfativa piramideOlfativa
        -boolean publicada
        -boolean activa
        +publicar() void
        +desactivar() void
    }

    class Volumen {
        <<Value Object>>
        +int mililitros
    }

    BotellaMadre *-- Fragancia
    BotellaMadre *-- Volumen : original y disponible
```

## Invariantes

- Nunca se crea un decant si el volumen solicitado supera el volumen disponible de la botella madre.
- Un decant siempre tiene un volumen positivo y menor que el volumen original de la botella madre.
- Al crear un decant, el volumen disponible de la botella madre siempre se reduce en la cantidad extraída.
- Todo decant conserva la misma fragancia y concentración de su botella madre.
- Una fragancia publicada siempre tiene concentración y una pirámide olfativa válida, con notas de salida, corazón y fondo.
