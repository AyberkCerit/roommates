package com.roommates.user;

import java.util.List;
import java.util.Optional;

/**
 * KULLANICI DEPOCUSU (arayüz)
 *
 * "Kullanıcılar tablosuyla hangi işleri yapabiliriz?" sorusunun listesi (M6, M18).
 * İşlerin nasıl yapıldığı UserRepositoryImpl sınıfındadır.
 *
 * Optional nedir?
 *   "Belki vardır, belki yoktur" kutusu. Kayıt bulunamazsa null yerine boş kutu döner.
 */
public interface UserRepository {

    // ---------- Temel işler (M18) ----------
    User save(User user);                 // ekle
    Optional<User> findById(Long id);     // numarayla bul
    List<User> findAll();                 // hepsini listele
    User update(User user);               // güncelle
    void delete(User user);               // sil

    // ---------- Sorgular ----------

    /** Aktif/pasif kullanıcıları listeler. null verilirse hepsini getirir (M19). */
    List<User> findByEnabled(Boolean enabled);

    /** Kullanıcı adıyla arar. Örnek: findByUsername("admin") */
    Optional<User> findByUsername(String username);

    /**
     * Kullanıcı adıyla arar ve rollerini de AYNI ANDA getirir.
     * Giriş sırasında kullanılır (bkz. DatabaseUserDetailsService).
     */
    Optional<User> findByUsernameWithRoles(String username);

    /** E-posta ile arar. */
    Optional<User> findByEmail(String email);

    /** Bekleyen değişiklikleri hemen veritabanına gönderir (silmede kullanılır). */
    void flush();
}
