package com.roommates.user;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * KULLANICI (veritabanındaki "users" tablosu)
 *
 * Tablodaki görünümü:
 *   id | username | password          | email                 | display_name | enabled | created_at
 *   1  | admin    | $argon2id$v=19... | admin@roommates.local | Yönetici     | 1       | 2026-11-09...
 *
 * Bir kullanıcının birden fazla rolü olabilir, bir rol de birden fazla kullanıcıda olabilir.
 * Bu "çoka çok" ilişki ayrı bir ara tabloda tutulur: user_roles (user_id, role_id).
 */
@Entity                  // Bu sınıf bir veritabanı tablosudur (M16)
@Table(name = "users")   // Tablonun adı "users"
public class User {

    @Id                                                   // Birincil anahtar (M17)
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Numarayı MySQL verir
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // 255 karakter: Argon2id ile şifrelenmiş parola ~100 karakter tutar,
    // hocanın istediği gibi "hash'in tamamı" sığar (M33).
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    // false ise kullanıcı doğru parolayla bile giremez (M32: "aktiflik bilgisi").
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Kullanıcının rolleri.
     *
     * @ManyToMany: çoka çok ilişki.
     * FetchType.LAZY: roller sadece gerektiğinde veritabanından okunur.
     * @JoinTable: ara tablonun adı ve sütunları.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /** Hibernate için parametresiz constructor (M17). */
    protected User() {
    }

    public User(String username, String password, String email, String displayName,
                boolean enabled, Instant createdAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.displayName = displayName;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // ---------- Getter ve setter'lar ----------
    // id için setter YOK: numarayı sadece veritabanı verir, kimse değiştiremez.

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
