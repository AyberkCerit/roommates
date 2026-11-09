package com.roommates.common.error;

import java.io.IOException;
import java.time.Clock;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

/**
 * HATA CEVABINI ELLE YAZAN YARDIMCI SINIF
 *
 * Neden gerekli?
 *   401 (giriş yapılmamış) ve 403 (yetki yok) hataları, istek daha
 *   controller'a ulaşmadan güvenlik katmanında oluşur. GlobalExceptionHandler
 *   sadece controller'dan gelen hataları görebildiği için bunları yakalayamaz.
 *
 *   Şevval'in güvenlik sınıfları bu sınıfı kullanarak 401 ve 403 hatalarını
 *   da aynı ErrorResponse biçiminde yazar. Böylece API'nin bütün hataları
 *   aynı görünür (kontrol listesi M30).
 *
 * @Component: Spring bu sınıftan kendisi bir nesne oluşturur ve ihtiyacı
 *             olan sınıflara verir.
 */
@Component
public class ErrorResponseWriter {

    // Java nesnesini JSON metnine çeviren araç (Spring Boot hazır verir).
    private final JsonMapper jsonDonusturucu;

    // Şu anki zamanı veren saat (AppConfig'te tanımlı).
    private final Clock saat;

    /**
     * Constructor injection (kontrol listesi M7):
     * İhtiyacımız olan iki nesneyi Spring bu constructor'a parametre olarak verir.
     * "final" oldukları için sonradan değiştirilemezler.
     */
    public ErrorResponseWriter(JsonMapper jsonDonusturucu, Clock saat) {
        this.jsonDonusturucu = jsonDonusturucu;
        this.saat = saat;
    }

    /**
     * Hata cevabını oluşturup kullanıcıya gönderir.
     *
     * Örnek:
     *   write(cevap, HttpStatus.UNAUTHORIZED, "Kimlik doğrulaması gerekli", "/api/rooms");
     *
     * @param cevap kullanıcıya gidecek HTTP cevabı
     * @param durum HTTP durum kodu, örnek: 401
     * @param mesaj Türkçe açıklama
     * @param yol   isteğin adresi
     */
    public void write(HttpServletResponse cevap,
                      HttpStatus durum,
                      String mesaj,
                      String yol) throws IOException {

        // 1) Hata nesnesini oluştur (form hatası yok, o yüzden son parametre null).
        ErrorResponse hata = ErrorResponse.of(saat, durum, mesaj, yol, null);

        // 2) Cevabın durum kodunu ayarla, örnek: 401.
        cevap.setStatus(durum.value());

        // 3) Cevabın JSON olduğunu söyle. UTF-8 olmazsa ş, ı, ğ bozuk görünür.
        cevap.setContentType("application/json;charset=UTF-8");

        // 4) Hata nesnesini JSON'a çevirip cevaba yaz.
        jsonDonusturucu.writeValue(cevap.getOutputStream(), hata);
    }
}
