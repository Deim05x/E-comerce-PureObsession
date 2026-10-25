package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresentacionesUseCaseNoGuardarTest {
    @Mock PresentacionRepository presentaciones;
    @Mock CuentaRepository cuentas;
    @Mock CertificadoRepository certificados;
    @Mock BotellaMadreRepository botellas;
    @InjectMocks PresentacionesUseCase casoDeUso;

    @Test void noDeberiaGuardarCuandoLaPresentacionNoExiste() {
        UUID id=UUID.randomUUID();
        Cuenta vendedor=Cuenta.crear(UUID.randomUUID(),"Vendedor","123456789","vendedor@prueba.com","hash",Rol.VENDEDOR);
        when(cuentas.buscarPorEmail("vendedor@prueba.com")).thenReturn(Optional.of(vendedor));
        when(presentaciones.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,()->casoDeUso.publicar("vendedor@prueba.com",id));

        verify(presentaciones,never()).guardar(any());
        verify(botellas,never()).guardar(any());
    }
}
