# Katkı Kuralları

Bu belge ekip üyeleri ve yapay zekâ asistanları içindir. Asistanınıza [docs/yapay-zeka-brifingi.md](docs/yapay-zeka-brifingi.md) dosyasını yapıştırın. Ayrıntılar için [tasarım](docs/superpowers/specs/2026-11-03-roommates-vize-design.md) (§10) ve [görev dağılımı](docs/gorev-dagilimi.md) belgelerine bakın.

## Dallar ve commit'ler

- Dal adı: `feat/<harf>-<konu>` (ör. `feat/a-security`, `feat/b-rooms`, `feat/c-study`, `feat/d-interview`).
- Commit mesajları İngilizce, Conventional Commits: `feat(room): add JPQL filter by type`, `fix(security): return JSON 401`.
- `main` korumalıdır: doğrudan push yok, PR ve 1 onay gerekir.

## Pull request kuralları

- Bir PR, bir görev.
- İnceleyen döngüsü: C, A'yı; A, B'yi; B, D'yi; D, C'yi inceler. Kendi PR'ınızı onaylamayın.
- PR açıklamasında karşıladığı M maddeleri listelenir.
- PR açıklamasına `scripts/checks/g<N>.sh` çıktısı yapıştırılır.

### PR kontrol listesi

- [ ] `@Autowired` yok, Spring Data yok, controller'da repository yok
- [ ] `@Transactional` importu `org.springframework.transaction.annotation.Transactional`
- [ ] Entity'de her alanda `@Column`
- [ ] Karşılanan M maddeleri yazıldı
- [ ] `scripts/checks/g<N>.sh` çıktısı PR açıklamasına yapıştırıldı

Yasaklı desen taraması (çıktı boş olmalı):

```bash
grep -rnE "@Autowired|JpaRepository|CrudRepository|PersistenceContext|jakarta.transaction.Transactional|com.fasterxml.jackson.databind" src/main/java
```

## Paket sahipliği

| Harf | Kişi | Modül | Paketler | İnceleyen |
|---|---|---|---|---|
| A | Şevval | Kullanıcılar ve güvenlik | `user/`, `common/security/` | C (Adem) |
| B | Pınar | Odalar ve video kuyruğu | `room/`, `video/` | A (Şevval) |
| C | Adem | Çalışma oturumları ve hata yönetimi | `study/`, `common/error/`, `postman/` (birleşik koleksiyon) | D (Ayberk) |
| D | Ayberk | Mülakat oturumları, yapılandırma, belgeleme | `interview/`, `common/config/OpenApiConfig`, `README.md` | B (Pınar) |

### Başkasının paketine dokunmadan önce sahibine sor

Sık paylaşılan dosyalar:

- `SecurityConfig`: A
- `GlobalExceptionHandler`: C
- `RoomServiceImpl`: B
- `application.properties`: iskelet; actuator/`info` ayarları için D

## Kontrol betikleri

Her görevin kontrolleri `scripts/checks/g<N>.sh` dosyasına yazılır ve çalışan uygulamaya karşı `bash -e scripts/checks/g<N>.sh` ile çalıştırılır (`-e`: ilk FAIL'de durur).

- İlk satır `source scripts/creds.sh`; `$A`, `$E`, `$V`, `$P` (admin, editor, viewer, pasif) kurulur.
- Geçici kullanıcı adlarına rastgele sonek ekleyin (`SFX=$RANDOM`, ör. `ayse$SFX`).
- Id'leri asla elle yazmayın: `check.sh` PASS'te ilk `"id"` değerini `/tmp/rm-last-id` dosyasına yazar; `X=$(cat /tmp/rm-last-id)` ile alın. Tek istisna, isteğin controller'a ulaşmadığı 401/403 kontrolleridir.
- Kullanıcı id'si: `scripts/user-id.sh <kullanici_adi>`.
- Kontroller tekrar çalıştırılabilir olmalı; her blok kendi verisini oluşturur.

### Windows / Git Bash

`scripts/user-id.sh` `mysql` istemcisini kullanır. MySQL'in `bin` klasörü `PATH`'te olmalıdır.

- `scripts/` altındaki betikler `.env.local`'daki CRLF satır sonlarını kendileri temizler. Dosyayı başka bir araçla okutacaksanız LF ile kaydedin (CRLF olursa parola sonuna görünmez `\r` eklenir).
- **IntelliJ ile çalıştırma:** IntelliJ `.env.local`'ı okumaz; parola boş kalır ve uygulama veritabanına bağlanamaz. Ya uygulamayı Git Bash'te `scripts/run-local.sh` ile başlatın ya da Run Configuration → *Environment variables* alanına `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` değerlerini girin (EnvFile eklentisiyle `.env.local` da gösterilebilir).

## Kurulum

Veritabanı: [docs/sql/01-create-db-user.sql](docs/sql/01-create-db-user.sql) (parola yer tutucusunu değiştirin). Yerel çalıştırma: `scripts/run-local.sh`. `.env.local` repoya girmez.
