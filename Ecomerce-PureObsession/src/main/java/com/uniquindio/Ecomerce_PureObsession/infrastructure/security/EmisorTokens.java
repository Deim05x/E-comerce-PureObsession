package com.uniquindio.Ecomerce_PureObsession.infrastructure.security;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import java.time.*;
/** Firma el JWT del login: correo en sub, rol y expiración. Nunca incluye la clave. */
@Component
public class EmisorTokens {
    private final JwtEncoder encoder;
    private final Duration duracion;
    private final com.uniquindio.Ecomerce_PureObsession.application.port.out.EstadoTokenPort estados;
    public EmisorTokens(JwtEncoder encoder,@Value("${app.jwt.expiracion:1h}") Duration duracion,
            com.uniquindio.Ecomerce_PureObsession.application.port.out.EstadoTokenPort estados){
        if(duracion.isNegative() || duracion.isZero() || duracion.compareTo(Duration.ofDays(1))>0) throw new IllegalArgumentException("La duracion JWT debe estar entre un segundo y un dia.");
        this.encoder=encoder;this.duracion=duracion;this.estados=estados;
    }
    public TokenResponse emitir(Authentication autenticacion) {
        Instant ahora=Instant.now(), expira=ahora.plus(duracion);
        String rol=autenticacion.getAuthorities().iterator().next().getAuthority().replaceFirst("^ROLE_","");
        var claims=JwtClaimsSet.builder().subject(autenticacion.getName()).claim("rol",rol).claim("ver",estados.version(autenticacion.getName())).issuedAt(ahora).expiresAt(expira).build();
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new TokenResponse(token,"Bearer",expira);
    }
}
