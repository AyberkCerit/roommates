package com.roommates.common.error;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * MERKEZİ HATA YAKALAYICI (kontrol listesi M28)
 *
 * Uygulamanın herhangi bir yerinde hata (exception) fırlatıldığında
 * Spring bu sınıfa gelir. Biz de hatayı her zaman aynı biçimde
 * (ErrorResponse) kullanıcıya döneriz.
 *
 * Böylece controller'larda try-catch yazmaya gerek kalmaz.
 *
 * Bu sınıf iki tür hatayla ilgilenir:
 *
 *   BÖLÜM 1 - BİZİM hatalarımız:
 *     ResourceNotFoundException → 404
 *     InvalidRequestException   → 400
 *     ConflictException         → 409
 *     Veritabanı kural hatası   → 409
 *     Beklenmeyen her şey       → 500
 *
 *   BÖLÜM 2 - SPRING'in hataları:
 *     Bozuk JSON, eksik alan, yanlış adres, yanlış HTTP metodu gibi.
 *     Bunları Spring zaten tanıyor ve durum kodunu kendisi belirliyor.
 *     Biz sadece cevabı kendi ErrorResponse biçimimize çeviriyoruz.
 *
 * @RestControllerAdvice: "Bu sınıf bütün controller'ların hatalarını yakalar" demek.
 * extends ResponseEntityExceptionHandler: Spring'in hazır hata yakalayıcısını
 *   miras alıyoruz; onun tanıdığı hataların cevaplarını değiştireceğiz.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Sunucu ekranına (log'a) mesaj yazmak için.
    private static final Logger logYazici = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Hata zamanını almak için saat.
    private final Clock saat;

    // Constructor injection (M7): Spring, AppConfig'teki saati buraya verir.
    public GlobalExceptionHandler(Clock saat) {
        this.saat = saat;
    }

    // =====================================================================
    // BÖLÜM 1 - BİZİM HATALARIMIZ
    //
    // @ExceptionHandler(X.class): "X türünde bir hata olursa bu metodu çalıştır"
    // =====================================================================

    /** Kayıt bulunamadı → 404. Örnek mesaj: "Oda bulunamadı: id=42" */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> bulunamadiHatasiniYakala(ResourceNotFoundException hata,
                                                                  HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.NOT_FOUND, hata.getMessage(), istek);
    }

    /** İş kuralı ihlali → 400. Örnek: "Geçersiz durum geçişi: RUNNING → PLANNED" */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> gecersizIstekHatasiniYakala(InvalidRequestException hata,
                                                                     HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.BAD_REQUEST, hata.getMessage(), istek);
    }

    /** Çakışma → 409. Örnek: "Kullanıcı adı zaten kullanılıyor" */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> cakismaHatasiniYakala(ConflictException hata,
                                                               HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.CONFLICT, hata.getMessage(), istek);
    }

    /**
     * Veritabanı bir kaydı kabul etmedi → 409.
     * Örnek: aynı kullanıcı adı ikinci kez eklenmeye çalışıldı.
     * Veritabanının teknik mesajını kullanıcıya göstermeyiz, sadece log'a yazarız.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> veritabaniHatasiniYakala(DataIntegrityViolationException hata,
                                                                  HttpServletRequest istek) {
        logYazici.warn("Veritabanı kural hatası: " + hata.getMostSpecificCause().getMessage());
        return cevapOlustur(HttpStatus.CONFLICT, "Kayıt mevcut verilerle çakışıyor", istek);
    }

    /**
     * Beklemediğimiz HER hata → 500.
     * Hatanın ayrıntısını sadece log'a yazarız, kullanıcıya sade bir mesaj göndeririz.
     * Böylece kodumuzun iç yapısı dışarı sızmaz.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> beklenmeyenHatayiYakala(Exception hata,
                                                                 HttpServletRequest istek) {
        logYazici.error("Beklenmeyen hata: " + istek.getMethod() + " " + istek.getRequestURI(), hata);
        return cevapOlustur(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu", istek);
    }

    // =====================================================================
    // BÖLÜM 2 - SPRING'İN HATALARI
    //
    // @Override: Üst sınıftaki (Spring'in) metodu kendi metodumuzla değiştiriyoruz.
    // Bu metotların adlarını Spring belirliyor, o yüzden İngilizce.
    // =====================================================================

    /**
     * Form doğrulama hatası → 400.
     *
     * DTO'daki @NotBlank, @Size gibi kurallara uymayan bir istek geldiğinde çalışır.
     * Hangi alanların neden hatalı olduğunu fieldErrors listesine koyarız.
     *
     * Örnek: {"name": ""} gönderildi, name alanında @NotBlank var →
     *   fieldErrors: [{"field": "name", "message": "boş değer olamaz"}]
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException hata,
                                                                  HttpHeaders basliklar,
                                                                  HttpStatusCode durum,
                                                                  WebRequest istek) {
        // Hatalı alanları tek tek listeye ekle.
        List<FieldErrorItem> alanHatalari = new ArrayList<>();
        for (FieldError alanHatasi : hata.getBindingResult().getFieldErrors()) {
            String alanAdi = alanHatasi.getField();
            String alanMesaji = alanHatasi.getDefaultMessage();
            alanHatalari.add(new FieldErrorItem(alanAdi, alanMesaji));
        }

        ErrorResponse cevap = ErrorResponse.of(saat, durum, "İstek doğrulanamadı",
                istekYolunuBul(istek), alanHatalari);

        // Cevabı Spring'in kendi metoduyla gönder.
        return super.handleExceptionInternal(hata, cevap, basliklar, durum, istek);
    }

    /**
     * Spring'in tanıdığı diğer bütün hatalar buradan geçer
     * (bozuk JSON, yanlış adres, yanlış HTTP metodu...).
     *
     * Spring normalde burada kendi cevap biçimini kullanır.
     * Biz onun yerine kendi ErrorResponse'umuzu koyuyoruz.
     * Durum kodunu (400, 404, 405...) Spring belirliyor, biz değiştirmiyoruz.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception hata,
                                                             Object springinCevabi,
                                                             HttpHeaders basliklar,
                                                             HttpStatusCode durum,
                                                             WebRequest istek) {
        String mesaj = turkceMesajSec(hata, springinCevabi, durum);
        ErrorResponse cevap = ErrorResponse.of(saat, durum, mesaj, istekYolunuBul(istek), null);

        // Cevabı Spring'in kendi metoduyla gönder (örnek: 405'teki "Allow" başlığı korunur).
        return super.handleExceptionInternal(hata, cevap, basliklar, durum, istek);
    }

    /**
     * Spring'in hatası için kullanıcıya gösterilecek Türkçe mesajı seçer.
     */
    private String turkceMesajSec(Exception hata, Object springinCevabi, HttpStatusCode durum) {

        // Bozuk JSON gönderildi. Örnek: {bozuk
        if (hata instanceof HttpMessageNotReadableException) {
            return "İstek gövdesi okunamadı";
        }

        // Adresteki bir değer yanlış türde. Örnek: /api/study-sessions/abc (sayı olmalıydı)
        if (hata instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException turHatasi = (MethodArgumentTypeMismatchException) hata;
            return turHatasi.getName() + " parametresi geçersiz";
        }

        // Zorunlu bir ?parametre= gönderilmedi.
        if (hata instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException eksikParametre = (MissingServletRequestParameterException) hata;
            return eksikParametre.getParameterName() + " parametresi eksik";
        }

        // Böyle bir adres yok. Örnek: /api/yok-boyle-bir-yer
        if (hata instanceof NoResourceFoundException) {
            return "Yol bulunamadı";
        }

        // 500'lü hatalar: ayrıntıyı log'a yaz, kullanıcıya sade mesaj göster.
        if (durum.is5xxServerError()) {
            logYazici.error("Sunucu hatası", hata);
            return "Beklenmeyen bir hata oluştu";
        }

        // Geri kalanlar (405, 415...): Spring'in hazır açıklamasını kullan.
        if (springinCevabi instanceof ProblemDetail) {
            ProblemDetail springAciklamasi = (ProblemDetail) springinCevabi;
            if (springAciklamasi.getDetail() != null) {
                return springAciklamasi.getDetail();
            }
        }
        return hata.getMessage();
    }

    // =====================================================================
    // YARDIMCI METOTLAR
    // =====================================================================

    /**
     * BÖLÜM 1'deki metotlar için hata cevabını oluşturur.
     */
    private ResponseEntity<ErrorResponse> cevapOlustur(HttpStatus durum,
                                                       String mesaj,
                                                       HttpServletRequest istek) {
        ErrorResponse cevap = ErrorResponse.of(saat, durum, mesaj, istek.getRequestURI(), null);
        return ResponseEntity.status(durum).body(cevap);
    }

    /**
     * İsteğin adresini bulur. Örnek: "/api/rooms/42"
     */
    private String istekYolunuBul(WebRequest istek) {
        ServletWebRequest servletIstegi = (ServletWebRequest) istek;
        return servletIstegi.getRequest().getRequestURI();
    }
}
