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

/**
 * 401 CEVABINI YAZAN SINIF (kimlik doğrulanamadı)
 *
 * Ne zaman çalışır?
 *   - Kullanıcı adı / parola hiç gönderilmemişse
 *   - Parola yanlışsa
 *   - Hesap pasifse
 *
 * Spring Security normalde boş bir 401 döner. Biz ErrorResponseWriter ile
 * ekibin ortak hata biçimini (ErrorResponse) yazıyoruz (M30, M35).
 *
 * AuthenticationEntryPoint: Spring Security'nin "kimlik yoksa ne yapayım?" arayüzü.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter hataYazici;

    // Constructor injection (M7)
    public JsonAuthenticationEntryPoint(ErrorResponseWriter hataYazici) {
        this.hataYazici = hataYazici;
    }

    @Override
    public void commence(HttpServletRequest istek,
                         HttpServletResponse cevap,
                         AuthenticationException hata) throws IOException {

        // Tarayıcıya / Postman'e "Basic ile giriş yap" bilgisini verir.
        cevap.setHeader("WWW-Authenticate", "Basic realm=\"RoomMates\"");

        String mesaj = mesajSec(hata);
        hataYazici.write(cevap, HttpStatus.UNAUTHORIZED, mesaj, istek.getRequestURI());
    }

    /** Hatanın türüne göre Türkçe mesaj seçer. */
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
