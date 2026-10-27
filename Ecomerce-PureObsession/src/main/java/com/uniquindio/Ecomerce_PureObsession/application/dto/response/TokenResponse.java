package com.uniquindio.Ecomerce_PureObsession.application.dto.response;
import java.time.Instant;
public record TokenResponse(String token,String tipo,Instant expiraEn) {}
