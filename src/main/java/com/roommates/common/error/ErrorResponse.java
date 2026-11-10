package com.roommates.common.error;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@JsonInclude(JsonInclude.Include.NON_EMPTY)

@JsonPropertyOrder({"timestamp", "status", "error", "message", "path", "fieldErrors"})
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorItem> fieldErrors
) {
    public static ErrorResponse of(Clock saat,
                                   HttpStatusCode durum,
                                   String mesaj,
                                   String yol,
                                   List<FieldErrorItem> alanHatalari) {
        Instant simdi = Instant.now(saat);
        int durumKodu = durum.value();
        String durumAdi = durumAdiniBul(durumKodu);

        return new ErrorResponse(simdi, durumKodu, durumAdi, mesaj, yol, alanHatalari);
    }

    private static String durumAdiniBul(int durumKodu) {
        HttpStatus bilinenDurum = HttpStatus.resolve(durumKodu);

        if (bilinenDurum == null) {
            return "";
        }
        return bilinenDurum.getReasonPhrase();
    }
}
