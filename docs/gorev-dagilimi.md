# RoomMates — Vize Görev Dağılımı

Bu belge kimin neyi yapacağını, neyi kimden beklediğini, neyi kime teslim ettiğini ve sunumda neyi anlatacağını kişi kişi anlatır. Ayrıntılı adımlar uygulama planındadır: [`superpowers/plans/2026-11-03-roommates-vize.md`](superpowers/plans/2026-11-03-roommates-vize.md). Neyin neden böyle olduğu tasarım belgesindedir: [`superpowers/specs/2026-11-03-roommates-vize-design.md`](superpowers/specs/2026-11-03-roommates-vize-design.md). "Görev N" planın görev numarası, "§N" tasarımın bölümü, "MN" kontrol listesi maddesidir.

## 1. Kim kim?

| Harf | Modül | Kişi |
|---|---|---|
| A | Kullanıcılar ve güvenlik | Şevval (`sevvalkayaa3434-cell`) |
| B | Odalar ve video kuyruğu | Pınar (`Pinarylmz`) |
| C | Çalışma oturumları ve hata yönetimi | Adem (`ademcabuk`) |
| D | Mülakat oturumları, yapılandırma ve belgeleme | Ayberk (`AyberkCerit`) |

**Not:** B'nin oda CRUD'u (Görev 8) herkesin kopyalayacağı referans kalıp ve Aşama 1'in son adımı; Aşama 2 onu bekler. Pınar takılırsa iskeleti kuran Ayberk'e sorsun.

## 2. Genel bakış

| | A | B | C | D |
|---|---|---|---|---|
| Plan görevleri | 3, 7, 9 | 4, 8, 10, 15 | 5, 11, 12, 16b | 6, 13, 14, 16a |
| Sahip olduğu paketler | `user/`, `common/security/` | `room/`, `video/` | `study/`, `common/error/`, `postman/` (birleşik koleksiyon) | `interview/`, `common/config/OpenApiConfig`, `README.md` |
| Kontrol listesi maddeleri | M14, M31, M32, M33, M34, M35 | M18, M19, M20 (referans), M21–M26 (referans) | M10, M12, M28, M29, M30, M38 | M8, M9, M11, M13, M36, M37 |
| Uç noktaları | `/api/users` | `/api/rooms`, `/api/rooms/{roomId}/videos` | `/api/study-sessions` | `/api/interview-sessions` |
| Tahmini iş | ~14 saat | ~16 saat | ~14 saat | ~13 saat |

**İnceleme yok:** Herkes kendi modülünü yapıp doğrudan `main`'e push eder; PR ve onay yok. Bu belgede "birleşti", görevin `main`'e push edildiği anlamına gelir. Push etmeden önce kendi `scripts/checks/g<N>.sh` kontrolünüz PASS olmalı (ayrıntı `CONTRIBUTING.md`).

**Herkesin ortak sorumluluğu:** M1–M7 ve M15–M27. Ayrıca her modül kendi **Postman parçasını** (`postman/parts/<modul>.postman_collection.json`) teslim eder. Parça kendi kendine yeter: gereken odaları ve kullanıcıları kendisi oluşturur, seed kullanıcılarını silmez ve boş veritabanında tek başına yeşil çalışır. Bu, her görevin "bitti" ölçütüdür. Her kişi kendi modülünde katmanları, constructor injection'ı, entity kurallarını, EntityManager CRUD'unu, JPQL filtresini, `@Transactional`'ı ve beş HTTP metodunu uygular.

## 3. Aşamalar ve bekleme noktaları

Son tarih yok; herkes kendi işini bitirdiğinde push eder. Bekleme yalnız Aşama 1'de var.

```
Aşama 0  Ayberk + Claude: G1 → G2 (bitti)

Aşama 1  (ortak temel)
         A: G3 ─┬─→ B: G4 ─────────┐
         C: G5 ─┤                  ├─→ B: G8   ══ Aşama 1 biter
         D: G6 ─┴─→ A: G7 ─────────┘

Aşama 2  (sırasız: herkes istediği zaman yapar ve push eder)
         A: G9    B: G10 → G15    C: G11 → G12    D: G13 → G14

Aşama 3  D: G16a (README)   C: G16b (Postman)   Hepsi: G17
```

