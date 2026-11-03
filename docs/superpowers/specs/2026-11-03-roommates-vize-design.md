# RoomMates — Vize Aşaması Tasarım Belgesi

- **Tarih:** 2026-11-03
- **Ders:** YZMSEC319 Sunucu Uygulama Geliştirme (Dr. Öğr. Üyesi Samet Tonyalı)
- **Ekip:** 4 kişi (A Şevval, B Pınar, C Adem, D Ayberk)
- **Kaynak belgeler:** `../../../../Proje_Teknik_Gereklilikler_Kontrol_Listesi.pdf` (vize değerlendirme kriterleri), `../../../../YZMSEC319 - Bologna.pdf` (dersin bütünü), `../../../../proje-onerisi.md` (proje önerisi), `../../../../w2g-analiz/RAPOR.md` (w2g davranış analizi)

---

## 1. Amaç ve gerekçe

RoomMates, öğrencilerin internet üzerinden üç tür odada buluştuğu bir platformun **sunucu tarafıdır**: izleme odası (YouTube videolarını sıraya ekleyip birlikte izleme), çalışma odası (ortak pomodoro zamanlayıcısı) ve mülakat odası (sırayla mülakatçı/aday rolü). Fikir w2g.tv'den çıktı; RoomMates onu ders çalışma ve mülakat pratiğiyle genişletiyor.

Ders bir REST API dersidir. Bologna belgesi açıkça "ön yüz beklenmez" der ve sunucuda HTML üretimini kapsam dışı tutar. Bu yüzden projenin değeri, odaları, kullanıcıları ve oda içi kayıtları yöneten **iyi tasarlanmış, güvenli, belgelenmiş bir REST API**'dir.

Proje iki aşamalıdır:

| Aşama | Ölçüt | Bu belgede |
|---|---|---|
| **Vize** | Teknik Gereklilikler Kontrol Listesi (38 madde, aşağıda M1–M38) | **Tamamı** |
| **Final** | Bologna öğrenme çıktıları Ö01–Ö09 | Yalnız yön (bölüm 11) |

Vize sunumunda her madde, çalışan uygulama üzerinde bir istek veya kod parçasıyla gösterilebilmelidir.

## 2. Hedefler, hedef olmayanlar, başarısızlık sayılacaklar

**Hedefler (vize):**
- Kontrol listesindeki 38 maddenin hepsi karşılanmış ve gösterilebilir.
- Dört modülün her biri kendi kaynağı üzerinde GET (liste + tek kayıt), POST, PUT, PATCH, DELETE sunar. Böylece her ekip üyesi sunumda kendi modülünü baştan sona anlatabilir.
- Kod, finalde JWT, Spring Data JPA (gerekirse), sayfalama, testler ve Spring AI eklenirken baştan yazılmayacak biçimde katmanlı ve arayüz arkasında.

**Hedef olmayanlar (vize):**
- Ön yüz, HTML sayfaları, şablon motoru.
- Gerçek zamanlı senkron (WebSocket/STOMP). w2g analizinden çıkan "durum sunucuda" kararı geçerli ama vize kapsamında değil.
- JWT, kayıt sahipliği denetimi, CORS, sayfalama/sıralama, birim ve entegrasyon testleri, Spring AI, profiller, Docker (hepsi finale).

**Başarısızlık sayılacaklar (çalışsa bile):**
- `JpaRepository`, `CrudRepository` veya Spring Data REST'in herhangi bir yerde kullanılması.
- Herhangi bir sınıfta `@Autowired` alan (field) veya setter enjeksiyonu.
- Controller'da `EntityManager` ya da repository çağrısı.
- Parola hash'inin herhangi bir API cevabında görünmesi.
- Aynı hata durumunda farklı modüllerin farklı JSON gövdesi döndürmesi.
- POST gövdesindeki `id` yüzünden mevcut bir kaydın üzerine yazılması.

## 3. Kısıtlar

