package com.roommates.common.error;

/**
 * ÇAKIŞMA HATASI → 409
 *
 * İstek, veritabanında zaten var olan bir kayıtla çakıştığında fırlatılır.
 *
 * Örnek:
 *   throw new ConflictException("Kullanıcı adı zaten kullanılıyor");
 *
 * Bu hatayı GlobalExceptionHandler yakalar ve 409 cevabına çevirir.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param mesaj kullanıcıya gösterilecek Türkçe açıklama
     */
    public ConflictException(String mesaj) {
        super(mesaj);
    }
}
