package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Primitivas criptográficas para la clave de consulta de un pedido invitado. */
final class ClaveConsultaPedido {
    private ClaveConsultaPedido() {}
    static String nuevaClave() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    static String hash(String clave) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(clave.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no disponible", error);
        }
    }
}
