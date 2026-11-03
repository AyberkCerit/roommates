# RoomMates — Vize Görev Dağılımı

Bu belge kimin neyi yapacağını, neyi kimden beklediğini, neyi kime teslim ettiğini ve sunumda neyi anlatacağını kişi kişi anlatır. Ayrıntılı adımlar uygulama planındadır: [`superpowers/plans/2026-11-03-roommates-vize.md`](superpowers/plans/2026-11-03-roommates-vize.md). Neyin neden böyle olduğu tasarım belgesindedir: [`superpowers/specs/2026-11-03-roommates-vize-design.md`](superpowers/specs/2026-11-03-roommates-vize-design.md). "Görev N" planın görev numarası, "§N" tasarımın bölümü, "MN" kontrol listesi maddesidir.

## 1. Kim kim?

| Harf | Modül | Kişi |
|---|---|---|
| A | Kullanıcılar ve güvenlik | _(ekip yazacak)_ |
| B | Odalar ve video kuyruğu | _(ekip yazacak)_ |
| C | Çalışma oturumları ve hata yönetimi | _(ekip yazacak)_ |
| D | Mülakat oturumları, yapılandırma ve belgeleme | _(ekip yazacak)_ |

**Öneri:** Aşama 0'ı (iskelet) Ayberk kurduğu için B'yi de Ayberk alsın. B'nin oda CRUD'u (Görev 8) herkesin kopyalayacağı referans kalıp ve kritik yolun ortasında. İskeleti kuran kişinin yazması gecikmeyi azaltır.

## 2. Genel bakış

| | A | B | C | D |
|---|---|---|---|---|
| Plan görevleri | 3, 7, 9 | 4, 8, 10, 15 | 5, 11, 12, 16b | 6, 13, 14, 16a |
| Sahip olduğu paketler | `user/`, `common/security/` | `room/`, `video/` | `study/`, `common/error/`, `postman/` (birleşik koleksiyon) | `interview/`, `common/config/OpenApiConfig`, `README.md` |
| Kontrol listesi maddeleri | M14, M31, M32, M33, M34, M35 | M18, M19, M20 (referans), M21–M26 (referans) | M10, M12, M28, M29, M30, M38 | M8, M9, M11, M13, M36, M37 |
| Uç noktaları | `/api/users` | `/api/rooms`, `/api/rooms/{roomId}/videos` | `/api/study-sessions` | `/api/interview-sessions` |
| PR'larını inceleyen | C | A | D | B |
| Tahmini iş | ~14 saat | ~16 saat | ~14 saat | ~13 saat |

İnceleme zinciri bir döngü: C → A → B → D → C. İnceleyici, incelediği kişinin işine en çok dokunan kişi:
- **C, A'yı inceler:** A'nın 401/403 cevapları C'nin hata modelini kullanıyor.
- **A, B'yi inceler:** B'nin oda sahibi, A'nın giriş yapan kullanıcısı.
- **B, D'yi inceler:** D, B'nin referans CRUD kalıbını kopyalıyor.
- **D, C'yi inceler:** Hata cevapları Swagger'da D'nin belgelediği şemayla aynı olmalı.

**Herkesin ortak sorumluluğu:** M1–M7 ve M15–M27. Ayrıca her modül kendi **Postman parçasını** (`postman/parts/<modul>.postman_collection.json`) teslim eder. Parça kendi kendine yeter: gereken odaları ve kullanıcıları kendisi oluşturur, seed kullanıcılarını silmez ve boş veritabanında tek başına yeşil çalışır. Bu, her görevin "bitti" ölçütüdür. Her kişi kendi modülünde katmanları, constructor injection'ı, entity kurallarını, EntityManager CRUD'unu, JPQL filtresini, `@Transactional`'ı ve beş HTTP metodunu uygular.

## 3. Zaman çizelgesi ve bekleme noktaları

```
Aşama 0  Ayberk + Claude: G1 → G2 ──────────────────────────────────┐
                                                                    ▼
Aşama 1  A: G3 ─────────┬─→ B: G4                         (herkes başlar)
         C: G5 ─────────┤
         D: G6 ─────────┴─→ A: G7          (G3 + G5 + G6 birleşti)
Aşama 2  B: G8 ◄──────────────────────── (G4 + G5 + G7 birleşti)
         C: G11 ◄─────────────────────── (G4 birleşti; G8'i beklemez)
         D: G13 ◄─────────────────────── (G1 yeter; erkenden yapılabilir)
         A: G9 ◄──────────────────────── (G7 + G8 birleşti)
         B: G10 ◄─────────────────────── (G8 birleşti)
         C: G12 ◄─────────────────────── (G8 + G11 birleşti)
         D: G14 ◄─────────────────────── (G8 + G13 birleşti)
         B: G15 ◄─────────────────────── (G10 + G12 + G14 birleşti)
Aşama 3  D: G16a (README) + C: G16b (Postman) ◄── (G15 birleşti)
         Hepsi: G17
```