| Kısıt | Değer | Kaynak |
|---|---|---|
| Spring Boot | **4.1.2** (Initializr'da mevcut, doğrulandı) | M1 |
| Java | 25 (ekip bilgisayarlarında kurulu sürüm; Boot 4.1.2 ile uyumlu) | ortam |
| Derleme | Maven Wrapper (`./mvnw`) | ders kodları |
| Veritabanı | MySQL 8.x; Ayberk'in makinesinde 8.4.11 kurulu, `roommates` DB ve kullanıcısı hazır | M15 |
| JPA | Hibernate 7 (Boot 4 ile gelen), yalnız `EntityManager` | M2, M6 |
| JSON | Jackson 3.1 (Boot 4 varsayılanı). Databind/`JsonMapper` `tools.jackson.*`, anotasyonlar (`@JsonInclude` vb.) hâlâ `com.fasterxml.jackson.annotation`. springdoc Jackson 2'yi de getirir: `com.fasterxml.jackson.databind` **import edilmez** | M27 |
| Güvenlik | Spring Security 7, HTTP Basic | M31 |
| Belgeleme | springdoc-openapi **3.2.0** (Boot 4.1.2'ye karşı derlenmiş; doğrulandı) | M36 |
| Git | GitHub, commit mesajları İngilizce Conventional Commits | ekip kuralı |

**Bağımlılık seçimi (bizim kararımız):** `spring-boot-starter-data-jpa` Spring Data JPA'yı sınıf yoluna getirir. Kontrol listesi kullanımını yasaklıyor, varlığını değil; ama değerlendirmede tartışma çıkmasın diye Spring Data'yı hiç eklemiyoruz. Yerine `spring-boot-starter-jdbc` + `spring-boot-hibernate` (Boot 4'ün Hibernate otomatik yapılandırma modülü; `spring-boot-jpa`, `hibernate-core`, `spring-orm` getirir) kullanılır. Doğrulandı (4.1.2 BOM: Framework 7.0.10, Security 7.1.2, Hibernate 7.4.13, Jackson 3.1.7): `spring-boot-jpa` `entityManagerFactory` ve `transactionManager` bean'lerini tanımlıyor; Framework 7'de paylaşılan `EntityManager` doğrudan **constructor'a enjekte edilebiliyor**, `@PersistenceContext` alan enjeksiyonu gerekmiyor (M7 için önemli). Spring Data'lı starter'a geri dönüş planlanmıyor.

Diğer bağımlılıklar: `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-actuator`, `mysql-connector-j` (runtime), `org.bouncycastle:bcprov-jdk18on` (Argon2 için zorunlu; Boot BOM yönetmediği için sürüm `pom.xml`'de açıkça yazılır), `springdoc-openapi-starter-webmvc-ui`.

## 4. Mimari

### 4.1 Katmanlar ve istek akışı (M3–M6)

```
HTTP  →  XController (@RestController)
           │  yalnız: URL/HTTP eşleme, @RequestBody/@PathVariable, ResponseEntity
           ▼
         XService (arayüz)  ←  XServiceImpl (@Service, @Transactional)
           │  iş kuralları, DTO ↔ entity dönüşümü, "bulunamadı" kararı
           ▼
         XRepository (arayüz)  ←  XRepositoryImpl (@Repository)
           │  yalnız EntityManager: persist / find / createQuery / merge / remove
           ▼
         MySQL
```

- Controller yalnız Service arayüzüne bağımlıdır. Service yalnız Repository arayüzlerine bağımlıdır; başka modülün kaydı gerekiyorsa o modülün **repository'sini** kullanır, servisini değil (constructor döngülerini önler). Tek istisna `RoomServiceImpl → StudySessionService.deleteByRoomId` (§5.3).
- Her bağımlılık `private final` alan + constructor parametresi. Tek constructor olduğu için `@Autowired` yazılmaz (M7). `EntityManager` de constructor'dan alınır.
- `@Transactional` **her zaman** `org.springframework.transaction.annotation.Transactional` (M20); `jakarta.transaction.Transactional` kullanılmaz. Okuma metotları `@Transactional(readOnly = true)`.
- İstisna: `DatabaseUserDetailsService` Spring Security'nin arayüzüdür ve `UserRepository`'yi doğrudan çağırır (kimlik doğrulama bir iş kuralı değil, altyapı). Sunumda bu tek cümleyle açıklanır.
- Entity'ler controller'dan dışarı çıkmaz; cevaplar DTO'dur (parola hash'i sızmasın diye ve Bologna hafta 4–5'teki entity–DTO ayrımı için).

### 4.2 Paket yapısı (özellik bazlı)

Dört kişinin aynı dosyalarda çakışmaması için paketler modüle göre ayrılır:

```
com.roommates
├── RoomMatesApplication.java
├── common/
│   ├── config/        AppConfig (Clock @Bean), OpenApiConfig
│   ├── error/         ErrorResponse, GlobalExceptionHandler, *Exception
│   └── security/      SecurityConfig, JSON 401/403 işleyicileri, UserDetailsService
├── user/              User, Role, UserRepository(+Impl), UserService(+Impl), UserController, dto/
├── room/              Room, RoomType, RoomVisibility, Room* katmanları, dto/
├── video/             VideoQueueItem, VideoQueue* katmanları, dto/
├── study/             StudySession, StudySessionStatus, StudySession* katmanları, StudyTimerRegistry, dto/
└── interview/         InterviewSession, InterviewStatus, Interview* katmanları,
                       QuestionProvider + 2 implementasyon, InterviewConfig, dto/
```

### 4.3 Veri modeli

Tüm entity'lerde: `@Entity`, `@Table(name=...)`, **her kalıcı alanda** `@Column(name=...)` (ilişkilerde `@JoinColumn`), `protected` parametresiz constructor, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` (MySQL AUTO_INCREMENT için uygun strateji) (M16, M17). Enum'lar `@Enumerated(EnumType.STRING)`.

| Entity / tablo | Alanlar (sütun) | İlişkiler | Sahip |
|---|---|---|---|
| `Role` / `roles` | id, name (`ROLE_VIEWER`, `ROLE_EDITOR`, `ROLE_ADMIN`; unique) | — | A |
| `User` / `users` | id, username (unique, 50), password (**255**, Argon2id hash'in tamamı), email (unique), display_name, enabled (boolean), created_at | `@ManyToMany` Role → `user_roles(user_id, role_id)` | A |
| `Room` / `rooms` | id, name, description, type (WATCH/STUDY/INTERVIEW), visibility (PUBLIC/PRIVATE), max_members, created_at, closes_at (nullable) | `@ManyToOne` owner → User (`owner_id`) | B |
| `VideoQueueItem` / `video_queue_items` | id, youtube_video_id, title, position, added_at | `@ManyToOne` room (`room_id`), `@ManyToOne` added_by → User | B |
| `StudySession` / `study_sessions` | id, title, focus_minutes, break_minutes, rounds, status (PLANNED/RUNNING/FINISHED), started_at (nullable) | `@ManyToOne` room (`room_id`) | C |
| `InterviewSession` / `interview_sessions` | id, topic, category (TECHNICAL/BEHAVIORAL), question, status (SCHEDULED/IN_PROGRESS/COMPLETED), scheduled_at | `@ManyToOne` room, `@ManyToOne` interviewer → User, `@ManyToOne` candidate → User | D |

İstek DTO'larında sayısal alanlar **sarmalayıcı tip** (`Integer`, `Long`, `Boolean`): Jackson 3'te `FAIL_ON_NULL_FOR_PRIMITIVES` varsayılan açık; `int` kullanılırsa eksik alan doğrulama hatası yerine "gövde okunamadı" döner ve `@Value` varsayılanları çalışmaz.

İlişkiler `FetchType.LAZY`. Cevap DTO'ları ilişkili kayıtların yalnız `id` (ve gerekiyorsa `username`/`name`) alanını taşır; servis DTO'yu transaction içinde kurar, böylece lazy yükleme hatası olmaz.

**Kalıcılık (M15):** `spring.jpa.hibernate.ddl-auto=update`. Uygulama yeniden başlatıldığında tablolar ve veriler korunur; `create`/`create-drop` **yasak**. Bağlantı bilgileri ortam değişkenlerinden okunur (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), varsayılanları `application.properties`'te yer tutucu olarak durur; her geliştiricinin kendi `.env.local`'ı vardır ve git'e girmez, şablonu `.env.example`.

## 5. REST sözleşmesi

### 5.1 Uç noktalar (M21–M26)

Kaynaklar çoğul isimle, fiilsiz adlandırılır. İç içe kaynak yalnız video kuyruğunda (video bir odaya aittir).

| Kaynak | Uç noktalar | JPQL filtresi (M19) | Sahip |
|---|---|---|---|
| Kullanıcılar | `/api/users`, `/api/users/{id}` | `GET /api/users?enabled=true` | A |
| Odalar | `/api/rooms`, `/api/rooms/{id}` | `GET /api/rooms?type=STUDY&visibility=PUBLIC` (ikisi de isteğe bağlı) | B |
| Video kuyruğu | `/api/rooms/{roomId}/videos`, `/api/rooms/{roomId}/videos/{videoId}` | liste zaten `roomId` ile filtreli, `position`'a göre sıralı | B |
| Çalışma oturumları | `/api/study-sessions`, `/api/study-sessions/{id}` | `GET /api/study-sessions?roomId=..&status=..` | C |
| Mülakat oturumları | `/api/interview-sessions`, `/api/interview-sessions/{id}` | `GET /api/interview-sessions?status=..` | D |

Her kaynakta:

| Metot | Yol | Başarı | Kurallar |
|---|---|---|---|
| GET | koleksiyon | 200 + dizi | filtre parametreleri `@RequestParam(required=false)` |
| GET | `/{id}` | 200 | id `@PathVariable`; yoksa 404 |
| POST | koleksiyon | **201** + oluşan kayıt (üretilen `id` dahil) + `Location` başlığı | aşağıda "POST ve id" |
| PUT | `/{id}` | 200 + güncel kayıt | tam güncelleme: tüm zorunlu alanlar gövdede olmalı, eksikse 400 |
| PATCH | `/{id}` | 200 + güncel kayıt | aşağıda "PATCH kuralları" |
| DELETE | `/{id}` | **204** | yoksa 404 |

### 5.2 DTO kuralları

Her kaynak için üç istek DTO'su ve bir cevap DTO'su (Java `record`):

- Tüm istek record'ları `@JsonIgnoreProperties(ignoreUnknown = true)` taşır.
- `XCreateRequest` — **`id` alanı yok**. Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Min`…). Controller'da `@Valid @RequestBody`.
- `XUpdateRequest` (PUT) — `id` yok; tüm alanlar zorunlu. Hangi kaydın güncelleneceğini yalnız URL'deki id belirler.
- `XPatchRequest` (PATCH) — tüm alanlar nullable (`null` = "gönderilmedi"). İçinde ayrıca `Long id` alanı vardır; **yalnız reddetmek için**.
- `XResponse` — `id` dahil, istemcinin görmesi gereken alanlar. `UserResponse`'ta parola **yok**.

**POST ve id (M23):** İstek record'larındaki `@JsonIgnoreProperties(ignoreUnknown = true)` bilinmeyen alanları yok sayar; `XCreateRequest`'te `id` olmadığı için gövdedeki `id` hiçbir yere ulaşmaz. Servis her zaman `new X(...)` kurar ve repository `persist` çağırır (asla `merge`). Böylece `{"id": 1, ...}` gönderilse bile 1 numaralı kayıt değişmez, yeni kayıt oluşur ve yeni id döner. Sunumda bu bir Postman isteğiyle gösterilir.

**PATCH kuralları (M25):**
1. Kayıt yoksa 404.
2. Gövdede `id` gönderilmişse (null değilse) → **400** `"Birincil anahtar (id) PATCH ile değiştirilemez"`.
3. Yalnız null olmayan alanlar entity'ye uygulanır; diğerleri olduğu gibi kalır. **Bilinen sınır (bizim kararımız):** PATCH ile nullable bir alan (`description`, `closesAt`) boşaltılamaz; bunun için PUT kullanılır. README ve Swagger açıklamasında yazılır.
4. Uygulanan alanlar aynı iş kurallarından geçer (ör. `focusMinutes` 1–180 arası); `@Valid` patch DTO'daki `@Size`/`@Min` gibi kısıtlar null'da tetiklenmez, doluysa tetiklenir.
5. Değişiklik `@Transactional` serviste yapılır; yönetilen entity'nin alanları değiştiği için ayrıca `merge` gerekmez, ama repository arayüzünde tutarlılık için `update(entity)` (`merge`) çağrılır.

**PUT:** Tüm alanlar entity'ye yazılır; `id` asla değişmez çünkü DTO'da yok.

### 5.3 İlişkili kayıtlar ve iş kuralları

- **Oda sahibi:** `POST /api/rooms`'ta sahip gövdeden alınmaz; kimliği doğrulanmış kullanıcıdır (`Authentication.getName()` → `UserRepository.findByUsername`). Sahiplik **denetimi** (yalnız sahibin silmesi) finale kalır.
- **Video ekleme:** `addedBy` yine giriş yapan kullanıcı. `position` verilmezse kuyruğun sonuna eklenir (kuyruktaki video sayısı + 1; sıra boşluksuz olduğu için `max(position)` ile aynı).
- **Video sırası (bizim kararımız):** `position` 1'den başlayan, boşluksuz sıra. PUT/PATCH ile bir videonun `position`'ı değişince aradaki videolar bir kayar; silinen videodan sonrakiler bir yukarı çeker; geçersiz konum (≤0 veya kuyruk boyunu aşan) 400. Hepsi tek transaction'da. Sütunda unique kısıtı yok (kaydırma sırasında geçici çakışma olmasın diye).
- **Çalışma oturumu durumu (bizim kararımız):** yalnız `PLANNED → RUNNING → FINISHED`; geri dönüş ve atlama 400. `RUNNING`'e geçişte `startedAt` sunucu saatiyle (`Clock`) yazılır, istemciden alınmaz. PUT ve PATCH aynı geçiş kuralına tabidir. Oturum `RUNNING` olunca `StudyTimerRegistry`'ye eklenir, `FINISHED` olunca veya oturum silinince (veritabanından silindikten sonra) çıkarılır. Bilinen sınır: silmeden sonra transaction geri alınırsa registry yeniden başlatmaya kadar eksik kalır; vize için kabul edildi.
- **Çalışma oturumu:** Bağlı oda `type = STUDY` değilse 400.
- **Mülakat oturumu:** Bağlı oda `type = INTERVIEW` değilse 400; `interviewerId == candidateId` ise 400; kullanıcılar yoksa 404.
- **Oda silme (M20, transaction örneği):** `RoomServiceImpl.delete(id)` tek `@Transactional` metot içinde sırayla `VideoQueueItemRepository.deleteByRoomId`, `StudySessionService.deleteByRoomId` (önce repository'nin JPQL `DELETE`'ini çağırır, sonra silinen oturumları registry'den çıkarır), `InterviewSessionRepository.deleteByRoomId` (her JPQL `DELETE` parametreli, `setParameter`) ve en son `RoomRepository.delete` çağırır. Biri hata verirse hepsi geri alınır. Bu, sunumda "neden transaction" sorusunun cevabıdır.
- **Kullanıcı silme:** Kullanıcının odası veya oturumu varsa silme 409 döner (`ConflictException`); vize için cascade yapılmaz. Tespit yöntemi: servis `remove` + `flush` çağırır, foreign key ihlalini (`DataIntegrityViolationException` / `PersistenceException`) yakalayıp `ConflictException`'a çevirir. Böylece kullanıcı modülü diğer modüllerin tablolarını bilmek zorunda kalmaz.
- **Kullanıcı DTO sözleşmesi (bizim kararımız):** roller **isimle** gönderilir (`["ROLE_EDITOR"]`), bilinmeyen rol 400. `UserCreateRequest`: username, password (8–72 karakter), email, displayName, roles (boş olamaz), enabled (gönderilmezse `true`). `UserUpdateRequest` (PUT): aynı alanların tümü, **parola dahil** zorunlu (tam güncelleme). `UserPatchRequest`: hepsi isteğe bağlı; parola gelirse encode edilir. Username değiştirilebilir; çakışırsa 409. Aynı email de 409 (servis kontrol eder, DB hatasına bırakılmaz).

### 5.4 Repository sözleşmesi (M18)

Her `XRepository` arayüzü en az şunları tanımlar ve `XRepositoryImpl` `EntityManager` ile gerçekler:

| Metot | EntityManager karşılığı |
|---|---|
| `X save(X x)` (yalnız yeni kayıt) | `persist(x)`, ardından `x` döner (IDENTITY ile id dolu) |
| `Optional<X> findById(Long id)` | `find(X.class, id)` |
| `List<X> findAll()` | `createQuery("SELECT x FROM X x ORDER BY x.id", X.class)` |
| `List<X> findByFilter(...)` | parametreli JPQL + `setParameter("type", type)` (aşağıya bakın) |
| `X update(X x)` | `merge(x)` |
| `void delete(X x)` | `remove(x)` (yönetilen entity üzerinde) |

Modüle göre farklar: video kuyruğunda `findAll` yerine `findByRoomId` (oda içindeki sıra) vardır; kullanıcı ve mülakat modüllerinde filtre metodu `findByEnabled` / `findByStatus` adını taşır. Ayrıntılı imzalar uygulama planındadır.

İsteğe bağlı filtre JPQL kalıbı (null parametre = filtre yok):

```java
SELECT r FROM Room r
WHERE (:type IS NULL OR r.type = :type)
  AND (:visibility IS NULL OR r.visibility = :visibility)
ORDER BY r.id
```

Repository'ler `@Transactional` taşımaz; transaction sınırı serviste.

## 6. Hata yönetimi (M28–M30)

### 6.1 Ortak hata modeli

```json
{
  "timestamp": "2026-11-20T14:03:12.481Z",
  "status": 404,
  "error": "Not Found",
  "message": "Oda bulunamadı: id=42",
  "path": "/api/rooms/42",
  "fieldErrors": [ { "field": "name", "message": "boş olamaz" } ]
}
```

`ErrorResponse` record'u; `fieldErrors` yalnız doğrulama hatalarında dolu, yoksa JSON'da hiç yer almaz (`@JsonInclude(NON_EMPTY)`). Zaman `Clock` bean'inden alınır (UTC `Instant`). Mesajlar Türkçe.

### 6.2 Özel exception'lar

`common.error` paketinde, hepsi `RuntimeException`'dan:
- `ResourceNotFoundException` → 404 (`"<Kaynak> bulunamadı: id=<id>"`; yapıcı kaynak adı + id alır).
- `InvalidRequestException` → 400 (iş kuralı ihlalleri, PATCH'te id).
- `ConflictException` → 409 (kullanıcı adı zaten var, silinemeyen kullanıcı).

### 6.3 `GlobalExceptionHandler` (`@RestControllerAdvice`)

| Yakalanan | Durum | Mesaj |
|---|---|---|
| `ResourceNotFoundException` | 404 | exception mesajı |
| `InvalidRequestException` | 400 | exception mesajı |
| `ConflictException` | 409 | exception mesajı |
| `MethodArgumentNotValidException` | 400 | "İstek doğrulanamadı" + `fieldErrors` |
| `HttpMessageNotReadableException` (bozuk JSON, yanlış enum) | 400 | "İstek gövdesi okunamadı" |
| `MethodArgumentTypeMismatchException` (`/api/rooms/abc`) | 400 | "<param> parametresi geçersiz" |
| `MissingServletRequestParameterException` | 400 | "<param> parametresi eksik" |
| `HttpRequestMethodNotSupportedException` | 405 | Spring'in mesajı |
| `NoResourceFoundException` (tanımsız yol) | 404 | "Yol bulunamadı" |
| `HttpMediaTypeNotSupportedException` (JSON olmayan gövde) | 415 | Spring'in mesajı |
| `HttpMediaTypeNotAcceptableException` | 406 | Spring'in mesajı |
| `DataIntegrityViolationException` (gözden kaçan unique ihlali) | 409 | "Kayıt mevcut verilerle çakışıyor" |
| `Exception` (geri kalan her şey) | 500 | "Beklenmeyen bir hata oluştu" (ayrıntı yalnız log'a) |

Uygulama: `GlobalExceptionHandler extends ResponseEntityExceptionHandler` ve `handleExceptionInternal`'ı ezerek Spring MVC'nin tüm standart hatalarını aynı `ErrorResponse`'a çevirir; tablodaki özel durumlar ayrıca `@ExceptionHandler` metotlarıdır. Böylece hiçbir standart hata 500'e düşmez.

**401 ve 403 de aynı modeli kullanır:** Bunlar controller'a ulaşmadan Security filtresinde oluştuğu için `@RestControllerAdvice` yakalayamaz. `JsonAuthenticationEntryPoint` (401) ve `JsonAccessDeniedHandler` (403) aynı `ErrorResponse`'u Jackson `ObjectMapper`/`JsonMapper` bean'i ile yazar. Böylece durum kodu ile gövde her yerde tutarlıdır (M30). Entry point **iki yerde** kayıtlı olmalı: `httpBasic(b -> b.authenticationEntryPoint(entryPoint))` (yanlış parola) ve `exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(handler))` (hiç kimlik yok / yetki yok). Yalnız ikincisi yapılırsa yanlış parolada JSON gövdesiz varsayılan 401 döner.

## 7. Güvenlik (M31–M35, M14)

### 7.1 Kimlik doğrulama

- `SecurityFilterChain` bean'i (`SecurityConfig`, `@Configuration`): `httpBasic(...)` + yukarıdaki JSON entry point; `csrf` kapalı (durumsuz REST API, oturum çerezi yok); `sessionManagement` `STATELESS`; `formLogin` kapalı.
- `DatabaseUserDetailsService implements UserDetailsService`: `UserRepository.findByUsernameWithRoles` (JPQL `JOIN FETCH u.roles` + `setParameter`) ile kullanıcıyı yükler. Roller LAZY olduğu ve güvenlik filtresinde transaction/open-in-view olmadığı için `JOIN FETCH` **zorunlu**; yoksa her girişte `LazyInitializationException` olur; `enabled=false` kullanıcı `User.withUsername(...).disabled(true)` ile döner → Spring 401 verir.
- `PasswordEncoder` bean'i: `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()` (Argon2id). Kodlanmış değer ~100 karakter; sütun 255.
- Kullanıcı oluşturma ve parola değiştirme (`POST`/`PUT`/`PATCH /api/users`) parolayı serviste encode eder; düz parola hiçbir yerde saklanmaz veya loglanmaz.

### 7.2 Yetki matrisi (HTTP metodu + yol, `requestMatchers` ile)

Roller hiyerarşik değil; her kullanıcıya gereken tüm roller atanır (seed'de admin üçüne de sahip).

| İstek | VIEWER | EDITOR | ADMIN | Anonim |
|---|---|---|---|---|
| `GET /api/**` (users hariç) | ✓ | ✓ | ✓ | 401 |
| `POST`, `PUT`, **`PATCH`** `/api/**` (users hariç) | 403 | ✓ | ✓ | 401 |
| `DELETE /api/**` (users hariç) | 403 | 403 | ✓ | 401 |
| `/api/users/**` (tüm metotlar) | 403 | 403 | ✓ | 401 |
| `GET /actuator/health`, `GET /actuator/info` | ✓ | ✓ | ✓ | ✓ (herkese açık) |
| Diğer `/actuator/**` (metrics, env, beans…) | 403 | 403 | ✓ | 401 |
| `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**` | ✓ | ✓ | ✓ | ✓ |
| `/error` | ✓ | ✓ | ✓ | ✓ (yoksa filtre içi hatalar yanıltıcı 401/403'e dönüşür) |
| Diğer her şey | `denyAll` | | | |

Kural sırası önemli: daha özel olanlar (`/api/users/**`) daha genel olanlardan (`/api/**`) önce yazılır.

### 7.3 Örnek kullanıcılar (seed)

`UserSeeder implements ApplicationRunner`, yazmayı **`@Transactional` bir servis metodu üzerinden** yapar (repository'lerde transaction yok, doğrudan `persist` hata verir); rol tabloları ve kullanıcılar yoksa oluşturur (varsa dokunmaz, böylece yeniden başlatmada çoğalmaz). Parolalar seed sırasında Argon2id ile encode edilir.

| Kullanıcı | Roller | Durum | Ne göstermek için |
|---|---|---|---|
| `admin` | VIEWER, EDITOR, ADMIN | aktif | DELETE, kullanıcı yönetimi, actuator |
| `editor` | VIEWER, EDITOR | aktif | POST/PUT/PATCH serbest, DELETE 403 |
| `viewer` | VIEWER | aktif | yalnız GET, POST 403 |
| `pasif` | VIEWER | **pasif** | doğru parolayla bile 401 |

Seed parolaları değerlendirme kolaylığı için sabit ve README'de yazılı (yalnız yerel geliştirme verisi; `application.properties`'te `roommates.seed.*` olarak tanımlanır, ortam değişkeniyle ezilebilir).

## 8. Spring çekirdeği gösterimleri (M7–M13)

Her madde sunumda tek bir yere işaret edilerek gösterilebilmeli. Yapay "demo sınıfları" yerine modüllerin gerçek işine bağlı:

| Madde | Nerede | Nasıl | Sahip |
|---|---|---|---|
| M7 Constructor injection | her sınıf | `private final` + tek constructor; kod incelemesinde `@Autowired` aranır, hiç olmamalı | herkes |
| M8 `@Configuration` + `@Bean` | `common.config.AppConfig` | `@Bean Clock clock()` → `Clock.systemUTC()`; hata zaman damgaları ve `created_at` alanları bunu kullanır | D |
| M8 (ek) | `SecurityConfig` | `PasswordEncoder` ve `SecurityFilterChain` de `@Bean` | A |
| M9 `@Lazy` | `interview.InterviewConfig` | İki sağlayıcı da **yalnız** burada `@Bean` olarak tanımlanır (sınıflarda `@Component` yok; çift tanım belirsizlik yaratır). `BehavioralQuestionProvider` bean'i `@Lazy` tanımlı; enjeksiyon noktasında da `@Lazy` (aksi halde tekil servis oluşurken hemen yaratılır). Constructor'ında log: `"Davranışsal soru bankası yüklendi"`. Uygulama açılışında bu log **görünmez**, ilk BEHAVIORAL mülakat oluşturulduğunda görünür | D |
| M10 `@PostConstruct` / `@PreDestroy` | `study.StudyTimerRegistry` | Çalışan oturumların bellekteki zamanlayıcı tablosu. `@PostConstruct`: tabloyu kurar, durumu `RUNNING` olan oturumları DB'den yükler, log yazar. `@PreDestroy`: tabloyu boşaltır, "N zamanlayıcı kapatıldı" log'u yazar (uygulama Ctrl+C ile kapatılınca görülür). `@PostConstruct`'ta `@Transactional` çalışmaz: yalnız okuma sorgusu yapılır, LAZY ilişkiye dokunulmaz, yazma yapılmaz | C |
| M11 Çoklu implementasyon + `@Primary` / `@Qualifier` | `interview.QuestionProvider` | `TechnicalQuestionProvider` (`@Primary`) ve `BehavioralQuestionProvider` (`@Qualifier("behavioral")`, `@Lazy`). `InterviewSessionServiceImpl` constructor'ı ikisini alır: niteleyicisiz parametre `@Primary`'yi, `@Qualifier("behavioral") @Lazy` parametre diğerini alır. Oturum oluşturulurken `category`'ye göre soru seçilir | D |
| M12 `@Value` ile constructor'a özellik | `study.StudySessionServiceImpl` | `roommates.study.default-focus-minutes=25` ve `roommates.study.default-break-minutes=5`; constructor parametresinde `@Value("${roommates.study.default-focus-minutes}") int defaultFocusMinutes`. İstek bu alanları göndermezse varsayılan kullanılır | C |
| M13 Actuator health + info | `application.properties` | `management.endpoints.web.exposure.include=health,info,metrics`; `management.info.env.enabled=true`; `info.app.name=RoomMates`, `info.app.description=...`, `info.app.version=@project.version@` (Maven kaynak filtreleme); `management.endpoint.health.show-details=when-authorized`, `roles=ROLE_ADMIN` | D |
| M14 Actuator koruması | `SecurityConfig` | §7.2 matrisi | A |

## 9. Belgeleme ve teslimler (M36–M38)

**OpenAPI / Swagger (M36):**
- `OpenApiConfig` (`@Configuration`): API başlığı, açıklama, sürüm; `basicAuth` güvenlik şeması (`@SecurityScheme(type = HTTP, scheme = "basic")`) ve global güvenlik gereksinimi, böylece Swagger UI'da "Authorize" ile giriş yapılabilir.
- Her controller (kendi sahibi yazar): `@Tag`, her metotta `@Operation(summary)`, `@ApiResponse`'lar (200/201/204 ve 400/401/403/404, hata cevaplarının şeması `ErrorResponse`). DTO'larda gerekirse `@Schema(example=...)`.
- Adresler: `http://localhost:8080/swagger-ui.html`, `http://localhost:8080/v3/api-docs`.

**Çalıştırma rehberi (M37, `README.md`, Türkçe):** gerekli yazılımlar ve sürümler (JDK 25, MySQL 8.x, Maven Wrapper hazır); MySQL'de DB ve kullanıcı oluşturma SQL'i (`docs/sql/01-create-db-user.sql`); `.env.example`'ı kopyalayıp doldurma; ortam değişkenleriyle çalıştırma (`./mvnw spring-boot:run`); API, Swagger ve Actuator adresleri; §7.3 tablosu (kullanıcı/parola/rol); sık hatalar (MySQL kapalı, yanlış parola).

**Postman koleksiyonu (M38, `postman/RoomMates.postman_collection.json` + `postman/RoomMates-local.postman_environment.json`):**
- Değişkenler: `baseUrl`, `adminUser`/`adminPass`, `editorUser`/`editorPass`, `viewerUser`/`viewerPass`, `pasifUser`/`pasifPass`, oluşturulan kayıtların id'leri (`roomId`, `videoId`…; POST'un test betiği cevaptan yazar).
- Koleksiyon düzeyinde Basic Auth `{{editorUser}}`/`{{editorPass}}`; özel istekler kendi auth'unu ezer.
- Klasörler: her modül için Liste, Tek kayıt, Ekle, Tam güncelle, Kısmi güncelle, Sil. Ayrıca "Hata senaryoları" klasörü: 400 (eksik alan, bozuk JSON, PATCH'te id), 404 (olmayan id), 401 (auth yok; yanlış parola; pasif kullanıcı), 403 (viewer ile POST, editor ile DELETE). Ek: "POST'ta id gönderme" isteği.

## 10. Görev dağılımı (vize)

### 10.1 Sorumluluk özeti

| | Kişi A | Kişi B | Kişi C | Kişi D |
|---|---|---|---|---|
| Modül | Kullanıcılar + güvenlik | Odalar + video kuyruğu | Çalışma oturumları + hata yönetimi | Mülakat oturumları + yapılandırma/belgeleme |
| Kaynak(lar) | `/api/users` | `/api/rooms`, `/api/rooms/{id}/videos` | `/api/study-sessions` | `/api/interview-sessions` |
| Kontrol listesi | M14, M31–M35 (+ users JPQL filtresi) | M18–M20 referans kalıbı, M21–M26 referans controller | M10, M12, M28–M30, M38 (birleştirme) | M8, M9, M11, M13, M36, M37 |
| Sunumda anlatacağı | Basic auth akışı, Argon2id, roller, 401/403 | katman akışı, EntityManager CRUD, JPQL, transaction | proje tanıtımı, DI özelliği, yaşam döngüsü, hata modeli, 400/404, Postman | bean yönetimi, Actuator, Swagger, README |

M1–M7 ve M15–M27 her modülün kendi kodunda karşılanır; herkesin sorumluluğu.

### 10.2 Aşamalar ve bağımlılıklar

**Aşama 0 — İskelet (Ayberk + Claude, tek PR, diğerleri bunu bekler)**
- GitHub deposu (`roommates`), 3 ekip üyesi davet, `main` korumalı (PR + 1 onay).
- Initializr ile proje (Boot 4.1.2, Java 25, `com.roommates`), §3'teki bağımlılıklar; `EntityManager` ve `@Transactional`'ın Spring Data olmadan çalıştığının doğrulanması.
- `application.properties` (DB ortam değişkenleri, `ddl-auto=update`, `roommates.*` yer tutucuları), `.env.example`, `.gitignore` (`.env.local`, `target/`, IDE dosyaları).
- Boş paket yapısı (§4.2), `AppConfig` (`Clock`), geçici "her şeye izin ver" `SecurityConfig` (A ilk PR'ında gerçeğiyle değiştirir).
- `docs/sql/01-create-db-user.sql`, bu tasarım belgesi, `CONTRIBUTING.md` (dal adları, commit kuralı, PR kontrol listesi: `@Autowired` yok, Spring Data yok, controller'da repository yok).

**Aşama 1 — Ortak sözleşmeler (paralel, küçük PR'lar, herkesin işi buna dayanır; önce bunlar birleşir)**
- **A:** `Role`, `User` entity'leri, `UserRepository` (`findById`, `findByUsername` B ve D tarafından kullanılır); ardından güvenlik (Basic, Argon2id, örnek kullanıcılar), çünkü herkes test ederken giriş yapmak zorunda.
- **B:** `Room` entity'si + enum'lar, `RoomRepository` (`findById` C ve D tarafından kullanılır). **Referans CRUD kalıbı** (`Room` için controller → service → repository tam akışı) Aşama 2'nin ilk işidir; diğerleri bu dosyaları örnek alır.
- **C:** `ErrorResponse`, üç özel exception, `GlobalExceptionHandler` (en azından 404/400/doğrulama). Bu birleşmeden kimse kendi exception'ını yazmaz.
- **D:** `AppConfig` dışındaki ortak yapılandırma: `OpenApiConfig`, Actuator özellikleri (`info.app.*`).

**Aşama 2 — Modüller (paralel)**

*Kişi A — Kullanıcılar ve güvenlik*
1. `SecurityConfig`: `SecurityFilterChain` (§7.2 matrisi, actuator ve swagger kuralları dahil), `httpBasic`, `STATELESS`, `csrf` kapalı.
2. `PasswordEncoder` (Argon2id) bean'i; `bcprov-jdk18on` bağımlılığının çalıştığını doğrulama.
3. `DatabaseUserDetailsService` (aktiflik + roller DB'den).
4. `JsonAuthenticationEntryPoint` (401), `JsonAccessDeniedHandler` (403) — C'nin `ErrorResponse`'unu kullanır.
5. `UserSeeder` (§7.3), `roommates.seed.*` özellikleri.
6. `/api/users` tam CRUD: `UserCreateRequest` (username, password, email, displayName, roles), `UserUpdateRequest`, `UserPatchRequest` (parola değişimi dahil; encode edilir), `UserResponse` (parola yok); `?enabled=` JPQL filtresi; aynı username → 409; ilişkili kaydı olan kullanıcı silinince 409.
7. Postman: users klasörü + 401 (auth yok, yanlış parola, pasif kullanıcı) ve 403 istekleri.

*Kişi B — Odalar ve video kuyruğu*
1. `/api/rooms` tam CRUD; sahip = giriş yapan kullanıcı; `?type=&visibility=` JPQL filtresi.
2. POST'ta gövdedeki `id`'nin yok sayıldığını gösteren durum; PATCH'te id → 400 (referans uygulama; diğerleri kopyalar).
3. `VideoQueueItem` entity'si; `/api/rooms/{roomId}/videos` tam CRUD (oda yoksa 404; video başka odaya aitse 404); `position` otomatik.
4. `VideoQueueItemRepository.deleteByRoomId` (JPQL `DELETE` + `setParameter`).
5. `RoomServiceImpl.delete`: §5.3'teki çok tablolu silme tek transaction'da (C ve D'nin `deleteByRoomId` metotlarını çağırır).
6. Postman: rooms + videos klasörleri, "POST'ta id gönderme" isteği.

*Kişi C — Çalışma oturumları ve hata yönetimi*
1. Aşama 1'deki hata altyapısını tamamlama (§6.3 tablosunun tamamı, `fieldErrors`, 500 log).
2. `StudySession` entity'si; `/api/study-sessions` tam CRUD; `?roomId=&status=` JPQL filtresi; oda STUDY değilse 400; `focusMinutes` 1–180, `breakMinutes` 1–60, `rounds` 1–12.
3. `@Value` ile varsayılan süreler (M12).
4. `StudyTimerRegistry` (`@PostConstruct`/`@PreDestroy`, M10); oturum PATCH ile `status=RUNNING` yapılınca kaydedilir, `FINISHED` olunca çıkarılır.
5. `StudySessionRepository.deleteByRoomId`.
6. Postman: study-sessions klasörü + 400 senaryoları (eksik alan, bozuk JSON, yanlış enum, `/abc` id) ve 404 senaryoları.

*Kişi D — Mülakat oturumları, yapılandırma ve belgeleme*
1. `QuestionProvider` arayüzü, `TechnicalQuestionProvider` (`@Primary`), `BehavioralQuestionProvider` (`@Lazy`, `@Qualifier("behavioral")`), `InterviewConfig` (`@Configuration` + `@Bean`) (M8, M9, M11).
2. `InterviewSession` entity'si; `/api/interview-sessions` tam CRUD; `?status=` JPQL filtresi; oda INTERVIEW değilse 400; interviewer = candidate ise 400; soru `category`'ye göre sağlayıcıdan.
3. `InterviewSessionRepository.deleteByRoomId`.
4. Actuator `health`/`info` (M13) ve A ile birlikte erişim kurallarının doğrulanması.
5. `OpenApiConfig`; tüm controller'ların Swagger'da eksiksiz görünmesinin son kontrolü.
6. `README.md` çalıştırma rehberi. (Postman koleksiyonunun birleştirilmesi iş yükü dengesi için C'ye verildi; her modül kendi kendine yeten Postman parçasını teslim eder.)

**Aşama 3 — Birleştirme ve prova (hep birlikte)**
- Temiz bir MySQL'de README'yi baştan sona izleyerek kurulum (README'yi yazmamış biri yapar).
- §12'deki kontrol tablosu üzerinden 38 maddenin tek tek gösterilmesi; her maddeye bir Postman isteği veya dosya:satır not edilir.
- Kod incelemesi: `grep -r "@Autowired\|JpaRepository\|CrudRepository\|spring-data"` boş dönmeli.
- Uygulamayı yeniden başlatıp verilerin durduğunu gösterme (M15).

### 10.3 Çalışma kuralları
- Dal adları: `feat/a-security`, `feat/b-rooms`, `feat/c-study`, `feat/d-interview` vb.; her iş kendi küçük PR'ı.
- Her PR'ı başka bir ekip üyesi onaylar; PR açıklamasında karşıladığı M maddeleri yazılır.
- Başkasının paketindeki dosyaya dokunmak gerekiyorsa sahibine sorulur (en çok `SecurityConfig`, `GlobalExceptionHandler`, `RoomServiceImpl.delete`).
- Commit mesajları İngilizce (`feat(room): add JPQL filter by type`).

## 11. Final için yön (bağlayıcı değil)

Bologna'nın vize sonrasına bıraktığı konular ve bu tasarımda nereye oturacakları:
- **JWT (Ö05):** `SecurityConfig`'te Basic yerine/yanına resource server; roller aynı kalır. **Kayıt sahipliği:** odayı yalnız sahibi silebilir/güncelleyebilir (servis katmanında `owner` kontrolü). **CORS.**
- **Spring Data JPA (Ö04):** Repository arayüzleri sayesinde istenirse `XRepositoryImpl` değiştirilerek geçilebilir; hocaya sorulacak.
- **Sayfalama/sıralama:** liste uç noktalarına `page`, `size`, `sort`.
- **Testler (Ö06):** servis birim testleri, MockMvc ile API ve 401/403 testleri.
- **Spring AI + RAG (Ö07):** ör. mülakat odasında soru üretme veya geri bildirim özetleme. **Ö09:** gizlilik/maliyet/etik değerlendirme bölümü.
- **Profiller, loglama, Docker (Ö08).**
- Gerçek zamanlı senkron (WebSocket/STOMP, w2g analizi) yalnız bunlar bittikten sonra.

## 12. Kontrol listesi izlenebilirlik tablosu

M numaraları, kontrol listesindeki 38 maddenin PDF'teki sırasıdır.

| # | Madde (kısaltılmış) | Nasıl karşılanıyor | Gösterim | Sahip |
|---|---|---|---|---|
| M1 | Spring Boot 4.1.2 | `pom.xml` parent 4.1.2 | `pom.xml` | iskelet |
| M2 | Spring Data JPA/REST yok, hazır repository yok | Bağımlılık hiç yok (§3); repository'ler elle | `pom.xml`, grep | herkes |
| M3 | Controller → Service → Repository → DB | §4.1 | bir isteğin kod içinde izlenmesi | herkes |
| M4 | `@RestController`, controller'da DB işlemi yok | §4.1 | controller dosyaları | herkes |
| M5 | Service arayüzü + `@Service`, iş kuralları serviste | §4.1, §5.3 | `RoomService`/`RoomServiceImpl` | herkes |
| M6 | Repository arayüzü + `@Repository`, JPA/Hibernate + EntityManager | §5.4 | `RoomRepositoryImpl` | herkes |
| M7 | Yalnız constructor injection | §8 | grep `@Autowired` → boş | herkes |
| M8 | `@Configuration` + `@Bean` | `AppConfig.clock()`, `InterviewConfig`, `SecurityConfig` | dosyalar | D (A) |
| M9 | `@Lazy` | `BehavioralQuestionProvider` | açılışta log yok, ilk kullanımda var | D |
| M10 | `@PostConstruct` / `@PreDestroy` | `StudyTimerRegistry` | açılış ve kapanış log'ları | C |
| M11 | Çoklu implementasyon + `@Qualifier`/`@Primary` | `QuestionProvider` x2 | TECHNICAL ve BEHAVIORAL mülakat oluşturma | D |
| M12 | Özel property, `@Value` ile constructor parametresine | `roommates.study.default-*` | süre göndermeden oturum oluşturma | C |
| M13 | Actuator health + info (ad, açıklama, sürüm) | §8 M13 | `GET /actuator/health`, `/actuator/info` | D |
| M14 | Yönetim bilgisi veren Actuator uçları korumalı | §7.2 | `/actuator/metrics`: anonim 401, editor 403, admin 200 | A |
| M15 | MySQL'de kalıcı, yeniden başlatmada korunur | `ddl-auto=update` | yeniden başlat + GET | iskelet |
| M16 | `@Entity`, `@Table`, `@Column` açıkça | §4.3 | entity dosyaları | herkes |
| M17 | Parametresiz constructor, `@Id`, uygun `@GeneratedValue` | `protected` ctor, `IDENTITY` | entity dosyaları | herkes |
| M18 | Repository: ekle/bul/listele/güncelle/sil, uygun EntityManager metotları | §5.4 (`persist/find/createQuery/merge/remove`) | `XRepositoryImpl` | herkes (B referans) |
| M19 | Parametreli JPQL filtresi + `setParameter` | §5.1 filtreleri | `GET /api/rooms?type=STUDY` | herkes (B referans) |
| M20 | Spring `@Transactional` serviste | tüm değiştiren servis metotları; çok tablolu oda silme | `RoomServiceImpl.delete` | herkes (B örnek) |
| M21 | Kaynak odaklı adlar, uygun HTTP metotları | §5.1 | Swagger | herkes |
| M22 | GET liste + id ile, `@PathVariable` | §5.1 | Postman | herkes |
| M23 | POST JSON → kayıt + üretilen id; gövdedeki id güncellemeye yol açmaz | §5.2 | "POST'ta id gönderme" isteği | herkes (B referans) |
| M24 | PUT tam güncelleme | §5.2 | Postman | herkes |
| M25 | PATCH kısmi; gönderilmeyen korunur; id değişimi engellenir | §5.2 | tek alanlı PATCH + id'li PATCH → 400 | herkes (B referans) |
| M26 | DELETE id ile | §5.1 | 204, ardından GET → 404 | herkes |
| M27 | `@RequestBody`, Jackson | DTO record'ları | controller'lar | herkes |
| M28 | Global handler: `@RestControllerAdvice` + `@ExceptionHandler`, özel mesajlar | §6.3 | `GlobalExceptionHandler` | C |
| M29 | Custom exception'lar + ortak JSON (durum, mesaj, zaman) | §6.1, §6.2 | herhangi bir 404 gövdesi | C |
| M30 | 404 / 400; durum kodu ve gövde tutarlı | §6.3 + JSON 401/403 işleyicileri | hata senaryoları klasörü | C (A) |
| M31 | Spring Security + `SecurityFilterChain` + HTTP Basic | §7.1 | `SecurityConfig` | A |
| M32 | Kullanıcı, aktiflik, roller DB'de; doğrulama DB'den | §4.3, §7.1 | `pasif` kullanıcı → 401 | A |
| M33 | Argon2id; uyumlu PasswordEncoder; sütun hash'in tamamını alır | §7.1, `password` 255 | DB'de `$argon2id$...` değeri | A |
| M34 | Okuma / ekleme-güncelleme / silme ayrı yetki; metot + yol; PATCH güncelleme yetkisi ister | §7.2 | viewer PATCH → 403 | A |
| M35 | Kimliksiz/başarısız → 401, yetkisiz → 403 | §6.3 son paragraf, §7.2 | hata senaryoları | A |
| M36 | OpenAPI + Swagger UI; tüm metotlar ve istek/cevap modelleri | §9 | `/swagger-ui.html`, `/v3/api-docs` | D (herkes kendi controller'ı) |
| M37 | Çalıştırma rehberi (yazılımlar, DB ayarları, başlatma, adresler, örnek kullanıcılar ve roller) | §9 | `README.md` | D |
| M38 | Postman koleksiyonu (tüm CRUD + 400/404/401/403; baseUrl ve kimlik bilgileri değişken) | §9 | `postman/` | C (herkes kendi parçası) |

## 13. Uygulayıcıya bırakılanlar

- Sınıf ve metot adlarının küçük farkları, log mesajlarının tam metni, Swagger açıklama metinleri.
- Bean Validation mesajlarının ifadesi (Türkçe olacak).
- `ErrorResponse`'un Jackson ile yazılma ayrıntısı (Boot 4'teki `JsonMapper` bean'inin enjekte edilmesi).
- Seed verisine örnek oda/oturum eklenip eklenmeyeceği (sunum için faydalı; eklenecekse yalnız tablo boşsa).
