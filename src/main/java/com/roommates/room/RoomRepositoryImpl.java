package com.roommates.room;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

/**
 * RoomRepository'nin EntityManager ile yazılmış gerçeklemesi (M18, M19).
 * Spring Data kullanılmaz. Diğer modüllerin repository'leri bu sınıfı örnek alır.
 * Burada @Transactional yok: transaction sınırı servis katmanındadır.
 */
@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final EntityManager entityManager;

    // Constructor injection (M7): Spring, paylaşılan EntityManager'ı bu tek constructor'dan verir.
    public RoomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // persist: yalnız yeni kayıt ekler (INSERT); gövdede id gelse bile mevcut kaydı ezemez.
    @Override
    public Room save(Room room) {
        entityManager.persist(room);
        return room;
    }

    // find: birincil anahtarla tek kayıt getirir; yoksa null döner, biz Optional'a çeviririz.
    @Override
    public Optional<Room> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Room.class, id));
    }

    // createQuery: JPQL ile tüm odaları id sırasıyla listeler.
    @Override
    public List<Room> findAll() {
        return entityManager
                .createQuery("SELECT r FROM Room r ORDER BY r.id", Room.class)
                .getResultList();
    }

    // createQuery + setParameter: null olan filtre "IS NULL" koşuluyla devre dışı kalır.
    // Değerler sorgu metnine eklenmez, parametre olarak bağlanır (SQL injection'a kapalı).
    @Override
    public List<Room> findByFilter(RoomType type, RoomVisibility visibility) {
        return entityManager
                .createQuery("""
                        SELECT r FROM Room r
                        WHERE (:type IS NULL OR r.type = :type)
                          AND (:visibility IS NULL OR r.visibility = :visibility)
                        ORDER BY r.id
                        """, Room.class)
                .setParameter("type", type)
                .setParameter("visibility", visibility)
                .getResultList();
    }

    // merge: entity'nin değişikliklerini kalıcı bağlama yazar ve yönetilen kopyayı döndürür (UPDATE).
    @Override
    public Room update(Room room) {
        return entityManager.merge(room);
    }

    // remove: yönetilen (aynı transaction'da yüklenmiş) entity'yi siler (DELETE).
    @Override
    public void delete(Room room) {
        entityManager.remove(room);
    }
}
