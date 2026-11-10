package com.roommates.common.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.roommates.common.error.ErrorResponseWriter;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ErrorResponseWriter hataYazici;

    public JsonAuthenticationEntryPoint(ErrorResponseWriter hataYazici) {
        this.hataYazici = hataYazici;
    }

    @Override
    public void commence(HttpServletRequest istek,
                         HttpServletResponse cevap,
                         AuthenticationException hata) throws IOException {
        cevap.setHeader("WWW-Authenticate", "Basic realm=\"RoomMates\"");

        String mesaj = mesajSec(hata);
        hataYazici.write(cevap, HttpStatus.UNAUTHORIZED, mesaj, istek.getRequestURI());
    }

    private String mesajSec(AuthenticationException hata) {
        if (hata instanceof BadCredentialsException) {
            return "Kullanıcı adı veya parola hatalı";
        }
        if (hata instanceof DisabledException) {
            return "Hesap pasif";
        }
        return "Kimlik doğrulaması gerekli";
    }
}
