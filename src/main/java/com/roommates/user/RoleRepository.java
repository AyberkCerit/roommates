package com.roommates.user;

import java.util.List;
import java.util.Optional;

/**
 * ROL DEPOCUSU (arayüz)
 *
 * "Roller tablosuyla hangi işleri yapabiliriz?" sorusunun listesi.
 * İşlerin nasıl yapıldığı RoleRepositoryImpl sınıfındadır (M6).
 */
public interface RoleRepository {

    /** Yeni rolü kaydeder. */
    Role save(Role role);

    /** Adıyla rol arar. Örnek: findByName("ROLE_ADMIN") */
    Optional<Role> findByName(String name);

    /** Bütün rolleri listeler. */
    List<Role> findAll();
}