**Aşama 1 — ortak temel.** Herkesin kodu buna dayanır: kullanıcı ve oda sınıfları, hata sınıfları, giriş (örnek kullanıcılar) ve `/api/rooms` (herkesin kontrolleri oda oluşturur). İçindeki zincir:
- **Hemen:** Şevval G3, Adem G5, Ayberk G6. Birbirlerini beklemezler.
- **G3 gelince:** Pınar G4'ü push eder.
- **G3, G5, G6 gelince:** Şevval G7'yi push eder.
- **G4, G5, G7 gelince:** Pınar G8'i push eder. Bu, Aşama 1'in sonu.

Aşama 1 görevi bitiren `main`'e push edip ekip kanalına yazar ("G5 main'de" gibi). Bekleyen kişi o arada kodunu yazar, yalnız push'u bekler.

**Aşama 2 — sıra yok.** G8 `main`'e geldikten sonra kimse kimseyi beklemez. Her kişinin kendi görevleri arasındaki sıra (Pınar G10 → G15, Adem G11 → G12, Ayberk G13 → G14) yalnız o kişiyi ilgilendirir. Bunu sağlamak için:
- Oda silmede (G15) çalışma ve mülakat oturumlarını veritabanı siler: Adem ve Ayberk `room` ilişkisine `@OnDelete(action = OnDeleteAction.CASCADE)` koyar. Pınar onların kodunu çağırmaz.
- Aşama 1 bitmeden de yapılabilenler: G11 (yalnız G4'e bağlı) ve G13 (hiçbir şeye bağlı değil).
- Tek küçük istisna Postman: Ayberk'in mülakat parçası geçici kullanıcıları Şevval'in `POST /api/users`'ı (G9) ile oluşturur. Kod ve kontrol G9'u beklemez; parçanın kullanıcı adımları G9 gelince eklenir.

Tablolardaki "Bekler" sütunu Aşama 2'de yalnız Aşama 1'i ve kişinin kendi önceki görevlerini gösterir.

**Aşama 3.** README (G16a) her an yazılmaya başlanabilir; Swagger kontrolü dört CRUD gelince yapılır. Postman birleştirme (G16b) parçalar geldikçe yapılır, son hâli dört modül de gelince. Prova (G17) hep birlikte.

## 4. Kişi A — Kullanıcılar ve güvenlik

**Amaç:** API'ye kim girebilir, ne yapabilir? Kullanıcılar ve roller veritabanında, parolalar Argon2id, her yetkisiz istek doğru kodla ve aynı JSON gövdesiyle reddediliyor.

### Görevler

| Sıra | Görev | Bekler | Teslim eder (başkalarının kullandığı) |
|---|---|---|---|
| 1 | **G3:** `User`, `Role` entity'leri; `UserRepository`, `RoleRepository` | G2 | `UserRepository.findByUsername` (B ve D kullanır), `UserRepository.findById` (D kullanır), `User` entity'si (B, C, D'nin ilişkileri) |
| 2 | **G7:** `SecurityConfig` (yetki matrisi §7.2), Argon2id `PasswordEncoder`, `DatabaseUserDetailsService` (`JOIN FETCH` ile), JSON 401/403 işleyicileri, `UserSeeder` | G3, G5, G6 | Herkesin test ederken kullandığı 4 örnek kullanıcı ve 401/403 davranışı |
| 3 | **G9:** `/api/users` tam CRUD; rol adlarıyla atama, kullanıcı adı/e-posta için 409, ilişkili kaydı olan kullanıcıyı silmede 409 | G7, G8 | Postman'de "Kullanıcılar" ve "Hata senaryoları/401-403" klasörleri |

### Dikkat edilecekler
- **İki yerde kayıt:** Entry point hem `httpBasic(...)` hem `exceptionHandling(...)` içinde kayıtlı olmalı. Biri unutulursa yanlış parolada gövdesiz 401 döner (§6.3).
- **Kural sırası:** `/api/users/**` kuralı `/api/**` kurallarından önce gelmeli. PATCH de EDITOR yetkisi ister (M34).
- **Parola sızmasın:** Parola hiçbir cevapta (`UserResponse`) ve hiçbir log'da görünmemeli.
- **Katman istisnası:** `DatabaseUserDetailsService`'in repository'yi doğrudan çağırması bilerek yapılan tek istisna. Sunumda "kimlik doğrulama altyapıdır, iş kuralı değil" diye açıklanır.

### Sunumda anlatacakları (~4 dk)
1. `SecurityConfig`: `SecurityFilterChain`, HTTP Basic, kural tablosu (M31, M34).
2. Veritabanındaki `users` tablosu: `$argon2id$...` hash'i, `enabled` sütunu, `user_roles` (M32, M33).
3. Postman canlı gösterim:
   - auth yok → 401
   - yanlış parola → 401
   - `pasif` kullanıcı → 401
   - viewer ile PATCH → 403
   - editor ile DELETE → 403
   - admin ile DELETE → 204 (M35)
4. `/actuator/metrics`: anonim 401, editor 403, admin 200 (M14).

### Bitti sayılması için
- [ ] G7 ve G9'daki tüm `check.sh` kontrolleri PASS
- [ ] `grep -c password` kullanıcı listesi cevabında 0
- [ ] Uygulama yeniden başlatılınca kullanıcı sayısı değişmiyor
- [ ] Postman klasörleri `postman/parts/users.postman_collection.json`'da

## 5. Kişi B — Odalar ve video kuyruğu

**Amaç:** Projenin merkezindeki kaynak olan odalar. B'nin oda CRUD'u diğer üç modülün kopyalayacağı **referans uygulama**. Oda silme, dersin "neden transaction" sorusuna cevap veren örnek.

### Görevler

| Sıra | Görev | Bekler | Teslim eder |
|---|---|---|---|
| 1 | **G4:** `Room` entity'si, `RoomType`, `RoomVisibility`; `RoomRepository` (JPQL filtreli, her metotta EntityManager yorumu) | G2 (G3'ün `User`'ı) | `RoomRepository.findById` (C ve D kullanır), `Room` entity'si (video, çalışma ve mülakat ilişkileri) |
| 2 | **G8:** `/api/rooms` tam CRUD: DTO'lar, servis, controller, Swagger anotasyonları | G4, G5, G7 | **Referans kalıp:** DTO düzeni, PATCH kuralları, `Location` başlığı, Swagger `@ApiResponse` kalıbı. A, C ve D bunu kopyalar |
| 3 | **G10:** `/api/rooms/{roomId}/videos` tam CRUD, boşluksuz sıra (`shiftPositions`) | G8 | `VideoQueueItemRepository.deleteByRoomId` |
| 4 | **G15:** Oda silmede tek transaction'da videoları ve odayı silme, geri alma denemesi (oturumları veritabanı siler) | G10 | M20'nin sunum kanıtı |

### Dikkat edilecekler
- **Kalıbı ekiple paylaş:** Diğer üç kişi bu dosyaları açıp kopyalayacak. Kod temiz ve yorumlu olmalı. G8 `main`'e push edilince ekip kanalına "kalıp hazır" mesajı atılır.
- **POST'ta id:** Gövdede id gönderilmesi mevcut kaydı değiştirmemeli. Servis her zaman `new Room(...)` + `persist` kullanır, asla `merge` (M23).
- **Oda sahibi:** Sahip gövdeden değil `Authentication.getName()`'den gelir.
- **G15 kimseyi beklemez:** Oda silinince çalışma ve mülakat oturumlarını veritabanı siler (C ve D'nin `@OnDelete(action = OnDeleteAction.CASCADE)` ilişkisi). G15 yalnız videoları ve odayı siler.

### Sunumda anlatacakları (~6 dk)
1. Bir `POST /api/rooms` isteğinin kod içinde izlenmesi: `RoomController` → `RoomService`/`RoomServiceImpl` → `RoomRepository`/`RoomRepositoryImpl` → `persist` (M3–M6).
2. `RoomRepositoryImpl`: `persist`, `find`, `createQuery`, `merge`, `remove`, ve `setParameter`'lı JPQL filtresi (M18, M19). Canlı: `GET /api/rooms?type=STUDY`.
3. POST'ta `"id":1` gönderme → yeni kayıt, 1 numaralı oda değişmedi (M23).
4. PATCH ile tek alan değiştirme; id'li PATCH → 400 (M25).
5. Oda silme: videolar ve oturumlar da gitti. Kodda `@Transactional` ve geri alma denemesinin anlatımı (M20).

(Proje tanıtımını C yapar; B'nin iki ayrı konuşması tek bir uzun konuşmaya indirildi.)

### Bitti sayılması için
- [ ] G8 ve G10'daki tüm kontroller ile "Gözden kaçabilecek durumlar" 1–3 PASS
- [ ] G15'teki geri alma denemesi yapıldı ve denemenin geçici satırı silindi
- [ ] Postman: `rooms` ve `videos` klasörleri

## 6. Kişi C — Çalışma oturumları ve hata yönetimi

**Amaç:** API'nin her hatası tek tip, anlaşılır, Türkçe JSON. Çalışma odası modülü de Spring'in iki çekirdek özelliğini gösteriyor: `@Value` ve bean yaşam döngüsü.

### Görevler

| Sıra | Görev | Bekler | Teslim eder |
|---|---|---|---|
| 1 | **G5:** `ErrorResponse`, `FieldErrorItem`, 3 özel exception, `ErrorResponseWriter`, `GlobalExceptionHandler` (§6.3 tablosunun tamamı) | G2 | **Herkesin kullandığı** `ResourceNotFoundException`, `InvalidRequestException`, `ConflictException`; A'nın 401/403 için kullandığı `ErrorResponseWriter` |
| 2 | **G11:** `StudySession` entity'si, `StudySessionStatus.canTransitionTo`, repository, `StudyTimerRegistry` (`@PostConstruct`/`@PreDestroy`) | G4 | — |
| 3 | **G12:** `/api/study-sessions` tam CRUD; `@Value` varsayılanları; durum geçişi; zamanlayıcı kaydı | G8, G11 | `StudySession.room` ilişkisinde `@OnDelete(action = OnDeleteAction.CASCADE)` (oda silinince oturumlar gider); Postman "Hata senaryoları/400-404" |
| 4 | **G16b:** `postman/parts/*` → tek koleksiyon + ortam dosyası; boş veritabanında Collection Runner iki kez yeşil | parçalar geldikçe; son hâli dört modül gelince | M38 teslimi |

### Dikkat edilecekler
- **G5 Aşama 1'de:** Şevval'in güvenlik işi (G7) G5'i bekliyor. Bitince hemen push et ve ekibe yaz.
- **İsim çakışması:** Bizim `ErrorResponse` ile Spring'in `org.springframework.web.ErrorResponse`'u aynı adı taşıyor. `GlobalExceptionHandler`'da Spring'inki import edilmez.
- **`@PostConstruct` sınırları:** Bu metotta transaction yok. Yalnız okuma yapılır, LAZY ilişkiye dokunulmaz.
- **`@Value` parametre tipi:** `@Value` constructor parametresinde `int`. DTO'daki süre alanları ise `Integer` (null = varsayılanı kullan).

### Sunumda anlatacakları (~5 dk)
0. Açılış: proje fikri, paket yapısı, `pom.xml`'de Boot 4.1.2 ve Spring Data'nın olmayışı, MySQL'de tablolar; yeniden başlatınca verinin durduğu (M1, M2, M15–M17).
1. `GlobalExceptionHandler` ve `ErrorResponse` (M28, M29).
2. Postman: aynı gövde biçimiyle
   - olmayan id → 404
   - eksik alan → 400 + `fieldErrors`
   - bozuk JSON → 400
   - `/abc` → 400 (M30)
3. Süre göndermeden oturum açma → `focusMinutes: 25`. `application.properties` ve constructor'daki `@Value` gösterilir (M12).
4. Uygulamayı yeniden başlatıp açılış ve kapanış log'larını gösterme (M10).

### Bitti sayılması için
- [ ] G5'teki prob kontrolleri PASS, prob controller'ı silinmiş olarak birleşti
- [ ] G12'deki tüm kontroller PASS; `RUNNING` oturum yeniden başlatmada registry'ye yükleniyor
- [ ] Postman: `study` klasörü ve 400/404 senaryoları
- [ ] Birleşik koleksiyon boş veritabanında iki kez çalıştırılınca tamamen yeşil, seed kullanıcıları değişmemiş

## 7. Kişi D — Mülakat oturumları, yapılandırma ve belgeleme

**Amaç:** Spring'in bean yönetimi özellikleri (`@Bean`, `@Lazy`, `@Primary`/`@Qualifier`) mülakat modülünde gerçek bir işe bağlı olarak gösteriliyor. API'nin dış yüzü de D'de: Actuator, Swagger, README. (Birleşik Postman koleksiyonu C'de.)

### Görevler

| Sıra | Görev | Bekler | Teslim eder |
|---|---|---|---|
| 1 | **G6:** Actuator `health`/`info` özellikleri, `OpenApiConfig` (basicAuth şeması) | G2 | Swagger UI'da "Authorize"; `/actuator/info` |
| 2 | **G13:** `QuestionProvider`, iki implementasyon, `InterviewConfig` (`@Bean`, `@Primary`, `@Lazy`, `@Qualifier`) | G1 | — |
| 3 | **G14:** `/api/interview-sessions` tam CRUD; kategoriye göre soru seçimi | G8, G13 | `InterviewSession.room` ilişkisinde `@OnDelete(action = OnDeleteAction.CASCADE)` (oda silinince mülakatlar gider) |
| 4 | **G16a:** README çalıştırma rehberi; Swagger'da 10 yolun eksiksiz göründüğünün kontrolü | yok (Swagger kontrolü dört CRUD gelince) | M37 teslimi, M36'nın son kontrolü |

### Dikkat edilecekler
- **Tek tanım:** İki soru sağlayıcı **yalnız** `InterviewConfig`'te `@Bean`. Sınıflarda `@Component` yazılırsa bean iki kez tanımlanır ve uygulama açılmaz.
- **`@Lazy` iki yerde:** Hem bean tanımında hem `InterviewSessionServiceImpl` constructor parametresinde. Yalnız birinde olursa bean açılışta oluşur ve M9'un gösterimi bozulur.
- **README'nin denetimi:** README'yi D yazar, ama Görev 17'de README'yi yazmamış biri (önerim C) sıfırdan kurulum yaparak sınar.
- **README'nin içeriği:** README'deki örnek kullanıcı parolaları `application.properties`'teki seed varsayılanlarıyla aynı olmalı. G17'deki temiz kurulum bunu sınar.

### Sunumda anlatacakları (~5 dk)
1. `AppConfig.clock()` ve `InterviewConfig`: `@Configuration` + `@Bean` (M8).
2. Uygulama açılışında "Davranışsal soru bankası yüklendi" log'unun **olmadığını**, ilk BEHAVIORAL mülakatta **çıktığını** gösterme (M9).
3. TECHNICAL ve BEHAVIORAL mülakat oluşturma, gelen sorunun farklı sağlayıcıdan geldiğini gösterme. Constructor'daki `@Primary`'li ve `@Qualifier("behavioral")`'lı parametreler (M11).
4. `/actuator/health` ve `/actuator/info` (M13).
5. Swagger UI: Authorize, beş metot, istek/cevap modelleri, hata şeması (M36).
6. README (M37). Postman koleksiyonunu C gösterir (M38).

### Bitti sayılması için
- [ ] G14'teki kontroller ve "Gözden kaçabilecek durumlar" 4 PASS
- [ ] Swagger'da 5 kaynak × 5 metot ve tüm DTO şemaları görünüyor
- [ ] `/v3/api-docs` tam 10 yol listeliyor
- [ ] README'yi izleyen başka bir üye uygulamayı sıfırdan kurabildi

## 8. Ortak kurallar (özet)

Ayrıntı `CONTRIBUTING.md`'de.
- **Push:** Herkes doğrudan `main`'e push eder, PR yok. Önce `git pull --rebase`, `./mvnw -q package` ve `scripts/checks/g<N>.sh` PASS; `--force` yasak. Commit mesajları İngilizce (`feat(room): ...`), gövdede karşılanan M maddeleri.
- **Dosya sahipliği:** Başkasının paketindeki dosya değiştirilmez. Gerekirse sahibinden istenir. En çok karışabilecek dosyalar ve sahipleri:
  - `SecurityConfig` → A
  - `GlobalExceptionHandler` → C
  - `RoomServiceImpl` → B
  - `application.properties` → D (`info`/actuator) ve iskelet (diğer satırlar)
- **Yasaklar** (Görev 17'de grep'le aranır): `@Autowired`, `JpaRepository`, `CrudRepository`, `@PersistenceContext`, `jakarta.transaction.Transactional`, `com.fasterxml.jackson.databind`.

## 9. Sunum akışı (~20 dk)

| Sıra | Kim | Konu | Maddeler |
|---|---|---|---|
| 1 | C | Proje tanıtımı, mimari, paket yapısı, MySQL ve kalıcılık | M1, M2, M15–M17 |
| 2 | B | Bir isteğin yolculuğu, CRUD, JPQL, POST/PATCH kuralları, transaction | M3–M6, M18–M27 |
| 3 | D | Bean yönetimi: `@Bean`, `@Lazy`, `@Primary`/`@Qualifier` | M7, M8, M9, M11 |
| 4 | C | `@Value`, yaşam döngüsü, hata yönetimi | M10, M12, M28–M30 |
| 5 | A | Güvenlik: Basic, Argon2id, roller, 401/403, Actuator koruması | M14, M31–M35 |
| 6 | D + C | Actuator, Swagger, README (D); Postman Collection Runner (C) | M13, M36–M38 |

M7 (constructor injection) her konuşmacının gösterdiği sınıflarda görünür. D bunu üçüncü bölümde (bean yönetimi) açıkça söyler. Prova Görev 17'de bu sırayla yapılır.
