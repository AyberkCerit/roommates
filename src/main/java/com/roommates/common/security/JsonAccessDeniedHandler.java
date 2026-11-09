package com.roommates.common.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.roommates.common.error.ErrorResponseWriter;

/**
 * 403 CEVABINI YAZAN SINIF (yetki yok)
 *
 * Ne zaman çalışır?
 *   Kullanıcı giriş yapmış ama bu işi yapmaya yetkisi yok.
 *   Örnek: viewer bir odayı silmeye çalışıyor.
 *
 * Cevabı ekibin ortak hata biçimiyle yazar (M30, M35).
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter hataYazici;

    // Constructor injection (M7)
    public JsonAccessDeniedHandler(ErrorResponseWriter hataYazici) {
        this.hataYazici = hataYazici;
    }

    @Override
    public void handle(HttpServletRequest istek,
                       HttpServletResponse cevap,
                       AccessDeniedException hata) throws IOException {
        hataYazici.write(cevap, HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok", istek.getRequestURI());
    }
}
