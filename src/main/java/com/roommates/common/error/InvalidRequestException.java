package com.roommates.common.error;

/**
 * GEÇERSİZ İSTEK HATASI → 400
 *
 * İstek bir iş kuralına uymadığında fırlatılır.
 *
 * Örnekler:
 *   throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
 *   throw new InvalidRequestException("Çalışma oturumu yalnız çalışma odasında açılabilir");
 *
 * Bu hatayı GlobalExceptionHandler yakalar ve 400 cevabına çevirir.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String mesaj) {
        super(mesaj);
    }
}
