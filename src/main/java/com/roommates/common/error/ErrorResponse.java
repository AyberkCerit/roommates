package com.roommates.common.error;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * ORTAK HATA CEVABI (kontrol listesi M29)
 *
 * API'de bir hata olduğunda kullanıcıya HER ZAMAN bu biçimde cevap döneriz:
 *
 *   {
 *     "timestamp": "2026-11-20T14:03:12.481Z",   ← hata ne zaman oldu
 *     "status": 404,                              ← HTTP durum kodu
 *     "error": "Not Found",                       ← durum kodunun adı
 *     "message": "Oda bulunamadı: id=42",         ← Türkçe açıklama
 *     "path": "/api/rooms/42",                    ← hangi adreste oldu
 *     "fieldErrors": [ ... ]                      ← sadece form hatalarında
 *   }
 *
 * JSON alan adları (timestamp, status...) ekibin tasarım belgesinde sabit,
 * bu yüzden İngilizce kalıyor.
 */

// fieldErrors listesi boşsa JSON'a hiç yazma.
@JsonInclude(JsonInclude.Include.NON_EMPTY)
// JSON'daki alanlar her zaman bu sırayla yazılsın.
@JsonPropertyOrder({"timestamp", "status", "error", "message", "path", "fieldErrors"})
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorItem> fieldErrors
) {

    /**
     * Yeni bir hata cevabı oluşturur.
     *
     * Örnek kullanım:
     *   ErrorResponse.of(saat, HttpStatus.NOT_FOUND, "Oda bulunamadı: id=42", "/api/rooms/42", null)
     *
     * @param saat         şu anki zamanı veren saat (AppConfig'teki Clock)
     * @param durum        HTTP durum kodu, örnek: 404
     * @param mesaj        kullanıcıya gösterilecek Türkçe mesaj
     * @param yol          isteğin adresi, örnek: "/api/rooms/42"
     * @param alanHatalari hatalı alanların listesi (form hatası yoksa null)
     */
    public static ErrorResponse of(Clock saat,
                                   HttpStatusCode durum,
                                   String mesaj,
                                   String yol,
                                   List<FieldErrorItem> alanHatalari) {

        Instant simdi = Instant.now(saat);
        int durumKodu = durum.value();               // örnek: 404
        String durumAdi = durumAdiniBul(durumKodu);  // örnek: "Not Found"

        return new ErrorResponse(simdi, durumKodu, durumAdi, mesaj, yol, alanHatalari);
    }

    /**
     * Durum kodunun adını bulur. Örnek: 404 → "Not Found", 400 → "Bad Request"
     */
    private static String durumAdiniBul(int durumKodu) {
        HttpStatus bilinenDurum = HttpStatus.resolve(durumKodu);

        if (bilinenDurum == null) {
            return "";  // Spring bu kodu tanımıyorsa boş bırak
        }
        return bilinenDurum.getReasonPhrase();
    }
}
