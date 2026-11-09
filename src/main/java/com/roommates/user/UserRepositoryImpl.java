package com.roommates.user;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

/**
 * KULLANICI DEPOCUSU (gerçek kod)
 *
 * Her metot EntityManager'ın bir metodunu kullanır (M18):
 *   persist     → yeni kayıt ekle
 *   find        → numarayla bul
 *   createQuery → JPQL sorgusu çalıştır
 *   merge       → kaydı güncelle
 *   remove      → kaydı sil
 *
 * Bu sınıfta @Transactional YOK: transaction'ı servis katmanı başlatır (M20).
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager entityManager;

    // Constructor injection (M7)
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // persist: YENİ bir kullanıcı ekler. MySQL id'yi verir, nesneye yazılır.
    @Override
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }

    // find: birincil anahtarla (id) tek kayıt bulur. Yoksa null döner.
    @Override
    public Optional<User> findById(Long id) {
        User kullanici = entityManager.find(User.class, id);
        return Optional.ofNullable(kullanici);
    }

    // createQuery: bütün kullanıcıları numara sırasıyla getirir.
    @Override
    public List<User> findAll() {
        return entityManager
                .createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .getResultList();
    }

    // merge: var olan kaydı günceller.
    @Override
    public User update(User user) {
        return entityManager.merge(user);
    }

    // remove: kaydı siler.
    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }

    /**
     * Parametreli JPQL filtresi (M19).
     *
     * ":enabled IS NULL OR ..." kalıbı:
     *   enabled = null  → koşul hep doğru → bütün kullanıcılar gelir
     *   enabled = true  → sadece aktifler
     *   enabled = false → sadece pasifler
     *
     * setParameter: değeri sorguya güvenli şekilde yerleştirir (SQL injection olmaz).
     */
    @Override
    public List<User> findByEnabled(Boolean enabled) {
        return entityManager
                .createQuery("SELECT u FROM User u "
                        + "WHERE (:enabled IS NULL OR u.enabled = :enabled) "
                        + "ORDER BY u.id", User.class)
                .setParameter("enabled", enabled)
                .getResultList();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    /**
     * "LEFT JOIN FETCH u.roles": kullanıcıyı getirirken rollerini de aynı sorguda getir.
     *
     * Neden gerekli?
     *   Roller LAZY (sonradan yüklenen) bir ilişki. Giriş kontrolü sırasında açık bir
     *   transaction yok; rollere sonradan ulaşmaya çalışırsak "LazyInitializationException"
     *   hatası alırız. JOIN FETCH ile roller baştan yüklenir, sorun çıkmaz.
     */
    @Override
    public Optional<User> findByUsernameWithRoles(String username) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u LEFT JOIN FETCH u.roles "
                        + "WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> bulunanlar = entityManager
                .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return ilkiniDondur(bulunanlar);
    }

    // flush: bekleyen değişiklikleri hemen veritabanına gönderir.
    // Silme sırasında veritabanı hatasını anında görmek için kullanılır.
    @Override
    public void flush() {
        entityManager.flush();
    }

    /** Liste boşsa boş kutu, doluysa ilk elemanı döndürür. */
    private Optional<User> ilkiniDondur(List<User> liste) {
        if (liste.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(liste.get(0));
    }
}
