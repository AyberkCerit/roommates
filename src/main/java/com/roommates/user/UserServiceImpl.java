package com.roommates.user;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.PersistenceException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roommates.common.error.ConflictException;
import com.roommates.common.error.InvalidRequestException;
import com.roommates.common.error.ResourceNotFoundException;
import com.roommates.user.dto.UserCreateRequest;
import com.roommates.user.dto.UserPatchRequest;
import com.roommates.user.dto.UserResponse;
import com.roommates.user.dto.UserUpdateRequest;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository kullaniciDeposu;
    private final RoleRepository rolDeposu;
    private final PasswordEncoder parolaSifreleyici;
    private final Clock saat;

    public UserServiceImpl(UserRepository kullaniciDeposu,
                           RoleRepository rolDeposu,
                           PasswordEncoder parolaSifreleyici,
                           Clock saat) {
        this.kullaniciDeposu = kullaniciDeposu;
        this.rolDeposu = rolDeposu;
        this.parolaSifreleyici = parolaSifreleyici;
        this.saat = saat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Boolean enabled) {
        List<User> kullanicilar = kullaniciDeposu.findByEnabled(enabled);

        List<UserResponse> cevaplar = new ArrayList<>();
        for (User kullanici : kullanicilar) {
            cevaplar.add(UserResponse.from(kullanici));
        }
        return cevaplar;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User kullanici = kullaniciyiBul(id);
        return UserResponse.from(kullanici);
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest istek) {
        kullaniciAdiBostaMi(istek.username(), null);
        epostaBostaMi(istek.email(), null);

        Set<Role> roller = rolleriBul(istek.roles());

        boolean aktifMi = true;
        if (istek.enabled() != null) {
            aktifMi = istek.enabled();
        }

        String sifreliParola = parolaSifreleyici.encode(istek.password());

        User yeniKullanici = new User(istek.username(), sifreliParola, istek.email(),
                istek.displayName(), aktifMi, Instant.now(saat));
        yeniKullanici.setRoles(roller);
        kullaniciDeposu.save(yeniKullanici);

        return UserResponse.from(yeniKullanici);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest istek) {
        User kullanici = kullaniciyiBul(id);

        kullaniciAdiBostaMi(istek.username(), id);
        epostaBostaMi(istek.email(), id);
        Set<Role> roller = rolleriBul(istek.roles());

        kullanici.setUsername(istek.username());
        kullanici.setPassword(parolaSifreleyici.encode(istek.password()));
        kullanici.setEmail(istek.email());
        kullanici.setDisplayName(istek.displayName());
        kullanici.setEnabled(istek.enabled());
        kullanici.setRoles(roller);

        User guncel = kullaniciDeposu.update(kullanici);
        return UserResponse.from(guncel);
    }

    @Override
    @Transactional
    public UserResponse patch(Long id, UserPatchRequest istek) {
        User kullanici = kullaniciyiBul(id);

        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        if (istek.username() != null) {
            kullaniciAdiBostaMi(istek.username(), id);
            kullanici.setUsername(istek.username());
        }
        if (istek.password() != null) {
            kullanici.setPassword(parolaSifreleyici.encode(istek.password()));
        }
        if (istek.email() != null) {
            epostaBostaMi(istek.email(), id);
            kullanici.setEmail(istek.email());
        }
        if (istek.displayName() != null) {
            kullanici.setDisplayName(istek.displayName());
        }
        if (istek.roles() != null) {
            kullanici.setRoles(rolleriBul(istek.roles()));
        }
        if (istek.enabled() != null) {
            kullanici.setEnabled(istek.enabled());
        }

        User guncel = kullaniciDeposu.update(kullanici);
        return UserResponse.from(guncel);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User kullanici = kullaniciyiBul(id);

        try {
            kullaniciDeposu.delete(kullanici);
            kullaniciDeposu.flush();
        } catch (DataIntegrityViolationException hata) {
            throw new ConflictException("Kullanıcının odaları veya oturumları olduğu için silinemez");
        } catch (PersistenceException hata) {
            throw new ConflictException("Kullanıcının odaları veya oturumları olduğu için silinemez");
        }
    }

    @Override
    @Transactional
    public void seedDefaultUsers(String adminPassword,
                                 String editorPassword,
                                 String viewerPassword,
                                 String inactivePassword) {
        Role viewer = roluBulVeyaOlustur(Role.VIEWER);
        Role editor = roluBulVeyaOlustur(Role.EDITOR);
        Role admin = roluBulVeyaOlustur(Role.ADMIN);

        Set<Role> adminRolleri = new HashSet<>();
        adminRolleri.add(viewer);
        adminRolleri.add(editor);
        adminRolleri.add(admin);
        ornekKullaniciOlustur("admin", adminPassword, "Yönetici", true, adminRolleri);

        Set<Role> editorRolleri = new HashSet<>();
        editorRolleri.add(viewer);
        editorRolleri.add(editor);
        ornekKullaniciOlustur("editor", editorPassword, "Editör", true, editorRolleri);

        Set<Role> viewerRolleri = new HashSet<>();
        viewerRolleri.add(viewer);
        ornekKullaniciOlustur("viewer", viewerPassword, "İzleyici", true, viewerRolleri);

        Set<Role> pasifRolleri = new HashSet<>();
        pasifRolleri.add(viewer);
        ornekKullaniciOlustur("pasif", inactivePassword, "Pasif Kullanıcı", false, pasifRolleri);
    }

    private User kullaniciyiBul(Long id) {
        Optional<User> bulunan = kullaniciDeposu.findById(id);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı", id);
        }
        return bulunan.get();
    }

    private void kullaniciAdiBostaMi(String kullaniciAdi, Long haricId) {
        Optional<User> bulunan = kullaniciDeposu.findByUsername(kullaniciAdi);
        if (bulunan.isPresent() && !bulunan.get().getId().equals(haricId)) {
            throw new ConflictException("Kullanıcı adı zaten kullanılıyor");
        }
    }

    private void epostaBostaMi(String eposta, Long haricId) {
        Optional<User> bulunan = kullaniciDeposu.findByEmail(eposta);
        if (bulunan.isPresent() && !bulunan.get().getId().equals(haricId)) {
            throw new ConflictException("E-posta zaten kullanılıyor");
        }
    }

    private Set<Role> rolleriBul(Set<String> rolAdlari) {
        Set<Role> roller = new HashSet<>();
        for (String rolAdi : rolAdlari) {
            Optional<Role> bulunan = rolDeposu.findByName(rolAdi);
            if (bulunan.isEmpty()) {
                throw new InvalidRequestException("Bilinmeyen rol: " + rolAdi);
            }
            roller.add(bulunan.get());
        }
        return roller;
    }

    private Role roluBulVeyaOlustur(String rolAdi) {
        Optional<Role> bulunan = rolDeposu.findByName(rolAdi);
        if (bulunan.isPresent()) {
            return bulunan.get();
        }
        return rolDeposu.save(new Role(rolAdi));
    }

    private void ornekKullaniciOlustur(String kullaniciAdi, String parola, String gorunenAd,
                                       boolean aktifMi, Set<Role> roller) {
        if (kullaniciDeposu.findByUsername(kullaniciAdi).isPresent()) {
            return;
        }

        String sifreliParola = parolaSifreleyici.encode(parola);
        String eposta = kullaniciAdi + "@roommates.local";
        Instant simdi = Instant.now(saat);

        User yeniKullanici = new User(kullaniciAdi, sifreliParola, eposta, gorunenAd, aktifMi, simdi);
        yeniKullanici.setRoles(roller);
        kullaniciDeposu.save(yeniKullanici);
    }
}
