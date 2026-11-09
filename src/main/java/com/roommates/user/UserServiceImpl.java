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

/**
 * KULLANICI SERVİSİ (gerçek kod): iş kuralları burada (M5).
 *
 * @Transactional (M20): metodun içindeki bütün veritabanı işleri tek paket hâlinde yapılır.
 *   Hepsi başarılı olursa kaydedilir; biri hata verirse hepsi geri alınır.
 *   Okuyan metotlarda readOnly = true, değiştirenlerde düz @Transactional.
 *
 * Hatalar için C'nin (ekibin ortak) exception sınıflarını kullanır:
 *   ResourceNotFoundException → 404, InvalidRequestException → 400, ConflictException → 409
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
    // LİSTELE VE GETİR (sadece okuma → readOnly = true)
    // =====================================================================

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

    // =====================================================================
    // EKLE (POST)
    // =====================================================================

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest istek) {

        // 1) Kullanıcı adı ve e-posta başkasında var mı? (null = yeni kayıt, kendisi yok)
        kullaniciAdiBostaMi(istek.username(), null);
        epostaBostaMi(istek.email(), null);

        // 2) Rol adlarını gerçek rol nesnelerine çevir. Bilinmeyen rol → 400.
        Set<Role> roller = rolleriBul(istek.roles());

        // 3) enabled gönderilmediyse aktif kabul et.
        boolean aktifMi = true;
        if (istek.enabled() != null) {
            aktifMi = istek.enabled();
        }

        // 4) Parolayı şifrele. Düz parola asla kaydedilmez (M33).
        String sifreliParola = parolaSifreleyici.encode(istek.password());

        // 5) HER ZAMAN "new" ile yeni nesne oluşturup persist ediyoruz (M23).
        //    Böylece istemci gövdede id gönderse bile var olan bir kayıt değişmez.
        User yeniKullanici = new User(istek.username(), sifreliParola, istek.email(),
                istek.displayName(), aktifMi, Instant.now(saat));
        yeniKullanici.setRoles(roller);
        kullaniciDeposu.save(yeniKullanici);

        return UserResponse.from(yeniKullanici);
    }

    // =====================================================================
    // TAM GÜNCELLE (PUT): bütün alanlar yazılır
    // =====================================================================

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest istek) {
        User kullanici = kullaniciyiBul(id);                // yoksa 404

        kullaniciAdiBostaMi(istek.username(), id);          // başkasında varsa 409
        epostaBostaMi(istek.email(), id);
        Set<Role> roller = rolleriBul(istek.roles());       // bilinmeyen rol 400

        kullanici.setUsername(istek.username());
        kullanici.setPassword(parolaSifreleyici.encode(istek.password()));
        kullanici.setEmail(istek.email());
        kullanici.setDisplayName(istek.displayName());
        kullanici.setEnabled(istek.enabled());
        kullanici.setRoles(roller);

        User guncel = kullaniciDeposu.update(kullanici);    // merge
        return UserResponse.from(guncel);
    }

    // =====================================================================
    // KISMİ GÜNCELLE (PATCH): sadece gönderilen alanlar değişir
    // =====================================================================

    @Override
    @Transactional
    public UserResponse patch(Long id, UserPatchRequest istek) {

        // Kural 1: Kayıt yoksa 404.
        User kullanici = kullaniciyiBul(id);

        // Kural 2: Gövdede id gönderilmişse 400 (birincil anahtar değiştirilemez).
        if (istek.id() != null) {
            throw new InvalidRequestException("Birincil anahtar (id) PATCH ile değiştirilemez");
        }

        // Kural 3: Sadece null OLMAYAN alanları değiştir.
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

    // =====================================================================
    // SİL (DELETE)
    // =====================================================================

    /**
     * Kullanıcının odası veya oturumu varsa veritabanı silmeye izin vermez
     * (foreign key kuralı). O hatayı yakalayıp 409'a çeviriyoruz.
     *
     * flush(): silme komutunu HEMEN veritabanına gönderir. Böylece hata
     * burada çıkar ve biz yakalayabiliriz. (flush olmasa hata metottan
     * çıktıktan sonra çıkardı, yakalayamazdık.)
     */
    @Override
    @Transactional
    public void delete(Long id) {
        User kullanici = kullaniciyiBul(id);   // yoksa 404

        try {
            kullaniciDeposu.delete(kullanici);
            kullaniciDeposu.flush();
        } catch (DataIntegrityViolationException hata) {
            throw new ConflictException("Kullanıcının odaları veya oturumları olduğu için silinemez");
        } catch (PersistenceException hata) {
            throw new ConflictException("Kullanıcının odaları veya oturumları olduğu için silinemez");
        }
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

    // =====================================================================
    // YARDIMCI METOTLAR
    // =====================================================================

    /** Kullanıcıyı numarasıyla bulur. Yoksa 404: "Kullanıcı bulunamadı: id=5" */
    private User kullaniciyiBul(Long id) {
        Optional<User> bulunan = kullaniciDeposu.findById(id);
        if (bulunan.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı", id);
        }
        return bulunan.get();
    }

    /**
     * Bu kullanıcı adı başka birinde var mı? Varsa 409.
     * haricId: güncellenen kullanıcının kendi numarası (kendisiyle çakışması sorun değil).
     *
     * Not: MySQL "Admin" ile "admin"i aynı sayar. Bu yüzden "Admin" eklemek de 409 verir.
     */
    private void kullaniciAdiBostaMi(String kullaniciAdi, Long haricId) {
        Optional<User> bulunan = kullaniciDeposu.findByUsername(kullaniciAdi);
        if (bulunan.isPresent() && !bulunan.get().getId().equals(haricId)) {
            throw new ConflictException("Kullanıcı adı zaten kullanılıyor");
        }
    }

    /** Bu e-posta başka birinde var mı? Varsa 409. */
    private void epostaBostaMi(String eposta, Long haricId) {
        Optional<User> bulunan = kullaniciDeposu.findByEmail(eposta);
        if (bulunan.isPresent() && !bulunan.get().getId().equals(haricId)) {
            throw new ConflictException("E-posta zaten kullanılıyor");
        }
    }

    /**
     * Rol adlarını gerçek rol nesnelerine çevirir.
     * Örnek: ["ROLE_VIEWER"] → {Role(id=1, ROLE_VIEWER)}
     * Bilinmeyen bir ad gelirse 400: "Bilinmeyen rol: ROLE_YOK"
     */
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
