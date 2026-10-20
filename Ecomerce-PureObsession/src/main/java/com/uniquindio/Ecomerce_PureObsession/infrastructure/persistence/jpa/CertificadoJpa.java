package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="certificado")
public class CertificadoJpa {
    @Id UUID id;
    String tipoContenido;
    @Lob byte[] contenido;
    @Enumerated(EnumType.STRING) EstadoCertificado estado;
    public CertificadoJpa() {}
}

