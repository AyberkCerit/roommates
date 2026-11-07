package com.roommates.room;

import java.util.List;
import java.util.Optional;

/**
 * Oda kayıtlarına veritabanı erişimi (M18). Servis katmanı yalnız bu arayüzü görür;
 * EntityManager ayrıntısı RoomRepositoryImpl içinde kalır.
 */
public interface RoomRepository {

    /** Yalnız yeni oda kaydeder; id veritabanında üretilir. */
    Room save(Room room);

    Optional<Room> findById(Long id);

    List<Room> findAll();

    /** type ve visibility isteğe bağlıdır; null olan filtre uygulanmaz (M19). */
    List<Room> findByFilter(RoomType type, RoomVisibility visibility);

    Room update(Room room);

    void delete(Room room);
}
