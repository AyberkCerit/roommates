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

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logYazici = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock saat;

    public GlobalExceptionHandler(Clock saat) {
        this.saat = saat;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> bulunamadiHatasiniYakala(ResourceNotFoundException hata,
                                                                  HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.NOT_FOUND, hata.getMessage(), istek);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> gecersizIstekHatasiniYakala(InvalidRequestException hata,
                                                                     HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.BAD_REQUEST, hata.getMessage(), istek);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> cakismaHatasiniYakala(ConflictException hata,
                                                               HttpServletRequest istek) {
        return cevapOlustur(HttpStatus.CONFLICT, hata.getMessage(), istek);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> veritabaniHatasiniYakala(DataIntegrityViolationException hata,
                                                                  HttpServletRequest istek) {
        logYazici.warn("Veritabanı kural hatası: " + hata.getMostSpecificCause().getMessage());
        return cevapOlustur(HttpStatus.CONFLICT, "Kayıt mevcut verilerle çakışıyor", istek);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> beklenmeyenHatayiYakala(Exception hata,
                                                                 HttpServletRequest istek) {
        logYazici.error("Beklenmeyen hata: " + istek.getMethod() + " " + istek.getRequestURI(), hata);
        return cevapOlustur(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu", istek);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException hata,
                                                                  HttpHeaders basliklar,
                                                                  HttpStatusCode durum,
                                                                  WebRequest istek) {
        List<FieldErrorItem> alanHatalari = new ArrayList<>();
        for (FieldError alanHatasi : hata.getBindingResult().getFieldErrors()) {
            String alanAdi = alanHatasi.getField();
            String alanMesaji = alanHatasi.getDefaultMessage();
            alanHatalari.add(new FieldErrorItem(alanAdi, alanMesaji));
        }

        ErrorResponse cevap = ErrorResponse.of(saat, durum, "İstek doğrulanamadı",
                istekYolunuBul(istek), alanHatalari);

        return super.handleExceptionInternal(hata, cevap, basliklar, durum, istek);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception hata,
                                                             Object springinCevabi,
                                                             HttpHeaders basliklar,
                                                             HttpStatusCode durum,
                                                             WebRequest istek) {
        String mesaj = turkceMesajSec(hata, springinCevabi, durum);
        ErrorResponse cevap = ErrorResponse.of(saat, durum, mesaj, istekYolunuBul(istek), null);

        return super.handleExceptionInternal(hata, cevap, basliklar, durum, istek);
    }

    private String turkceMesajSec(Exception hata, Object springinCevabi, HttpStatusCode durum) {
        if (hata instanceof HttpMessageNotReadableException) {
            return "İstek gövdesi okunamadı";
        }

        if (hata instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException turHatasi = (MethodArgumentTypeMismatchException) hata;
            return turHatasi.getName() + " parametresi geçersiz";
        }

        if (hata instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException eksikParametre = (MissingServletRequestParameterException) hata;
            return eksikParametre.getParameterName() + " parametresi eksik";
        }

        if (hata instanceof NoResourceFoundException) {
            return "Yol bulunamadı";
        }

        if (durum.is5xxServerError()) {
            logYazici.error("Sunucu hatası", hata);
            return "Beklenmeyen bir hata oluştu";
        }

        if (springinCevabi instanceof ProblemDetail) {
            ProblemDetail springAciklamasi = (ProblemDetail) springinCevabi;
            if (springAciklamasi.getDetail() != null) {
                return springAciklamasi.getDetail();
            }
        }
        return hata.getMessage();
    }

    private ResponseEntity<ErrorResponse> cevapOlustur(HttpStatus durum,
                                                       String mesaj,
                                                       HttpServletRequest istek) {
        ErrorResponse cevap = ErrorResponse.of(saat, durum, mesaj, istek.getRequestURI(), null);
        return ResponseEntity.status(durum).body(cevap);
    }

    private String istekYolunuBul(WebRequest istek) {
        ServletWebRequest servletIstegi = (ServletWebRequest) istek;
        return servletIstegi.getRequest().getRequestURI();
    }
}
