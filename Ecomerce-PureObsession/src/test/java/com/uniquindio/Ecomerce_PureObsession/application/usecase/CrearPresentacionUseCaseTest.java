package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.model.CrearPresentacionCommand;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearPresentacionUseCaseTest {
    @Mock PresentacionRepository presentaciones;
    @Mock CuentaRepository cuentas;
    @Mock BotellaMadreRepository botellas;
    @InjectMocks CrearPresentacionUseCase casoDeUso;

    @Test
    void vendedorCreaPresentacionSinDependerDelDtoHttp() {
        Cuenta vendedor = Cuenta.crear(UUID.randomUUID(), "Vendedor", "123456789",
                "vendedor@prueba.com", "hash", Rol.VENDEDOR);
        Fragancia fragancia = fraganciaActiva();
        BotellaMadre botella = BotellaMadre.crear(UUID.randomUUID(), fragancia, Volumen.crear(100));
        CrearPresentacionCommand comando = new CrearPresentacionCommand(
                fragancia.getId(), null, TipoPresentacion.PERFUME, 100,
                new BigDecimal("350000.00"), OrigenProducto.NACIONAL, "CO", false);

        when(cuentas.buscarPorEmail(vendedor.getEmail())).thenReturn(Optional.of(vendedor));
        when(botellas.listarTodas()).thenReturn(List.of(botella));
        when(presentaciones.guardar(any(Presentacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Presentacion creada = casoDeUso.ejecutar(vendedor.getEmail(), comando);

        assertEquals(vendedor.getId(), creada.getVendedorId());
        assertEquals(fragancia.getId(), creada.getFraganciaId());
        verify(presentaciones).guardar(any(Presentacion.class));
    }

    @Test
    void clienteNoPuedeCrearYNuncaGuarda() {
        Cuenta cliente = Cuenta.crear(UUID.randomUUID(), "Cliente", "987654321",
                "cliente@prueba.com", "hash", Rol.CLIENTE);
        when(cuentas.buscarPorEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));

        assertThrows(ReglaDominioException.class, () -> casoDeUso.ejecutar(
                cliente.getEmail(), new CrearPresentacionCommand(
                        UUID.randomUUID(), null, TipoPresentacion.PERFUME, 100,
                        new BigDecimal("10.00"), OrigenProducto.NACIONAL, "CO", false)));

        verify(presentaciones, never()).guardar(any());
    }

    private Fragancia fraganciaActiva() {
        return Fragancia.crear(UUID.randomUUID(), "Sauvage", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM,
                PiramideOlfativa.crear("Bergamota", "Lavanda", "Ámbar"));
    }
}
