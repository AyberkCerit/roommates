package com.roommates.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ROL (veritabanındaki "roles" tablosu)
 *
 * Bir kullanıcının ne yapabileceğini belirler:
 *   ROLE_VIEWER → sadece okuyabilir (GET)
 *   ROLE_EDITOR → ekleyip güncelleyebilir (POST, PUT, PATCH)
 *   ROLE_ADMIN  → silebilir ve kullanıcıları yönetebilir (DELETE, /api/users)
 *
 * Tablodaki görünümü:
 *   id | name
 *   1  | ROLE_VIEWER
 *   2  | ROLE_EDITOR
 *   3  | ROLE_ADMIN
 */
@Entity                  // Bu sınıf bir veritabanı tablosudur (M16)
@Table(name = "roles")   // Tablonun adı "roles"
public class Role {

    // Rol adlarını her yerde aynı yazmak için sabitler.
    public static final String VIEWER = "ROLE_VIEWER";
    public static final String EDITOR = "ROLE_EDITOR";
    public static final String ADMIN = "ROLE_ADMIN";

    @Id                                                   // Birincil anahtar (M17)
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Numarayı MySQL verir: 1, 2, 3...
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    /**
     * Parametresiz constructor (M17).
     * Hibernate, veritabanından okuduğu satırı nesneye çevirirken bunu kullanır.
     * "protected": bizim kodumuz yanlışlıkla boş rol oluşturmasın diye.
     */
    protected Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
