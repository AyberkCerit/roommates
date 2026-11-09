package com.roommates.user;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

/**
 * ROL DEPOCUSU (gerçek kod)
 *
 * Veritabanıyla EntityManager üzerinden konuşur (M6). Spring Data kullanmıyoruz (M2).
 *
 * EntityManager nedir?
 *   Hibernate'in "veritabanı ile konuşma aracı". Nesneyi kaydetme, bulma,
 *   silme ve sorgu çalıştırma işlerini o yapar.
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final EntityManager entityManager;

    // Constructor injection (M7): EntityManager'ı Spring verir.
    public RoleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // persist: YENİ bir kaydı veritabanına ekler.
    @Override
    public Role save(Role role) {
        entityManager.persist(role);
        return role;
    }

    // createQuery + setParameter: parametreli JPQL sorgusu (M19).
    @Override
    public Optional<Role> findByName(String name) {
        List<Role> bulunanlar = entityManager
                .createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", name)
                .getResultList();

        if (bulunanlar.isEmpty()) {
            return Optional.empty();       // bulunamadı
        }
        return Optional.of(bulunanlar.get(0));
    }

    // createQuery: bütün rolleri numara sırasıyla getirir.
    @Override
    public List<Role> findAll() {
        return entityManager
                .createQuery("SELECT r FROM Role r ORDER BY r.id", Role.class)
                .getResultList();
    }
}
