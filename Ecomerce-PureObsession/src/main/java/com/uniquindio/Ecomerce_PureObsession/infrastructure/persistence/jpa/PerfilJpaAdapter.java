package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.*;
import com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaClave;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@Repository @Transactional
public class PerfilJpaAdapter implements PerfilRepository,EstadoTokenPort {
    @PersistenceContext private EntityManager em;
    private final PasswordEncoder encoder;private final ObjectProvider<JavaMailSender> correo;private final Environment env;
    private final String url,desde;
    public PerfilJpaAdapter(PasswordEncoder encoder,ObjectProvider<JavaMailSender> correo,Environment env,
            @Value("${app.recuperacion.url:http://localhost:5173/restablecer}") String url,@Value("${app.correo.desde:}") String desde) {
        this.encoder=encoder;this.correo=correo;this.env=env;this.url=url;this.desde=desde;
    }
    private Optional<CuentaJpa> buscar(String email) {
        if(email==null || email.length()>254) return Optional.empty();
        return em.createQuery("select c from CuentaJpa c where c.email=:email",CuentaJpa.class)
                .setParameter("email",email.trim().toLowerCase(Locale.ROOT)).getResultStream().findFirst();
    }
    private CuentaJpa cuenta(String actor) {
        CuentaJpa c=buscar(actor).orElseThrow(()->new ReglaDominioException("La cuenta no existe."));em.lock(c,LockModeType.PESSIMISTIC_WRITE);return c;
    }
    private Perfil vista(CuentaJpa c) {return new Perfil(c.id,c.nombre,c.email,c.rol,c.telefono,c.nacimiento);}
    @Override public int version(String email) {return buscar(email).map(c->c.versionToken).orElse(-1);}
    @Override public Perfil perfil(String actor) {return vista(cuenta(actor));}
    private void texto(String t,int max,boolean obligatorio) {
        if((obligatorio && (t==null || t.isBlank())) || (t!=null && t.length()>max)) throw new ReglaDominioException("Revise los campos y sus longitudes.");
    }
    @Override public Perfil guardar(String actor,DatosPerfil d) {
        texto(d.nombre(),120,true);texto(d.telefono(),22,false);
        if(d.nacimiento()!=null && d.nacimiento().isAfter(LocalDate.now())) throw new ReglaDominioException("Fecha de nacimiento invalida.");
        CuentaJpa c=cuenta(actor);c.nombre=d.nombre().trim();c.telefono=d.telefono();c.nacimiento=d.nacimiento();return vista(c);
    }
    private Direccion vista(DireccionJpa d) {return new Direccion(d.id,d.etiqueta,d.receptor,d.telefono,d.departamento,d.ciudad,d.direccion,d.indicaciones,d.predeterminada);}
    @Override public List<Direccion> direcciones(String actor) {
        UUID id=cuenta(actor).id;return em.createQuery("select d from DireccionJpa d where d.cuentaId=:id and d.eliminada=false order by d.predeterminada desc,d.id",DireccionJpa.class)
                .setParameter("id",id).getResultList().stream().map(this::vista).toList();
    }
    @Override public Direccion direccion(String actor,UUID id,Direccion datos) {
        texto(datos.etiqueta(),60,true);texto(datos.receptor(),120,true);texto(datos.telefono(),22,true);
        texto(datos.departamento(),100,true);texto(datos.ciudad(),100,true);texto(datos.direccion(),350,true);texto(datos.indicaciones(),500,false);
        UUID cuentaId=cuenta(actor).id;DireccionJpa d=id==null ? null : em.find(DireccionJpa.class,id);
        if(id!=null && (d==null || d.eliminada || !cuentaId.equals(d.cuentaId))) throw new RecursoNoEncontradoException("La direccion no existe.");
        long cantidad=em.createQuery("select count(d) from DireccionJpa d where d.cuentaId=:id and d.eliminada=false",Long.class).setParameter("id",cuentaId).getSingleResult();
        if(id==null && cantidad>=10) throw new ReglaDominioException("Maximo diez direcciones.");
        if(d==null) {d=new DireccionJpa();d.id=UUID.randomUUID();d.cuentaId=cuentaId;em.persist(d);}
        boolean predeterminada=datos.predeterminada() || cantidad==0 || d.predeterminada;
        if(predeterminada) em.createQuery("update DireccionJpa d set d.predeterminada=false where d.cuentaId=:id and d.id<>:direccion").setParameter("id",cuentaId).setParameter("direccion",d.id).executeUpdate();
        d.etiqueta=datos.etiqueta();d.receptor=datos.receptor();d.telefono=datos.telefono();d.departamento=datos.departamento();d.ciudad=datos.ciudad();
        d.direccion=datos.direccion();d.indicaciones=datos.indicaciones();d.predeterminada=predeterminada;return vista(d);
    }
    @Override public void eliminarDireccion(String actor,UUID id) {
        UUID cliente=cuenta(actor).id;DireccionJpa d=em.find(DireccionJpa.class,id);
        if(d==null || d.eliminada || !cliente.equals(d.cuentaId)) throw new RecursoNoEncontradoException("La direccion no existe.");
        boolean principal=d.predeterminada;d.eliminada=true;d.predeterminada=false;em.flush();
        if(principal) em.createQuery("select d from DireccionJpa d where d.cuentaId=:id and d.eliminada=false order by d.id",DireccionJpa.class)
                .setParameter("id",cliente).setMaxResults(1).getResultStream().findFirst().ifPresent(x->x.predeterminada=true);
    }
    @Override public void cambiarClave(String actor,String actual,String nueva) {
        PoliticaClave.validar(nueva);CuentaJpa c=cuenta(actor);
        if(actual==null || !encoder.matches(actual,c.claveHash)) throw new ReglaDominioException("Clave actual incorrecta.");
        c.claveHash=encoder.encode(nueva);c.versionToken++;
        em.createQuery("update RecuperacionClaveJpa r set r.utilizada=true where r.cuentaId=:id").setParameter("id",c.id).executeUpdate();
    }
    static String hash(String token) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    @Override public String recuperar(String email) {
        boolean demo=env.acceptsProfiles(Profiles.of("demo"));JavaMailSender sender=correo.getIfAvailable();
        if(!demo && (sender==null || desde.isBlank())) throw new ServicioNoDisponibleException();
        Optional<CuentaJpa> encontrada=buscar(email);if(encontrada.isEmpty())return null;
        CuentaJpa c=cuenta(email);
        if(em.createQuery("select count(r) from RecuperacionClaveJpa r where r.cuentaId=:id and r.utilizada=false and r.venceEn>:limite",Long.class)
                .setParameter("id",c.id).setParameter("limite",Instant.now().plusSeconds(840)).getSingleResult()>0) return null;
        // Un unico token vigente por cuenta, con caducidad y sin guardar el token en claro.
        em.createQuery("update RecuperacionClaveJpa r set r.utilizada=true where r.cuentaId=:id").setParameter("id",c.id).executeUpdate();
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RecuperacionClaveJpa r=new RecuperacionClaveJpa();r.hash=hash(token);r.cuentaId=c.id;r.venceEn=Instant.now().plusSeconds(900);em.persist(r);
        if(!demo) {
            SimpleMailMessage mensaje=new SimpleMailMessage();mensaje.setTo(c.email);mensaje.setFrom(desde);mensaje.setSubject("Restablece tu clave de Pure Obsession");
            mensaje.setText("Abre este enlace durante los proximos 15 minutos: "+url+"?token="+token);
            try {sender.send(mensaje);} catch(org.springframework.mail.MailException e) {throw new ServicioNoDisponibleException();}
        }
        return demo ? token : null;
    }
    @Override public void restablecer(String token,String nueva) {
        PoliticaClave.validar(nueva);
        if(token==null || token.length()!=43) throw new ReglaDominioException("Enlace invalido o vencido.");
        // Bloquear primero cuenta y luego token, igual que cambiarClave/recuperar.
        RecuperacionClaveJpa inicial=em.find(RecuperacionClaveJpa.class,hash(token));
        if(inicial==null) throw new ReglaDominioException("Enlace invalido o vencido.");
        CuentaJpa c=em.find(CuentaJpa.class,inicial.cuentaId,LockModeType.PESSIMISTIC_WRITE);
        em.refresh(inicial,LockModeType.PESSIMISTIC_WRITE);
        if(inicial.utilizada || inicial.venceEn.isBefore(Instant.now())) throw new ReglaDominioException("Enlace invalido o vencido.");
        c.claveHash=encoder.encode(nueva);c.versionToken++;inicial.utilizada=true;
    }
}
