package com.roommates.common.error;

/**
 * HATALI TEK BİR ALAN
 *
 * Kullanıcı bir formu eksik ya da yanlış doldurduğunda, hangi alanın neden
 * hatalı olduğunu bu sınıfla bildiririz.
 *
 * JSON'daki görünümü:
 *   {"field": "name", "message": "boş değer olamaz"}
 *
 * "record" nedir?
 *   Sadece veri taşıyan basit bir sınıftır. Java, constructor'ı ve
 *   field() / message() okuma metotlarını bizim yerimize kendisi yazar.
 */
public record FieldErrorItem(
        String field,   // hatalı alanın adı, örnek: "name"
        String message  // hatanın açıklaması, örnek: "boş değer olamaz"
) {
}
