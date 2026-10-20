package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CertificadoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoCertificado;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
@Repository
@Transactional
public class CertificadoJpaAdapter implements CertificadoRepository {
    @PersistenceContext private EntityManager em;
    public void guardar(UUID id,String tipo,byte[] contenido) {
        CertificadoJpa j=new CertificadoJpa(); j.id=id; j.tipoContenido=tipo;
        j.contenido=contenido.clone(); j.estado=EstadoCertificado.PENDIENTE; em.merge(j);
    }
    public Optional<Certificado> buscar(UUID id) {
        return Optional.ofNullable(em.find(CertificadoJpa.class,id))
            .map(j->new Certificado(j.tipoContenido,j.contenido.clone(),j.estado));
    }
    public void revisar(UUID id,boolean aprobado) {
        CertificadoJpa j=em.find(CertificadoJpa.class,id);
        if(j==null) throw new ReglaDominioException("No hay certificado para revisar.");
        j.estado=aprobado?EstadoCertificado.APROBADO:EstadoCertificado.RECHAZADO;
    }
}

