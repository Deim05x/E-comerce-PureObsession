package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CheckoutRegistroPort;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.*;
import jakarta.persistence.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository @Transactional
public class CheckoutRegistroJpaAdapter implements CheckoutRegistroPort {
    @PersistenceContext private EntityManager em;
    @Override public PedidoResponse ejecutar(String actor,String clave,String huella,Supplier<PedidoResponse> compra) {
        if(clave==null || clave.isBlank())return compra.get();
        if(actor==null) throw new ReglaDominioException("Inicie sesion para usar Idempotency-Key.");
        if(!clave.matches("[A-Za-z0-9_-]{16,128}")) throw new ReglaDominioException("Idempotency-Key debe tener de 16 a 128 caracteres alfanumericos, guion o guion bajo.");
        CuentaJpa c=em.createQuery("select c from CuentaJpa c where c.email=:email",CuentaJpa.class).setParameter("email",actor)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        String id=PerfilJpaAdapter.hash(c.id+":"+clave);CheckoutRegistroJpa anterior=em.find(CheckoutRegistroJpa.class,id);
        if(anterior!=null) {
            if(!anterior.huella.equals(huella)) throw new ConflictoException("La clave de idempotencia ya se uso con otros datos.");
            return PedidoResponse.desde(PedidoPersistenceMapper.aDominio(em.find(PedidoJpa.class,anterior.pedidoId)),null);
        }
        PedidoResponse resultado=compra.get();CheckoutRegistroJpa registro=new CheckoutRegistroJpa();registro.id=id;registro.huella=huella;registro.pedidoId=resultado.id();em.persist(registro);return resultado;
    }
}
