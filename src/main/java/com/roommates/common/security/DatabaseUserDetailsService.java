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

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository kullaniciDeposu;

    public DatabaseUserDetailsService(UserRepository kullaniciDeposu) {
        this.kullaniciDeposu = kullaniciDeposu;
    }

    @Override
    public UserDetails loadUserByUsername(String kullaniciAdi) throws UsernameNotFoundException {
        Optional<User> bulunan = kullaniciDeposu.findByUsernameWithRoles(kullaniciAdi);
        if (bulunan.isEmpty()) {
            throw new UsernameNotFoundException("Kullanıcı bulunamadı");
        }
        User kullanici = bulunan.get();

        Set<Role> roller = kullanici.getRoles();
        String[] rolAdlari = new String[roller.size()];
        int sira = 0;
        for (Role rol : roller) {
            rolAdlari[sira] = rol.getName();
            sira++;
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(kullanici.getUsername())
                .password(kullanici.getPassword())
                .authorities(rolAdlari)
                .disabled(!kullanici.isEnabled())
                .build();
    }
}
