package com.roommates.user;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KULLANICI SERVİSİ (gerçek kod): iş kuralları burada (M5).
 *
 * @Transactional (M20): metodun içindeki bütün veritabanı işleri tek paket hâlinde yapılır.
 *   Hepsi başarılı olursa kaydedilir; biri hata verirse hepsi geri alınır.
 *   Okuyan metotlarda readOnly = true, değiştirenlerde düz @Transactional.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository kullaniciDeposu;
    private final RoleRepository rolDeposu;
    private final PasswordEncoder parolaSifreleyici;   // Argon2id (SecurityConfig'te tanımlı)
    private final Clock saat;                          // AppConfig'te tanımlı

    // Constructor injection (M7)
    public UserServiceImpl(UserRepository kullaniciDeposu,
                           RoleRepository rolDeposu,
                           PasswordEncoder parolaSifreleyici,
                           Clock saat) {
        this.kullaniciDeposu = kullaniciDeposu;
        this.rolDeposu = rolDeposu;
        this.parolaSifreleyici = parolaSifreleyici;
        this.saat = saat;
    }

    // =====================================================================
    // ÖRNEK KULLANICILAR (uygulama açılışında)
    // =====================================================================

    @Override
    @Transactional
    public void seedDefaultUsers(String adminPassword,
                                 String editorPassword,
                                 String viewerPassword,
                                 String inactivePassword) {

        // 1) Üç rolü oluştur (yoksa).
        Role viewer = roluBulVeyaOlustur(Role.VIEWER);
        Role editor = roluBulVeyaOlustur(Role.EDITOR);
        Role admin = roluBulVeyaOlustur(Role.ADMIN);

        // 2) Dört kullanıcıyı bu sırayla oluştur (yoksa).
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

        // pasif kullanıcı: enabled = false → doğru parolayla bile giremez.
        Set<Role> pasifRolleri = new HashSet<>();
        pasifRolleri.add(viewer);
        ornekKullaniciOlustur("pasif", inactivePassword, "Pasif Kullanıcı", false, pasifRolleri);
    }

    /** Rol veritabanında varsa onu döndürür, yoksa oluşturur. */
    private Role roluBulVeyaOlustur(String rolAdi) {
        Optional<Role> bulunan = rolDeposu.findByName(rolAdi);
        if (bulunan.isPresent()) {
            return bulunan.get();
        }
        return rolDeposu.save(new Role(rolAdi));
    }

    /** Kullanıcı yoksa oluşturur. Varsa hiçbir şey yapmaz (yeniden başlatmada çoğalmasın). */
    private void ornekKullaniciOlustur(String kullaniciAdi, String parola, String gorunenAd,
                                       boolean aktifMi, Set<Role> roller) {
        if (kullaniciDeposu.findByUsername(kullaniciAdi).isPresent()) {
            return;
        }

        String sifreliParola = parolaSifreleyici.encode(parola);   // Argon2id ile şifrele
        String eposta = kullaniciAdi + "@roommates.local";
        Instant simdi = Instant.now(saat);

        User yeniKullanici = new User(kullaniciAdi, sifreliParola, eposta, gorunenAd, aktifMi, simdi);
        yeniKullanici.setRoles(roller);
        kullaniciDeposu.save(yeniKullanici);
    }
}
