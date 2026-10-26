package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.InventarioVentaPort;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.OrigenProducto;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(InventarioVentaJpaAdapter.class)
class InventarioVentaJpaAdapterTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InventarioVentaPort inventario;

    @Test
    void reservaYLiberaElVolumenDeLaBotellaAsociada() {
        UUID presentacionId = guardarInventario(100, 25);

        inventario.reservar(presentacionId, 3);
        entityManager.flush();
        entityManager.clear();
        assertEquals(25, volumenDisponible(presentacionId));

        inventario.liberar(presentacionId, 3);
        entityManager.flush();
        entityManager.clear();
        assertEquals(100, volumenDisponible(presentacionId));
    }

    @Test
    void rechazaUnaReservaQueSuperaElVolumenDisponible() {
        UUID presentacionId = guardarInventario(40, 25);

        assertThrows(ReglaDominioException.class, () -> inventario.reservar(presentacionId, 2));
        entityManager.flush();
        entityManager.clear();
        assertEquals(40, volumenDisponible(presentacionId));
    }

    private UUID guardarInventario(int volumenDisponible, int volumenPresentacion) {
        FraganciaJpa fragancia = new FraganciaJpa();
        fragancia.id = UUID.randomUUID();
        fragancia.nombre = "Fragancia de prueba";
        fragancia.familia = FamiliaOlfativa.FLORAL;
        fragancia.concentracion = Concentracion.EAU_DE_PARFUM;
        fragancia.activa = true;
        fragancia.publicada = true;
        entityManager.persist(fragancia);

        BotellaJpa botella = new BotellaJpa();
        botella.id = UUID.randomUUID();
        botella.fragancia = fragancia;
        botella.originalMl = 100;
        botella.disponibleMl = volumenDisponible;
        entityManager.persist(botella);

        PresentacionJpa presentacion = new PresentacionJpa();
        presentacion.id = UUID.randomUUID();
        presentacion.vendedorId = UUID.randomUUID();
        presentacion.fraganciaId = fragancia.id;
        presentacion.botellaMadreId = botella.id;
        presentacion.tipo = TipoPresentacion.DECANT;
        presentacion.volumenMl = volumenPresentacion;
        presentacion.precio = new BigDecimal("50000.00");
        presentacion.origen = OrigenProducto.IMPORTADO;
        presentacion.paisOrigen = "FR";
        presentacion.original = false;
        presentacion.publicada = true;
        entityManager.persist(presentacion);
        entityManager.flush();
        entityManager.clear();
        return presentacion.id;
    }

    private int volumenDisponible(UUID presentacionId) {
        PresentacionJpa presentacion = entityManager.find(PresentacionJpa.class, presentacionId);
        return entityManager.find(BotellaJpa.class, presentacion.botellaMadreId).disponibleMl;
    }
}
