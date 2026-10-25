package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.PresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPresentacionRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PresentacionResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.*;
public class PresentacionesUseCase implements PresentacionesPort {
    private final PresentacionRepository presentaciones;
    private final CuentaRepository cuentas;
    private final CertificadoRepository certificados;
    private final BotellaMadreRepository botellas;
    public PresentacionesUseCase(PresentacionRepository p,CuentaRepository c,CertificadoRepository cert,BotellaMadreRepository b) {
        presentaciones=p; cuentas=c; certificados=cert; botellas=b;
    }
    private Cuenta cuenta(String email) {
        return cuentas.buscarPorEmail(email).orElseThrow(()->new ReglaDominioException("La cuenta no existe."));
    }
    private Presentacion propia(String actor,UUID id) {
        Cuenta c=cuenta(actor);
        Presentacion p=presentaciones.buscarPorId(id).orElseThrow(()->new RecursoNoEncontradoException("La presentación no existe."));
        if(c.getRol()!=Rol.VENDEDOR || !p.getVendedorId().equals(c.getId()))
            throw new ReglaDominioException("La presentación no pertenece al vendedor.");
        return p;
    }
    public PresentacionResponse crear(String actor,CrearPresentacionRequest r) {
        Cuenta c=cuenta(actor);
        if(c.getRol()!=Rol.VENDEDOR) throw new ReglaDominioException("Solo los vendedores registran presentaciones.");
        if(r==null || r.volumenMl()==null || r.original()==null)
            throw new ReglaDominioException("La solicitud está incompleta.");
        if(botellas.listarTodas().stream().noneMatch(b->b.getFragancia().getId().equals(r.fraganciaId()) && b.getFragancia().isActiva()))
            throw new ReglaDominioException("La fragancia debe existir y estar activa.");
        if(r.tipo()==TipoPresentacion.DECANT) {
            if(r.botellaMadreId()==null) throw new ReglaDominioException("El decant requiere botella madre.");
            BotellaMadre b=botellas.buscarPorId(r.botellaMadreId()).orElseThrow(()->new ReglaDominioException("La botella no existe."));
            if(!b.getFragancia().getId().equals(r.fraganciaId()) || r.volumenMl()>=b.getVolumenOriginal().mililitros())
                throw new ReglaDominioException("El decant debe corresponder a su botella y ser menor que ella.");
        }
        Presentacion p=Presentacion.crear(UUID.randomUUID(),c.getId(),r.fraganciaId(),r.botellaMadreId(),
            r.tipo(),r.volumenMl(),r.precio(),r.origen(),r.paisOrigen(),r.original());
        return PresentacionResponse.desde(presentaciones.guardar(p));
    }
    public PresentacionResponse publicar(String actor,UUID id) {
        Presentacion p=propia(actor,id);
        List<BotellaMadre> asociadas=botellas.listarTodas().stream()
            .filter(b->b.getFragancia().getId().equals(p.getFraganciaId())).toList();
        if(asociadas.isEmpty()) throw new ReglaDominioException("La fragancia no existe.");
        p.publicar(certificados.buscar(id).map(c->c.estado()==EstadoCertificado.APROBADO).orElse(false));
        for(BotellaMadre b:asociadas) {b.getFragancia().publicar(); botellas.guardar(b);}
        return PresentacionResponse.desde(presentaciones.guardar(p));
    }
    public List<PresentacionResponse> catalogo() {
        Set<UUID> activas=new HashSet<>();
        botellas.listarTodas().stream().map(BotellaMadre::getFragancia)
            .filter(f->f.isActiva() && f.isPublicada()).forEach(f->activas.add(f.getId()));
        return presentaciones.listarPublicadas().stream().filter(p->activas.contains(p.getFraganciaId()))
            .map(PresentacionResponse::desde).toList();
    }
    public void cargarCertificado(String actor,UUID id,String tipo,byte[] archivo) {
        
        Presentacion p=propia(actor,id);
        if(!p.isOriginal()) throw new ReglaDominioException("El certificado corresponde a una presentación original.");
        if(archivo==null || archivo.length==0 || archivo.length>5*1024*1024)
            throw new ReglaDominioException("El certificado debe pesar entre 1 byte y 5 MB.");
        boolean pdf="application/pdf".equals(tipo) && comienza(archivo,new byte[]{37,80,68,70,45});
        boolean png="image/png".equals(tipo) && comienza(archivo,new byte[]{(byte)137,80,78,71,13,10,26,10});
        boolean jpg="image/jpeg".equals(tipo) && comienza(archivo,new byte[]{(byte)255,(byte)216,(byte)255});
        if(!pdf && !png && !jpg) throw new ReglaDominioException("Use un archivo PDF, PNG o JPEG válido.");
        p.despublicar(); presentaciones.guardar(p); certificados.guardar(id,tipo,archivo);
    }
    private boolean comienza(byte[] archivo,byte[] firma) {
        if(archivo.length<firma.length) return false;
        for(int i=0;i<firma.length;i++) if(archivo[i]!=firma[i]) return false;
        return true;
    }
    public com.uniquindio.Ecomerce_PureObsession.application.dto.response.EmpaqueResponse validarEmpaque(UUID id,TipoEmpaque tipo) {
        Presentacion p=presentaciones.buscarPorId(id).orElseThrow(()->new ReglaDominioException("La presentación no existe."));
        if(catalogo().stream().noneMatch(x->x.id().equals(id)))
            throw new ReglaDominioException("La presentación no está disponible en el catálogo.");
        p.getTipo().validarEmpaque(tipo);
        return new com.uniquindio.Ecomerce_PureObsession.application.dto.response.EmpaqueResponse(id,tipo,true);
    }
    public CertificadoRepository.Certificado consultarCertificado(String actor,UUID id) {
        Cuenta c=cuenta(actor);
        if(c.getRol()!=Rol.ADMIN) propia(actor,id);
        return certificados.buscar(id).orElseThrow(()->new ReglaDominioException("No hay certificado."));
    }
    public void revisarCertificado(String actor,UUID id,boolean aprobado) {
        if(cuenta(actor).getRol()!=Rol.ADMIN) throw new ReglaDominioException("Solo un administrador revisa certificados.");
        Presentacion p=presentaciones.buscarPorId(id).orElseThrow(()->new ReglaDominioException("La presentación no existe."));
        certificados.revisar(id,aprobado);
        if(!aprobado) {p.despublicar(); presentaciones.guardar(p);}
    }
}

