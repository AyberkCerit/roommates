package com.roommates.common.error;

import java.io.IOException;
import java.time.Clock;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

@Component
public class ErrorResponseWriter {
    private final JsonMapper jsonDonusturucu;

    private final Clock saat;

    public ErrorResponseWriter(JsonMapper jsonDonusturucu, Clock saat) {
        this.jsonDonusturucu = jsonDonusturucu;
        this.saat = saat;
    }

    public void write(HttpServletResponse cevap,
                      HttpStatus durum,
                      String mesaj,
                      String yol) throws IOException {
        ErrorResponse hata = ErrorResponse.of(saat, durum, mesaj, yol, null);

        cevap.setStatus(durum.value());

        cevap.setContentType("application/json;charset=UTF-8");

        jsonDonusturucu.writeValue(cevap.getOutputStream(), hata);
    }
}
