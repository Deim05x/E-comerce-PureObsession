package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.InventarioVentaPort;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Reserva el volumen de la botella madre y lo protege con bloqueos pesimistas. */
@Repository
@Transactional
public class InventarioVentaJpaAdapter implements InventarioVentaPort {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void reservar(UUID presentacionId, int cantidad) {
        moverExistencias(presentacionId, cantidad, true);
    }

    @Override
    public void liberar(UUID presentacionId, int cantidad) {
        moverExistencias(presentacionId, cantidad, false);
    }
    @Override public void confirmar(UUID presentacionId,int cantidad) {
        PresentacionJpa p=entityManager.find(PresentacionJpa.class,presentacionId,LockModeType.PESSIMISTIC_WRITE);
        if(p!=null && p.tipo!=com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion.DECANT) {
            FichaProductoJpa ficha=entityManager.find(FichaProductoJpa.class,presentacionId,LockModeType.PESSIMISTIC_WRITE);
            if(ficha!=null) ficha.reservadas=com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace.liberar(ficha.reservadas,cantidad);
        }
    }

    private void moverExistencias(UUID presentacionId, int cantidad, boolean reservar) {
        if (presentacionId == null || cantidad <= 0) {
            throw new ReglaDominioException("La presentación y la cantidad deben ser válidas.");
        }

        PresentacionJpa presentacion = entityManager.find(PresentacionJpa.class, presentacionId,
                LockModeType.PESSIMISTIC_WRITE);
        if (presentacion == null) {
            throw new RecursoNoEncontradoException("La presentación no existe.");
        }
        FichaProductoJpa ficha=entityManager.find(FichaProductoJpa.class,presentacionId,LockModeType.PESSIMISTIC_WRITE);
        if(presentacion.tipo!=com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion.DECANT && ficha!=null) {
            if(reservar) {
                FraganciaJpa f=entityManager.find(FraganciaJpa.class,presentacion.fraganciaId);
                if(!presentacion.publicada || f==null || f.eliminada || !f.activa || !f.publicada)
                    throw new ReglaDominioException("La oferta no esta disponible.");
                ficha.disponibles=com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace.reservar(ficha.disponibles,cantidad);
                ficha.reservadas=Math.addExact(ficha.reservadas,cantidad);
            } else {
                ficha.reservadas=com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace.liberar(ficha.reservadas,cantidad);
                ficha.disponibles=Math.addExact(ficha.disponibles,cantidad);
            }
            return;
        }
        if (presentacion.botellaMadreId == null) {
            throw new ReglaDominioException("La presentación no tiene inventario asociado.");
        }

        BotellaJpa botella = entityManager.find(BotellaJpa.class, presentacion.botellaMadreId,
                LockModeType.PESSIMISTIC_WRITE);
        if (botella == null) {
            throw new RecursoNoEncontradoException("La botella madre de la presentación no existe.");
        }
        if (!botella.fragancia.id.equals(presentacion.fraganciaId)) {
            throw new ReglaDominioException("La presentación no corresponde a la botella madre.");
        }

        final int volumenReservado;
        try {
            volumenReservado = Math.multiplyExact(presentacion.volumenMl, cantidad);
        } catch (ArithmeticException ex) {
            throw new ReglaDominioException("La cantidad solicitada supera el inventario permitido.");
        }

        if (reservar) {
            if(botella.vendedorId!=null && !botella.vendedorId.equals(presentacion.vendedorId))
                throw new ReglaDominioException("El inventario pertenece a otro vendedor.");
            if (botella.eliminada || botella.fragancia.eliminada || !botella.fragancia.activa) {
                throw new ReglaDominioException("El inventario fue retirado del catalogo.");
            }
            if (volumenReservado > botella.disponibleMl) {
                throw new ReglaDominioException("No hay volumen disponible para la cantidad solicitada.");
            }
            botella.disponibleMl -= volumenReservado;
            return;
        }

        if (volumenReservado > botella.originalMl - botella.disponibleMl) {
            throw new ReglaDominioException("La liberación supera el volumen reservado.");
        }
        botella.disponibleMl += volumenReservado;
    }
}