**Kritik yol:** G1 → G2 → (G3 + G5 + G6) → G7 → G8 → G10 → G15 → G16a/G16b → G17. G12 ve G14 de G15'i besler ama B'nin G8 → G10 zinciri daha uzundur. Gecikmeler en çok bu zinciri etkiler. Bu yüzden:
- G6 **ilk gün** birleşir.
- G3, G5, G7 ve G8 PR'ları birleşir birleşmez, aynı gün incelenir.
- B'nin yükünü azaltmak için B, G10'a G8 incelemedeyken başlar.

**Beklerken yapılabilecekler:**
- **A** (G3 bitti, C'nin G5'ini bekliyor): G7'nin `DatabaseUserDetailsService` ve `SecurityConfig` kodunu yazmaya başlar. 401/403 işleyicileri G5 birleşince eklenir.
- **B** (G4 bitti, G7'yi bekliyor): G8'in DTO'larını ve servisini yazar. Sahip kontrolü G7 birleşince test edilir.
- **C** (G5 bitti): G11'i yapar, çünkü G11 yalnız G4'e bağlı.
- **D** (G6 bitti): G13'ü yapar, çünkü yalnız G1'e bağlı. Ardından README iskeletini yazmaya başlar.
- **A, C, D** (G8'i bekliyor): Kendi CRUD'larının DTO'larını, entity'lerini ve repository'lerini yazar. PR'ı G8 birleştikten sonra açar ve G8'in kalıbına uyar.

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
| 4 | **G15:** Oda silmede tek transaction'da çocuk kayıtları silme ve geri alma denemesi | G10, G12, G14 | M20'nin sunum kanıtı |

### Dikkat edilecekler
- **G8 birleşmeden önce kalıbı ekiple paylaş:** Diğer üç kişi bu dosyaları açıp kopyalayacak. Kod temiz ve yorumlu olmalı. G8 PR'ı açılınca ekip kanalına "kalıp hazır" mesajı atılır.
- **POST'ta id:** Gövdede id gönderilmesi mevcut kaydı değiştirmemeli. Servis her zaman `new Room(...)` + `persist` kullanır, asla `merge` (M23).
- **Oda sahibi:** Sahip gövdeden değil `Authentication.getName()`'den gelir.
- **G15'te dokunma kuralı:** G15'te başkalarının metotları **çağrılır**, onların dosyalarına dokunulmaz. `StudySessionService.deleteByRoomId` C'nin, `InterviewSessionRepository.deleteByRoomId` D'nin.

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
| 3 | **G12:** `/api/study-sessions` tam CRUD; `@Value` varsayılanları; durum geçişi; zamanlayıcı kaydı | G8, G11 | `StudySessionService.deleteByRoomId` (B'nin G15'i kullanır); Postman "Hata senaryoları/400-404" |
| 4 | **G16b:** `postman/parts/*` → tek koleksiyon + ortam dosyası; boş veritabanında Collection Runner iki kez yeşil | G15 | M38 teslimi |

### Dikkat edilecekler
- **G5 kritik yolda:** A'nın güvenlik işi G5'e bekliyor. G5 Aşama 1'in ilk günü bitmeli.
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
| 3 | **G14:** `/api/interview-sessions` tam CRUD; kategoriye göre soru seçimi | G8, G13 | `InterviewSessionRepository.deleteByRoomId` (B'nin G15'i kullanır) |
| 4 | **G16a:** README çalıştırma rehberi; Swagger'da 10 yolun eksiksiz göründüğünün kontrolü | G15 | M37 teslimi, M36'nın son kontrolü |

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
- **Dal ve commit:** Dal adı `feat/<harf>-<konu>`, ör. `feat/b-rooms`. Commit mesajları İngilizce (`feat(room): ...`).
- **PR:** Her görev ayrı bir PR. Açıklamada karşılanan M maddeleri ve `scripts/checks/g<N>.sh` çıktısı yer alır. İnceleyici §2'deki tabloya göre atanır.
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
