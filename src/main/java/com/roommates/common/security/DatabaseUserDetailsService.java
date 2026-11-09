package com.roommates.common.security;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.roommates.user.Role;
import com.roommates.user.User;
import com.roommates.user.UserRepository;

/**
 * GİRİŞ YAPAN KULLANICIYI VERİTABANINDAN BULAN SINIF (M32)
 *
 * Her istekte Spring Security şunu sorar:
 *   "Bu kullanıcı adına sahip biri var mı? Parolası ne, rolleri ne, aktif mi?"
 * Bu sınıf cevabı veritabanından bulup Spring'e verir.
 * Parolanın doğru olup olmadığını Spring, PasswordEncoder ile kendisi karşılaştırır.
 *
 * Not: Bu sınıf servis katmanını atlayıp depocuyu (repository) doğrudan kullanıyor.
 * Bu bilinçli bir istisna: giriş kontrolü bir iş kuralı değil, altyapıdır.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository kullaniciDeposu;

    // Constructor injection (M7)
    public DatabaseUserDetailsService(UserRepository kullaniciDeposu) {
        this.kullaniciDeposu = kullaniciDeposu;
    }

    @Override
    public UserDetails loadUserByUsername(String kullaniciAdi) throws UsernameNotFoundException {

        // 1) Kullanıcıyı rolleriyle birlikte bul.
        //    Bulamazsak Spring'e "böyle biri yok" deriz → kullanıcı 401 alır.
        Optional<User> bulunan = kullaniciDeposu.findByUsernameWithRoles(kullaniciAdi);
        if (bulunan.isEmpty()) {
            throw new UsernameNotFoundException("Kullanıcı bulunamadı");
        }
        User kullanici = bulunan.get();

        // 2) Rol adlarını bir diziye koy. Örnek: ["ROLE_VIEWER", "ROLE_EDITOR"]
        Set<Role> roller = kullanici.getRoles();
        String[] rolAdlari = new String[roller.size()];
        int sira = 0;
        for (Role rol : roller) {
            rolAdlari[sira] = rol.getName();
            sira++;
        }

        // 3) Spring'in anladığı "UserDetails" nesnesini oluştur.
        //    Bizim User sınıfımızla karışmasın diye Spring'inkini tam adıyla yazıyoruz.
        //    disabled(true) ise Spring doğru parolada bile 401 "Hesap pasif" verir.
        return org.springframework.security.core.userdetails.User
                .withUsername(kullanici.getUsername())
                .password(kullanici.getPassword())        // veritabanındaki şifreli parola
                .authorities(rolAdlari)
                .disabled(!kullanici.isEnabled())
                .build();
    }
}
