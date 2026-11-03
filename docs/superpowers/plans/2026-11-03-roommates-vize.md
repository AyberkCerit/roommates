# RoomMates Vize Uygulama Planı

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Amaç:** Teknik Gereklilikler Kontrol Listesi'nin 38 maddesini karşılayan, dört modüllü RoomMates REST API'sini (Spring Boot 4.1.2, MySQL, HTTP Basic) vize sunumuna hazır hâle getirmek.

**Mimari:** Modül bazlı paketler (`user`, `room`, `video`, `study`, `interview`) ve ortak `common` paketi. Her modülde Controller → Service (arayüz + `@Service`) → Repository (arayüz + `EntityManager` ile `@Repository`) akışı. Spring Data yok. Hata ve güvenlik altyapısı ortak, her şey aynı JSON hata modelini kullanıyor.

**Teknolojiler:** Java 25, Spring Boot 4.1.2 (Framework 7.0.10, Security 7.1.2, Hibernate 7.4.13, Jackson 3.1.7), MySQL 8.x, springdoc-openapi 3.2.0, BouncyCastle (Argon2id), Maven Wrapper.

**Tasarım belgesi:** `docs/superpowers/specs/2026-11-03-roommates-vize-design.md`. Bu plan o belgeye dayanır, uygulayıcı ikisini birlikte okur. Plandaki §-numaraları tasarım belgesinin bölümleridir.

## Kimin hangi görevleri yapacağı

| Aşama | Kişi A (kullanıcı + güvenlik) | Kişi B (oda + video) | Kişi C (çalışma + hata) | Kişi D (mülakat + belgeleme) |
|---|---|---|---|---|
| 0 | — | — | — | — |
| 1 | Görev 3, Görev 7 | Görev 4 | Görev 5 | Görev 6 |
| 2 | Görev 9 | Görev 8, Görev 10, Görev 15 | Görev 11, Görev 12 | Görev 13, Görev 14 |
| 3 | Görev 17 | Görev 17 | Görev 16b (Postman), Görev 17 | Görev 16a (README), Görev 17 |

Görev 1 ve Görev 2 Aşama 0'dır (Ayberk + Claude; PR'ını bir ekip üyesi inceler). Görevlerin bağımlılıkları her görevin **Bağımlılık** satırındadır. Özet:
- G3, G5 ve G6 yalnız G2'ye bağlı. G6 ilk gün birleşir, çünkü G7'nin Actuator kontrolleri ona dayanıyor.
- G4, G3'ün `User`'ına bağlı. G7; G3, G5 ve G6'ya bağlı.
- Aşama 2'de G8 (oda CRUD) ilk birleşen olmalı, çünkü diğer CRUD'lar onun kalıbını kopyalar. G9, G10, G12 ve G14 ondan sonra PR açar.
- G11 yalnız G4'e, G13 yalnız G1'e bağlı. İkisi de Aşama 1 bitmeden yapılabilir.
- G15 en son birleşen koddur: G10, G12 ve G14'e bağlı.

```
G1 → G2 → ┬ G3 (A) ─┬→ G4 (B) ─┬────────────────→ G8 (B) → ┬ G9 (A)
          ├ G5 (C) ─┤          └→ G11 (C) ───────────────→ ├ G12 (C)
          ├ G6 (D) ─┴→ G7 (A) ───────────────→ (G8'e) ┘    ├ G10 (B)
          └ (G1'den) G13 (D) ──────────────────────────→  └ G14 (D)
          G10 + G12 + G14 → G15 (B) → G16a (D) + G16b (C) → G17 (hepsi)
```

## Genel kısıtlar

Bu kurallar her görevin gereksinimlerine örtük olarak dahildir:

- `pom.xml` parent: `spring-boot-starter-parent` **4.1.2**. Java sürümü `25`. Proje sürümü `0.1.0`.
- **Spring Data yok.** `spring-boot-starter-data-jpa` eklenmez. `JpaRepository`, `CrudRepository`, `@EnableJpaRepositories` ve Spring Data REST yasak. JPA için `spring-boot-starter-jdbc` + `org.springframework.boot:spring-boot-hibernate` kullanılır.
- **Yalnız constructor injection.** Sınıflarda `@Autowired`, setter injection, `@PersistenceContext` yok. Bağımlılıklar `private final` alanlarda tutulur. `EntityManager` de constructor'dan alınır.
- `@Transactional` yalnız `org.springframework.transaction.annotation.Transactional`. Servis sınıfında verilir: değiştiren metotlarda düz, okuyanlarda `readOnly = true`. Repository'ler `@Transactional` taşımaz.
- **Servisler arası bağımlılık kuralı:** Bir servis başka bir modülün **servisini değil repository'sini** kullanır. Örneğin oda yüklemek için `RoomRepository.findById(...).orElseThrow(() -> new ResourceNotFoundException("Oda", id))`. Tek istisna `RoomServiceImpl → StudySessionService.deleteByRoomId`. Bu kural constructor döngülerini önler.
- **Entity kuralları:**
  - `@Entity`, `@Table(name = "...")`, her kalıcı alanda `@Column(name = "...")`, ilişkilerde `@JoinColumn(name = "...")`.
  - `protected` parametresiz constructor.
  - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`.
  - Enum'lar `@Enumerated(EnumType.STRING)`, ilişkiler `FetchType.LAZY`.
  - Zamanlar `java.time.Instant`.
- **DTO kuralları:**
  - DTO'lar `record`; istek DTO'larında sayısal ve boolean alanlar sarmalayıcı tip (`Integer`, `Long`, `Boolean`).
  - `XCreateRequest`'te `id` alanı yoktur. `XPatchRequest`'in ilk alanı `Long id`'dir; null değilse 400 ile reddedilir.
  - `@Size(max=...)` değerleri sütun uzunluğuyla aynıdır.
  - Her istek record'u `@JsonIgnoreProperties(ignoreUnknown = true)` taşır. Böylece POST'ta gönderilen `id` gibi bilinmeyen alanlar, Jackson ayarından bağımsız olarak yok sayılır (M23).
- **Controller kuralları:**
  - Controller yalnız servis arayüzüne bağlıdır. Entity döndürmez, `ResponseEntity<XResponse>` ya da `List<XResponse>` döndürür.
  - POST 201 döner ve `Location` başlığı ekler (`ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")`). DELETE 204 döner.
  - Gövde `@Valid @RequestBody` ile alınır.
- **JSON:** Jackson 3. Databind tarafı `tools.jackson.databind.*`, anotasyonlar `com.fasterxml.jackson.annotation.*`. `com.fasterxml.jackson.databind` import edilmez.
- **Hata mesajları:** Türkçe. 404 mesajı `"<Kaynak> bulunamadı: id=<id>"`. PATCH'te id mesajı tam olarak `"Birincil anahtar (id) PATCH ile değiştirilemez"`.
- `spring.jpa.hibernate.ddl-auto=update`. `create` ve `create-drop` yasak.
- **Commit mesajları** İngilizce Conventional Commits: `feat(room): ...`, `fix(security): ...`. Dal adları `feat/<kişi>-<konu>`. Her PR'ı başka bir üye onaylar ve PR açıklamasında karşıladığı M maddelerini listeler.
- **Doğrulama yöntemi:** Vizede test kodu kapsam dışı (tasarım §2). Her görev, çalışan uygulamaya karşı `scripts/check.sh` ile bekleme kontrolleri yapar. Bu kontroller aynı zamanda Postman isteklerinin taslağıdır.
- **Kontrol kuralları:**
  - Her görevin kontrol bloğu `scripts/checks/g<N>.sh` dosyasına kaydedilir ve `bash -e scripts/checks/g<N>.sh` ile çalıştırılır. `-e` sayesinde ilk FAIL'de durur; boş id ile devam edip zincirleme hata üretmez. Dosyalar repoya girer, Görev 17'de hepsi sırayla yeniden çalıştırılır.
  - Blok dosyasının ilk satırı `source scripts/creds.sh`. Bu, `$A`, `$E`, `$V`, `$P` (admin, editor, viewer, pasif) değişkenlerini kurar.
  - Geçici kullanıcı adları rastgele sonek taşır (`SFX=$RANDOM`, ör. `ayse$SFX`). Yarıda kalan bir çalıştırma, sonrakini 409 ile bozmaz.
  - Id'ler **asla elle yazılmaz**. `check.sh` yalnız PASS'te, cevaptaki ilk `"id"` değerini `/tmp/rm-last-id` dosyasına yazar; FAIL'de dosyayı boşaltır. Kontroller bunu `X=$(cat /tmp/rm-last-id)` ile alır. Tek istisna: isteğin controller'a hiç ulaşmadığı 401/403 kontrollerinde sabit `/1` yazılabilir (Görev 7).
  - Kullanıcı id'leri `scripts/user-id.sh <kullanici_adi>` ile alınır.
  - Kontroller tekrar çalıştırılabilir olmalıdır: her blok kendi verisini oluşturur.
- **JSON alan sırası:** Jackson 3 alanları varsayılan olarak alfabetik sıralayabilir. Bu yüzden kontroller alan sırasına dayanmaz; her alan ayrı `grep` ile aranır. `ErrorResponse`'ta sıra `@JsonPropertyOrder` ile sabitlenir.
- **Postman parçaları:** Her modülün `postman/parts/<modul>.postman_collection.json` dosyası **kendi kendine yeter**. Gereken odaları ve kullanıcıları kendisi oluşturur, id'lerini koleksiyon değişkenlerine yazar ve seed kullanıcılarını silmez ya da değiştirmez. Boş bir veritabanında tek başına çalıştırıldığında yeşil olması, o görevin "bitti" ölçütüdür.

## Gözden kaçabilecek durumlar

Tasarımın ima ettiği ama hiçbir görevin ana akışının denemediği beş durum. Her birinin kontrolü sahibi olan göreve eklendi:

1. **Türkçe karakterli veri** (`Çalışma Odası ğüşıöç`) kaydedilip okunduğunda aynen dönmeli. Görev 8, adım 5.
2. **Sütundan uzun metin** (101 karakterlik oda adı) 500 değil, `fieldErrors` ile 400 vermeli. Görev 8, adım 5.
3. **Boş gövdeli PATCH** (`{}`) 200 dönmeli ve hiçbir alanı değiştirmemeli. Görev 8, adım 5.
4. **Geçersiz tarih biçimi** (`"scheduledAt": "2026-13-45"`) 500 değil, 400 vermeli. Görev 14, adım 5.
5. **Yalnız harf büyüklüğü farklı kullanıcı adı** (`Admin` varken `admin`): MySQL `utf8mb4_0900_ai_ci` karşılaştırması büyük/küçük harf duyarsız. 500 değil, 409 vermeli. Görev 9, adım 5.

---

## Aşama 0 — İskelet (Ayberk + Claude)

### Görev 1: Proje iskeleti ve çalışma ortamı

**Dosyalar:**
- Oluştur: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/main/java/com/roommates/RoomMatesApplication.java`
- Oluştur: `src/main/java/com/roommates/common/config/AppConfig.java`
- Oluştur: `src/main/java/com/roommates/common/security/SecurityConfig.java` (geçici)
- Oluştur: `src/main/resources/application.properties`
- Oluştur: `.env.example`, `.gitignore`, `scripts/run-local.sh`, `scripts/check.sh`
- Var: `.env.local` (Ayberk'in makinesinde hazır, git'e girmez)

**Arayüzler:**
- Üretir: `AppConfig.clock()` → `java.time.Clock` bean'i (`Clock.systemUTC()`). Zaman damgası gereken her sınıf `Clock`'u constructor'dan alır, `Instant.now(clock)` kullanır.
- Üretir: `scripts/check.sh <beklenen_kod> <METHOD> <yol> [kullanici:parola|-] [json_govde]`. `PASS` ya da `FAIL` yazdırır, FAIL'de çıkış kodu 1. Cevap gövdesini `/tmp/rm-check-body`'ye, gövdedeki ilk `"id"` değerini `/tmp/rm-last-id`'ye yazar.
- Üretir: `scripts/creds.sh` (`source` ile kullanılır). `A`, `E`, `V`, `P` değişkenlerini `kullanici:parola` biçiminde kurar. Parolalar `.env.local`'da `SEED_*_PASSWORD` olarak tanımlıysa onları, yoksa `application.properties`'teki varsayılanları kullanır.
- Üretir: `scripts/user-id.sh <kullanici_adi>`. `.env.local`'daki `DB_USERNAME`/`DB_PASSWORD` ile `mysql -N -h 127.0.0.1 roommates` çalıştırır ve kullanıcının id'sini yazdırır. `mysql` istemcisi gerekir. Windows'ta MySQL kurulumuyla gelir; `bin` klasörü Git Bash'in `PATH`'ine eklenir. Bu `CONTRIBUTING.md`'de yazılır.

- [ ] **Adım 1: Initializr'dan projeyi indir ve `pom.xml`'i düzenle**

```bash
curl -s https://start.spring.io/starter.zip -d type=maven-project -d bootVersion=4.1.2 -d javaVersion=25 \
  -d groupId=com.roommates -d artifactId=roommates -d name=RoomMates -d packageName=com.roommates \
  -d dependencies=web,security,actuator,validation,mysql -o /tmp/rm.zip && unzip -o /tmp/rm.zip -d .
```

`pom.xml` ayarları:
- `<version>0.1.0</version>` ve `<description>Öğrencilerin izleme, çalışma ve mülakat odalarında buluştuğu platformun REST API'si</description>`.
- Eklenecek bağımlılıklar: `spring-boot-starter-jdbc`, `org.springframework.boot:spring-boot-hibernate` (sürümsüz, BOM yönetir), `org.bouncycastle:bcprov-jdk18on` (Maven Central'daki son kararlı sürüm, açık `<version>`), `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0`.
- Initializr'ın eklediği `*-test` starter'ları kalabilir.
- Ana sınıf adı `RoomMatesApplication`.

- [ ] **Adım 2: `application.properties`'i yaz**

Değerler:

```properties
spring.application.name=roommates
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/roommates}
spring.datasource.username=${DB_USERNAME:roommates}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
roommates.study.default-focus-minutes=25
roommates.study.default-break-minutes=5
roommates.seed.admin-password=${SEED_ADMIN_PASSWORD:Admin.2026!}
roommates.seed.editor-password=${SEED_EDITOR_PASSWORD:Editor.2026!}
roommates.seed.viewer-password=${SEED_VIEWER_PASSWORD:Viewer.2026!}
roommates.seed.inactive-password=${SEED_INACTIVE_PASSWORD:Pasif.2026!}
```

Actuator ve `info.*` satırları Görev 6'da eklenir. Seed parolaları yalnız yerel değerlendirme verisidir ve README'de yazılı olacak (tasarım §7.3).

- [ ] **Adım 3: `AppConfig` ve geçici `SecurityConfig`'i yaz**

- `AppConfig`: `@Configuration`, `@Bean public Clock clock()`.
- Geçici `SecurityConfig`: `@Configuration`, `@Bean SecurityFilterChain`; tüm isteklere izin verir, `csrf` kapalı.
- Sınıfın başına `// TEMP: Görev 7'de değiştirilecek` yorumu yazılır.

- [ ] **Adım 4: Yardımcı betikleri ve ortam şablonunu yaz**

`.env.example`:

```
DB_URL=jdbc:mysql://localhost:3306/roommates
DB_USERNAME=roommates
DB_PASSWORD=
```

`.gitignore`: Initializr'ınkine `.env.local` satırı eklenir.

`scripts/run-local.sh`: `.env.local`'ı `set -a` ile okur, sonra `exec ./mvnw spring-boot:run` çalıştırır.

`scripts/check.sh`:

```bash
#!/usr/bin/env bash
# Kullanım: scripts/check.sh <beklenen_kod> <METHOD> <yol> [kullanici:parola|-] [json_govde]
expected=$1; method=$2; path=$3; auth=${4:--}; body=$5
args=(-s -o /tmp/rm-check-body -w '%{http_code}' -X "$method" "${BASE_URL:-http://localhost:8080}$path")
[[ "$auth" != "-" ]] && args+=(-u "$auth")
[[ -n "$body" ]] && args+=(-H 'Content-Type: application/json' --data "$body")
code=$(curl "${args[@]}")
if [[ "$code" == "$expected" ]]; then
  echo "PASS $code $method $path"
  grep -o '"id":[0-9]*' /tmp/rm-check-body | head -1 | cut -d: -f2 > /tmp/rm-last-id
else
  : > /tmp/rm-last-id
  echo "FAIL $method $path: beklenen $expected, gelen $code"; cat /tmp/rm-check-body; echo; exit 1
fi
```

`scripts/creds.sh` ve `scripts/user-id.sh` yukarıdaki Arayüzler satırlarına göre yazılır. Tüm betikler `chmod +x` ile çalıştırılabilir yapılır. Windows'taki ekip üyeleri betikleri Git Bash'te çalıştırır.

- [ ] **Adım 5: Spring Data'nın olmadığını ve JPA'nın açıldığını doğrula**

Çalıştır: `./mvnw -q dependency:tree | grep -i "spring-data"`
Beklenen: çıktı yok.

Çalıştır: `scripts/run-local.sh`
Beklenen: log'da `Initialized JPA EntityManagerFactory for persistence unit 'default'` var, uygulama 8080'de açılıyor.

Çalıştır: `scripts/check.sh 200 GET /actuator/health`
Beklenen: `PASS 200 GET /actuator/health`.

- [ ] **Adım 6: Commit**

```bash
git init -b main && git add -A && git commit -m "chore: scaffold Spring Boot 4.1.2 project without Spring Data"
```

### Görev 2: Depo, ekip kuralları ve kurulum belgeleri

**Dosyalar:**
- Oluştur: `CONTRIBUTING.md`, `docs/sql/01-create-db-user.sql`, `README.md` (taslak; Görev 16'da tamamlanır)
- Oluştur: `src/main/java/com/roommates/{user,room,video,study,interview}/package-info.java`, `common/error/package-info.java`
- Var: `docs/superpowers/specs/…-design.md`, `docs/superpowers/plans/…-vize.md`

- [ ] **Adım 1: SQL betiğini yaz**

`docs/sql/01-create-db-user.sql` şunları yapar: `roommates` veritabanını `utf8mb4` / `utf8mb4_0900_ai_ci` ile oluşturur, `'roommates'@'localhost'` kullanıcısını `'BURAYA_PAROLA'` yer tutucusuyla açar ve yalnız `roommates.*` üzerinde `ALL PRIVILEGES` verir.

- [ ] **Adım 2: `CONTRIBUTING.md`'yi yaz**

İçerik:
- Dal adları ve commit kuralı (Genel kısıtlar'daki gibi).
- PR kontrol listesi:
  - `@Autowired` yok, Spring Data yok, controller'da repository yok
  - `@Transactional` importu doğru
  - Entity'de her alanda `@Column`
  - Karşılanan M maddeleri yazıldı
  - `scripts/check.sh` kontrolleri PR açıklamasına yapıştırıldı
- Paket sahipliği tablosu (tasarım §10.1).
- "Başkasının paketine dokunmadan önce sor" kuralı.

- [ ] **Adım 3: Boş modül paketlerini oluştur**

Her `package-info.java` dosyasına modülün tek satırlık açıklaması ve sahibinin harfi yazılır.

- [ ] **Adım 4: Doğrula ve commit et**

Çalıştır: `./mvnw -q compile`. Beklenen: hata yok.

```bash
git add -A && git commit -m "docs: add contributing guide, DB setup script and module packages"
```

- [ ] **Adım 5: GitHub deposunu aç (Ayberk onaylarsa)**

Bu adım dışarıya dönük olduğu için Ayberk'ten açık onay istenir:
- Depo özel açılır ve gönderilir: `gh repo create roommates --private --source . --push`.
- 3 ekip üyesi davet edilir: `gh api repos/{owner}/roommates/collaborators/<kullanici> -X PUT`.
- `main` dalı korunur: PR zorunlu, 1 onay.

---

## Aşama 1 — Ortak temel (paralel)

### Görev 3 (Kişi A): `User` ve `Role` entity'leri, repository'leri

**Dosyalar:**
- Oluştur (`src/main/java/com/roommates/user/` altında): `Role.java`, `User.java`, `RoleRepository.java`, `RoleRepositoryImpl.java`, `UserRepository.java`, `UserRepositoryImpl.java`

**Arayüzler:**
- Üretir (`Role`):
  - Alanlar: `Long id`, `String name`.
  - Sabitler: `public static final String VIEWER = "ROLE_VIEWER", EDITOR = "ROLE_EDITOR", ADMIN = "ROLE_ADMIN"`.
  - Constructor: `Role(String name)`.
  - Tablo `roles`, sütun `name` unique, uzunluk 30.
- Üretir (`User`):
  - Alanlar: `Long id`, `String username` (50, unique), `String password` (255), `String email` (100, unique), `String displayName` (100), `boolean enabled`, `Instant createdAt`, `Set<Role> roles`.
  - Tablo `users`. `roles` ilişkisi `@ManyToMany(fetch = LAZY)` ve `@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))`.
  - Constructor: `User(String username, String password, String email, String displayName, boolean enabled, Instant createdAt)`.
  - Getter ve setter'lar (id için setter yok).
- Üretir (`RoleRepository`): `Role save(Role role)`, `Optional<Role> findByName(String name)`, `List<Role> findAll()`.
- Üretir (`UserRepository`):
  - Temel CRUD: `User save(User user)`, `Optional<User> findById(Long id)`, `List<User> findAll()`, `User update(User user)`, `void delete(User user)`.
  - Sorgular: `List<User> findByEnabled(Boolean enabled)` (null = hepsi), `Optional<User> findByUsername(String username)`, `Optional<User> findByUsernameWithRoles(String username)` (JPQL `LEFT JOIN FETCH u.roles`), `Optional<User> findByEmail(String email)`.
  - `void flush()`.

- [ ] **Adım 1: Entity'leri yaz**

Kurallar için Genel kısıtlar'a bakın.

- [ ] **Adım 2: Repository'leri yaz**

- `@Repository` sınıfları `EntityManager`'ı constructor'dan alır.
- `findBy...` sorguları `getResultStream().findFirst()` ile `Optional` döndürür.
- Tüm JPQL parametreleri `setParameter` ile bağlanır.
- `findByEnabled` sorgusu: `SELECT u FROM User u WHERE (:enabled IS NULL OR u.enabled = :enabled) ORDER BY u.id`.

- [ ] **Adım 3: Tabloların oluştuğunu doğrula**

Çalıştır: `scripts/run-local.sh`, ardından `mysql -u roommates -p roommates -e "SHOW TABLES; SHOW CREATE TABLE users\G"`.
Beklenen:
- `roles`, `users`, `user_roles` tabloları var.
- `password` sütunu `varchar(255)`.
- `username` ve `email` sütunlarında unique anahtar var.

- [ ] **Adım 4: Commit**

```bash
git add src/main/java/com/roommates/user && git commit -m "feat(user): add User and Role entities with EntityManager repositories"
```

### Görev 4 (Kişi B): `Room` entity'si ve repository'si

**Dosyalar:**
- Oluştur (`src/main/java/com/roommates/room/` altında): `RoomType.java`, `RoomVisibility.java`, `Room.java`, `RoomRepository.java`, `RoomRepositoryImpl.java`

**Arayüzler:**
- Tüketir: `User` (Görev 3; sahip ilişkisi için). Görev 3 henüz birleşmediyse dal onun üzerine kurulur.
- Üretir (enum'lar): `RoomType { WATCH, STUDY, INTERVIEW }`, `RoomVisibility { PUBLIC, PRIVATE }`.
- Üretir (`Room`):
  - Alanlar: `Long id`, `String name` (100), `String description` (500, nullable), `RoomType type`, `RoomVisibility visibility`, `Integer maxMembers`, `Instant createdAt`, `Instant closesAt` (nullable), `User owner` (`owner_id`, nullable olamaz).
  - Tablo `rooms`.
  - Constructor: `Room(String name, String description, RoomType type, RoomVisibility visibility, Integer maxMembers, Instant closesAt, User owner, Instant createdAt)`.
- Üretir (`RoomRepository`):
  - Temel CRUD: `Room save(Room room)`, `Optional<Room> findById(Long id)`, `List<Room> findAll()`, `Room update(Room room)`, `void delete(Room room)`.
  - Filtre: `List<Room> findByFilter(RoomType type, RoomVisibility visibility)`. JPQL tasarım §5.4'teki kalıp.

- [ ] **Adım 1: Enum'ları ve entity'yi yaz**

- [ ] **Adım 2: Repository'yi yaz**

Bu sınıf diğer modüller için **referans** olacak. Her metodun üstüne hangi `EntityManager` metodunu neden kullandığını anlatan tek satırlık yorum yazılır (`persist`: yalnız yeni kayıt; `merge`: ayrık entity'yi güncelleme; vb.).

- [ ] **Adım 3: Tablonun oluştuğunu doğrula**

Çalıştır: `scripts/run-local.sh`, ardından `SHOW CREATE TABLE rooms\G`.
Beklenen:
- `type` ve `visibility` sütunları `varchar` (sıra numarası değil).
- `owner_id` sütununda `users`'a foreign key var.

- [ ] **Adım 4: Commit**

```bash
git add -A src/main/java/com/roommates/room && git commit -m "feat(room): add Room entity and EntityManager repository with JPQL filter"
```

### Görev 5 (Kişi C): Ortak hata altyapısı

**Dosyalar:**
- Oluştur (`src/main/java/com/roommates/common/error/` altında): `ErrorResponse.java`, `FieldErrorItem.java`, `ResourceNotFoundException.java`, `InvalidRequestException.java`, `ConflictException.java`, `ErrorResponseWriter.java`, `GlobalExceptionHandler.java`

**Arayüzler:**
- Tüketir: `Clock` (Görev 1).
- Üretir (`ErrorResponse`):
  - `record ErrorResponse(Instant timestamp, int status, String error, String message, String path, List<FieldErrorItem> fieldErrors)`.
  - Sınıf düzeyinde `@JsonInclude(JsonInclude.Include.NON_EMPTY)` ve `@JsonPropertyOrder({"timestamp", "status", "error", "message", "path", "fieldErrors"})`.
  - Fabrika metodu: `static ErrorResponse of(Clock clock, HttpStatusCode status, String message, String path, List<FieldErrorItem> fieldErrors)`. `error` alanı `HttpStatus.valueOf(status.value()).getReasonPhrase()` ile doldurulur.
- Üretir (`FieldErrorItem`): `record FieldErrorItem(String field, String message)`.
- Üretir (exception'lar):
  - `ResourceNotFoundException(String resourceName, Long id)`. Mesajı `resourceName + " bulunamadı: id=" + id`.
  - `InvalidRequestException(String message)`.
  - `ConflictException(String message)`.
- Üretir (`ErrorResponseWriter`):
  - `@Component`, constructor `(JsonMapper jsonMapper, Clock clock)`.
  - Metot: `void write(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException`.
  - Content-Type `application/json;charset=UTF-8`.
  - Görev 7'deki 401/403 işleyicileri bunu kullanır.
- Üretir (`GlobalExceptionHandler`):
  - `@RestControllerAdvice public class GlobalExceptionHandler extends ResponseEntityExceptionHandler`, constructor `(Clock clock)`.
  - Spring'in `org.springframework.web.ErrorResponse` arayüzüyle isim çakışması var. Bizim sınıfımız tam adla kullanılır ya da Spring'inki import edilmez.

- [ ] **Adım 1: Kontrolleri hazırla (şu an başarısız olacaklar)**

Bu kontroller, Görev 8'deki ilk controller birleşene kadar gerçek uç noktalara karşı çalıştırılamaz. Görevin içinde, yalnız bu PR'da duran ve birleşmeden önce silinen geçici bir `@RestController` (`common/error/ErrorProbeController`) kullanılır. Uçları:
- `GET /api/_probe/notfound`: `ResourceNotFoundException("Oda", 42L)` fırlatır.
- `POST /api/_probe/valid`: `@Valid` bir record alır (`@NotBlank String name`).
- `GET /api/_probe/num/{n}`: `Long` path değişkeni alır.
- `GET /api/_probe/boom`: `IllegalStateException` fırlatır.

- [ ] **Adım 2: Exception sınıflarını, `ErrorResponse`'u ve `ErrorResponseWriter`'ı yaz**

- [ ] **Adım 3: `GlobalExceptionHandler`'ı yaz**

- `handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request)` ezilir. Spring MVC'nin standart hataları buradan `ErrorResponse`'a çevrilir. Mesaj seçimi:
  - `HttpMessageNotReadableException` → `"İstek gövdesi okunamadı"`
  - `MissingServletRequestParameterException` → `"<ad> parametresi eksik"`
  - `TypeMismatchException` / `MethodArgumentTypeMismatchException` → `"<ad> parametresi geçersiz"`
  - `NoResourceFoundException` → `"Yol bulunamadı"`
  - Diğerleri (405, 406, 415 dahil) → `ProblemDetail` gövdesinin `detail`'i, o yoksa `ex.getMessage()`.
- `handleMethodArgumentNotValid` ezilir: mesaj `"İstek doğrulanamadı"`, `fieldErrors` her `FieldError` için `(field, defaultMessage)`.
- `@ExceptionHandler` metotları:
  - `ResourceNotFoundException` → 404.
  - `InvalidRequestException` → 400.
  - `ConflictException` → 409.
  - `DataIntegrityViolationException` → 409 `"Kayıt mevcut verilerle çakışıyor"`.
  - `Exception` → 500 `"Beklenmeyen bir hata oluştu"`, ayrıntı `log.error` ile yalnız log'a yazılır.
- `path` alanı `((ServletWebRequest) request).getRequest().getRequestURI()`.

- [ ] **Adım 4: Kontrolleri çalıştır**

```bash
scripts/check.sh 404 GET /api/_probe/notfound
scripts/check.sh 400 POST /api/_probe/valid - '{}'
scripts/check.sh 400 POST /api/_probe/valid - '{bozuk'
scripts/check.sh 400 GET /api/_probe/num/abc
scripts/check.sh 405 DELETE /api/_probe/notfound
scripts/check.sh 500 GET /api/_probe/boom
curl -s -X POST -H 'Content-Type: text/plain' --data x localhost:8080/api/_probe/valid -o /dev/null -w '%{http_code}\n'   # 415
```

Beklenen:
- Hepsi PASS, son satır `415`.
- `{}` cevabında `fieldErrors[0].field == "name"`.
- 404 cevabının gövdesi bu sırayla `timestamp`, `"status":404`, `"error":"Not Found"`, `"message":"Oda bulunamadı: id=42"`, `"path":"/api/_probe/notfound"` içeriyor. `fieldErrors` anahtarı yok.

(Geçici `SecurityConfig` her şeye izin verdiği için bu adımda kimlik bilgisi gerekmez.)

- [ ] **Adım 5: Geçici prob controller'ını sil ve commit et**

```bash
rm src/main/java/com/roommates/common/error/ErrorProbeController.java
git add -A src/main/java/com/roommates/common/error && git commit -m "feat(error): add shared error model, custom exceptions and global handler"
```

### Görev 6 (Kişi D): Actuator bilgileri ve OpenAPI yapılandırması

**Dosyalar:**
- Değiştir: `src/main/resources/application.properties`
- Oluştur: `src/main/java/com/roommates/common/config/OpenApiConfig.java`

**Arayüzler:**
- Üretir: `"basicAuth"` adlı OpenAPI güvenlik şeması. Controller'lar ayrıca bir şey eklemez, global gereksinim olarak gelir.

- [ ] **Adım 1: Kontrolü hazırla (şu an başarısız)**

Çalıştır: `curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/actuator/info`
Beklenen (şu an): `404`. Boot varsayılan olarak yalnız `health`'i açar.

- [ ] **Adım 2: Actuator özelliklerini ekle**

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.info.env.enabled=true
management.endpoint.health.show-details=when-authorized
management.endpoint.health.roles=ROLE_ADMIN
info.app.name=RoomMates
info.app.description=@project.description@
info.app.version=@project.version@
```

- [ ] **Adım 3: `OpenApiConfig`'i yaz**

Yalnız anotasyonlardan oluşan bir `@Configuration` sınıfı:
- `@OpenAPIDefinition(info = @Info(title = "RoomMates API", version = "0.1.0", description = "…"), security = @SecurityRequirement(name = "basicAuth"))`
- `@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")`

- [ ] **Adım 4: Doğrula**

Çalıştır: `curl -s localhost:8080/actuator/info`
Beklenen: cevapta ayrı ayrı `"name":"RoomMates"`, `"version":"0.1.0"` ve `Öğrencilerin` ile başlayan `"description"` var (alan sırasına bakılmaz).

Çalıştır: `curl -s localhost:8080/v3/api-docs | grep -o '"basicAuth"' | head -1`
Beklenen: `"basicAuth"`.

Tarayıcıda `http://localhost:8080/swagger-ui.html` açılıyor ve "Authorize" düğmesi görünüyor.

`description` içindeki Türkçe karakterler bozuk görünürse (`Ã–` gibi), `.properties` dosyası ISO-8859-1 olarak okunuyor demektir. Bu durumda `info.app.*` üç satırı `src/main/resources/application.yml`'e (YAML her zaman UTF-8 okunur) taşınır ve kontrol tekrarlanır. YAML'da `@` ile başlayan değerler tırnak içinde yazılır: `description: "@project.description@"`.

- [ ] **Adım 5: Commit**

```bash
git add -A && git commit -m "feat(config): expose actuator info and configure OpenAPI basic auth scheme"
```

### Görev 7 (Kişi A): Güvenlik — Basic auth, Argon2id, yetki matrisi, örnek kullanıcılar

**Bağımlılık:** Görev 3, Görev 5 ve Görev 6 birleşmiş olmalı. Görev 6 olmadan `/actuator/info` ve `/actuator/metrics` açık değil.

**Dosyalar:**
- Değiştir: `src/main/java/com/roommates/common/security/SecurityConfig.java` (geçici sürüm baştan yazılır)
- Oluştur (`common/security/` altında): `DatabaseUserDetailsService.java`, `JsonAuthenticationEntryPoint.java`, `JsonAccessDeniedHandler.java`
- Oluştur (`user/` altında): `UserSeeder.java`, `UserService.java`, `UserServiceImpl.java` (bu görevde yalnız seed metodu)

**Arayüzler:**
- Tüketir: `UserRepository`, `RoleRepository` (Görev 3), `ErrorResponseWriter` (Görev 5), `Clock`.
- Üretir (`SecurityConfig`):
  - Constructor: `SecurityConfig(JsonAuthenticationEntryPoint entryPoint, JsonAccessDeniedHandler accessDeniedHandler)`.
  - `@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)`.
  - `@Bean PasswordEncoder passwordEncoder()`, `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()` döner.
- Üretir (`UserService`): `void seedDefaultUsers(String adminPassword, String editorPassword, String viewerPassword, String inactivePassword)`. `@Transactional` olur. Görev 9 aynı arayüze CRUD metotlarını ekler.
- Üretir (`UserSeeder`):
  - `implements ApplicationRunner`.
  - Constructor `(UserService userService, @Value("${roommates.seed.admin-password}") String adminPassword, …)`, dört parola alır.

- [ ] **Adım 1: Kontrolleri hazırla (şu an başarısız, geçici yapılandırma her şeye izin veriyor)**

```bash
source scripts/creds.sh
scripts/check.sh 401 GET /api/rooms
scripts/check.sh 401 GET /api/rooms 'admin:yanlis'
scripts/check.sh 401 GET /actuator/metrics
scripts/check.sh 403 GET /actuator/metrics "$E"
scripts/check.sh 200 GET /actuator/metrics "$A"
scripts/check.sh 200 GET /actuator/health
scripts/check.sh 200 GET /actuator/info
scripts/check.sh 401 GET /api/rooms "$P"
scripts/check.sh 403 GET /api/users "$E"
scripts/check.sh 403 PATCH /api/rooms/1 "$V" '{}'
scripts/check.sh 403 DELETE /api/rooms/1 "$E"
scripts/check.sh 200 GET /v3/api-docs
```

`/api/rooms/1` gibi sabit id'ler burada bilerek kullanılıyor: 401/403 controller'a ulaşmadan oluşur, kaydın var olup olmadığı önemli değil. `/api/rooms` henüz yok. Bu yüzden yetkili bir GET kontrolü ancak Görev 8'den sonra 200 verir. Yukarıdaki listede yalnız güvenlik katmanının sonucu ölçülüyor: 401 ve 403 controller'a ulaşmadan oluşur.

- [ ] **Adım 2: `JsonAuthenticationEntryPoint` ve `JsonAccessDeniedHandler`'ı yaz**

Entry point:
- `WWW-Authenticate: Basic realm="RoomMates"` başlığını ekler.
- Ardından `ErrorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, mesaj, request.getRequestURI())` çağırır.
- Mesaj seçimi:
  - `BadCredentialsException` → `"Kullanıcı adı veya parola hatalı"`
  - `DisabledException` → `"Hesap pasif"`
  - diğerleri → `"Kimlik doğrulaması gerekli"`

Access denied işleyicisi: 403, mesaj `"Bu işlem için yetkiniz yok"`.

- [ ] **Adım 3: `DatabaseUserDetailsService`'i yaz**

- `@Service`, constructor `(UserRepository userRepository)`.
- `findByUsernameWithRoles` ile kullanıcıyı yükler. Bulamazsa `UsernameNotFoundException` fırlatır.
- `org.springframework.security.core.userdetails.User.withUsername(u.getUsername()).password(u.getPassword()).authorities(rol adları).disabled(!u.isEnabled()).build()` döner.

- [ ] **Adım 4: `SecurityConfig`'i yaz**

- Genel ayarlar: `csrf` kapalı, `formLogin` kapalı, `sessionManagement` `STATELESS`.
- Entry point iki yerde kayıtlı olmalı (tasarım §6.3 son paragraf):
  - `httpBasic(b -> b.authenticationEntryPoint(entryPoint))`
  - `exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))`
- `authorizeHttpRequests` kuralları, **bu sırayla**:
  1. `/error`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `GET /actuator/health`, `GET /actuator/health/**`, `GET /actuator/info` → `permitAll()`
  2. `/actuator/**` → `hasAuthority("ROLE_ADMIN")`
  3. `/api/users/**` → `hasAuthority("ROLE_ADMIN")`
  4. `DELETE /api/**` → `hasAuthority("ROLE_ADMIN")`
  5. `POST`, `PUT`, `PATCH /api/**` → `hasAuthority("ROLE_EDITOR")`
  6. `GET /api/**` → `hasAuthority("ROLE_VIEWER")`
  7. `anyRequest()` → `denyAll()`

- [ ] **Adım 5: `UserService.seedDefaultUsers` ve `UserSeeder`'ı yaz**

`seedDefaultUsers` adımları:
1. Üç rolü yoksa oluşturur.
2. Tasarım §7.3'teki dört kullanıcıyı **bu sırayla** (`admin`, `editor`, `viewer`, `pasif`), `findByUsername` boş dönüyorsa oluşturur. E-postalar `<kullanici>@roommates.local`, görünen adlar `Yönetici`, `Editör`, `İzleyici`, `Pasif Kullanıcı`.
3. Parolalar `PasswordEncoder.encode` ile kodlanır.
4. `pasif` kullanıcısının `enabled` alanı `false`.

`UserSeeder.run`, `seedDefaultUsers`'ı çağırır.

- [ ] **Adım 6: Kontrolleri çalıştır**

Adım 1'deki komutlar çalıştırılır.
Beklenen:
- Hepsi PASS.
- `admin:yanlis` cevabının gövdesinde `"message":"Kullanıcı adı veya parola hatalı"` var.
- 403 cevabının gövdesi `ErrorResponse` biçiminde.

Ek kontroller:
- `mysql ... -e "SELECT username, LEFT(password, 10), enabled FROM users"`. Beklenen: parolalar `$argon2id$` ile başlıyor, `pasif` için `enabled=0`.
- Uygulamayı yeniden başlat. Beklenen: `SELECT COUNT(*) FROM users` hâlâ 4.

- [ ] **Adım 7: Commit**

```bash
git add -A && git commit -m "feat(security): add HTTP Basic with Argon2id, DB-backed users, role matrix and JSON 401/403"
```

---

## Aşama 2 — Modüller

### Görev 8 (Kişi B): Oda CRUD — referans uygulama

**Bağımlılık:** Görev 4, 5 ve 7 birleşmiş olmalı. Bu görev birleşmeden diğer CRUD görevleri PR açmaz. Kod yazmaya başlayabilirler, ama kalıbı bundan alırlar.

**Dosyalar:**
- Oluştur (`room/dto/` altında): `RoomCreateRequest.java`, `RoomUpdateRequest.java`, `RoomPatchRequest.java`, `RoomResponse.java`
- Oluştur (`room/` altında): `RoomService.java`, `RoomServiceImpl.java`, `RoomController.java`

**Arayüzler:**
- Tüketir: `RoomRepository` (Görev 4), `UserRepository.findByUsername` (Görev 3), exception'lar (Görev 5), `Clock`.
- Üretir (DTO'lar):
  - `RoomCreateRequest(@NotBlank @Size(max=100) String name, @Size(max=500) String description, @NotNull RoomType type, @NotNull RoomVisibility visibility, @NotNull @Min(2) @Max(50) Integer maxMembers, @Future Instant closesAt)`.
  - `RoomUpdateRequest`: aynı alanlar ve aynı kısıtlar. `description` ve `closesAt` null olabilir; PUT'ta null göndermek o alanı temizler.
  - `RoomPatchRequest(Long id, @Size(min=1, max=100) String name, @Size(max=500) String description, RoomType type, RoomVisibility visibility, @Min(2) @Max(50) Integer maxMembers, @Future Instant closesAt)`.
  - `RoomResponse(Long id, String name, String description, RoomType type, RoomVisibility visibility, Integer maxMembers, Instant createdAt, Instant closesAt, Long ownerId, String ownerUsername)` ve `static RoomResponse from(Room room)`.
- Üretir (`RoomService`):
  - `List<RoomResponse> findAll(RoomType type, RoomVisibility visibility)`
  - `RoomResponse findById(Long id)`
  - `RoomResponse create(RoomCreateRequest request, String username)`
  - `RoomResponse update(Long id, RoomUpdateRequest request)`
  - `RoomResponse patch(Long id, RoomPatchRequest request)`
  - `void delete(Long id)`
- `delete` bu görevde yalnız odayı siler. Çok tablolu sürüm Görev 15'tedir.

- [ ] **Adım 1: Kontrolleri hazırla (şu an 404/405)**

```bash
source scripts/creds.sh
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Algoritma Çalışması","type":"STUDY","visibility":"PUBLIC","maxMembers":8}'; R1=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$E" "{\"id\":$R1,\"name\":\"Film Gecesi\",\"type\":\"WATCH\",\"visibility\":\"PRIVATE\",\"maxMembers\":4}"; R2=$(cat /tmp/rm-last-id)
if [[ -n "$R2" && "$R2" != "$R1" ]]; then echo "PASS POST'taki id yok sayıldı"; else echo "FAIL POST'taki id mevcut kaydı ezdi"; exit 1; fi
scripts/check.sh 200 GET /api/rooms "$V"
scripts/check.sh 200 GET '/api/rooms?type=STUDY' "$V"; grep -c '"type":"WATCH"' /tmp/rm-check-body   # 0
scripts/check.sh 200 GET /api/rooms/$R1 "$V"; grep -o '"name":"Algoritma Çalışması"' /tmp/rm-check-body
scripts/check.sh 404 GET /api/rooms/999999 "$V"
scripts/check.sh 200 PUT /api/rooms/$R1 "$E" '{"name":"Algoritma 2","type":"STUDY","visibility":"PUBLIC","maxMembers":10}'
scripts/check.sh 400 PUT /api/rooms/$R1 "$E" '{"name":"Eksik"}'
scripts/check.sh 200 PATCH /api/rooms/$R1 "$E" '{"maxMembers":12}'; grep -o '"name":"Algoritma 2"' /tmp/rm-check-body
scripts/check.sh 400 PATCH /api/rooms/$R1 "$E" '{"id":5}'
scripts/check.sh 204 DELETE /api/rooms/$R2 "$A"
scripts/check.sh 404 DELETE /api/rooms/$R2 "$A"
```

- [ ] **Adım 2: DTO'ları yaz**

- [ ] **Adım 3: `RoomServiceImpl`'i yaz**

- Constructor: `(RoomRepository roomRepository, UserRepository userRepository, Clock clock)`.
- `create`:
  - Sahip `userRepository.findByUsername(username)` ile yüklenir; yoksa `InvalidRequestException("Giriş yapan kullanıcı bulunamadı")` fırlatılır.
  - `createdAt = Instant.now(clock)`.
  - Kayıt `roomRepository.save(new Room(...))` ile oluşturulur.
- `patch`: Tasarım §5.2'deki PATCH kurallarının 1–5. maddeleri sırayla uygulanır. id kontrolü kayıt bulunduktan sonra yapılır (önce 404, sonra 400).
- `update`: Tüm alanlar yazılır.
- Her metot `RoomResponse.from(...)`'u transaction içinde döndürür.
- Okuma metotları `@Transactional(readOnly = true)`, diğerleri `@Transactional`.

- [ ] **Adım 4: `RoomController`'ı yaz**

- `@RestController @RequestMapping("/api/rooms") @Tag(name = "Odalar")`, constructor `(RoomService roomService)`.
- `GET` metodu `@RequestParam(required = false) RoomType type, @RequestParam(required = false) RoomVisibility visibility` alır.
- `POST` metodu `Authentication authentication` parametresi alır ve servise `authentication.getName()` geçirir.
- Her metotta:
  - `@Operation(summary = "…")`
  - Hata kodları için `@ApiResponse(responseCode = "…", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))`. Hangi kodlar: GET/{id} için 404; POST, PUT ve PATCH için 400; DELETE için 404; hepsi için 401 ve 403.
- POST'un sonucu: `ResponseEntity.created(location).body(response)`.

- [ ] **Adım 5: Kontrolleri ve "Gözden kaçabilecek durumlar" 1–3'ü çalıştır**

Adım 1'deki komutlar çalıştırılır. Ek kontroller:

```bash
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Çalışma Odası ğüşıöç","type":"STUDY","visibility":"PUBLIC","maxMembers":5}'; R3=$(cat /tmp/rm-last-id)
scripts/check.sh 200 GET /api/rooms/$R3 "$V"; grep -o 'Çalışma Odası ğüşıöç' /tmp/rm-check-body   # aynen dönmeli
scripts/check.sh 400 POST /api/rooms "$E" "{\"name\":\"$(printf 'a%.0s' {1..101})\",\"type\":\"WATCH\",\"visibility\":\"PUBLIC\",\"maxMembers\":3}"
scripts/check.sh 200 PATCH /api/rooms/$R1 "$E" '{}'; grep -o '"maxMembers":12' /tmp/rm-check-body
```

Beklenen:
- Hepsi PASS.
- `id` gönderilen POST yeni bir kayıt oluşturdu (`R2 ≠ R1`), `R1` odasının adı değişmemiş.
- `{"maxMembers":12}` PATCH'inden sonra `name` hâlâ `"Algoritma 2"`.
- `{}` PATCH'i hiçbir alanı değiştirmemiş (`maxMembers` hâlâ 12).
- 101 karakterlik ad için `fieldErrors[0].field == "name"`.
- `?type=STUDY` yalnız STUDY odalarını döndürüyor.

- [ ] **Adım 6: Postman klasörünü dışa aktar**

`postman/parts/rooms.postman_collection.json`: Adım 1 ve Adım 5'teki her kontrol bir istek olarak eklenir. Kimlik bilgileri `{{editorUser}}`/`{{editorPass}}` gibi değişkenlerle, adres `{{baseUrl}}` ile yazılır. POST isteğinin Tests sekmesine `pm.collectionVariables.set("roomId", pm.response.json().id)` eklenir.

- [ ] **Adım 7: Commit**

```bash
git add -A && git commit -m "feat(room): add rooms CRUD as the reference layered implementation"
```

### Görev 9 (Kişi A): Kullanıcı CRUD

**Bağımlılık:** Görev 7 ve Görev 8 birleşmiş olmalı.

**Dosyalar:**
- Oluştur (`user/dto/` altında): `UserCreateRequest.java`, `UserUpdateRequest.java`, `UserPatchRequest.java`, `UserResponse.java`
- Değiştir (`user/` altında): `UserService.java`, `UserServiceImpl.java`
- Oluştur (`user/` altında): `UserController.java`

**Arayüzler:**
- Tüketir: `UserRepository`, `RoleRepository`, `PasswordEncoder`, `Clock`.
- Üretir (DTO'lar):
  - `UserCreateRequest(@NotBlank @Size(max=50) String username, @NotBlank @Size(min=8, max=72) String password, @NotBlank @Email @Size(max=100) String email, @NotBlank @Size(max=100) String displayName, @NotEmpty Set<String> roles, Boolean enabled)`. `enabled` null ise `true` kabul edilir.
  - `UserUpdateRequest`: aynı alanlar, `enabled` `@NotNull`.
  - `UserPatchRequest(Long id, …)`: tüm alanlar nullable, aynı boyut kısıtları.
  - `UserResponse(Long id, String username, String email, String displayName, boolean enabled, Instant createdAt, List<String> roles)`. Parola alanı **yok**.
- Üretir (`UserService` arayüzüne eklenenler): `List<UserResponse> findAll(Boolean enabled)`, `UserResponse findById(Long id)`, `UserResponse create(UserCreateRequest request)`, `UserResponse update(Long id, UserUpdateRequest request)`, `UserResponse patch(Long id, UserPatchRequest request)`, `void delete(Long id)`.

- [ ] **Adım 1: Kontrolleri hazırla**

```bash
source scripts/creds.sh
SFX=$RANDOM; AY="ayse$SFX"; OS="odasahibi$SFX"
scripts/check.sh 201 POST /api/users "$A" "{\"username\":\"$AY\",\"password\":\"Parola.123\",\"email\":\"$AY@roommates.local\",\"displayName\":\"Ayşe\",\"roles\":[\"ROLE_VIEWER\"]}"; U=$(cat /tmp/rm-last-id)
scripts/check.sh 409 POST /api/users "$A" "{\"username\":\"$AY\",\"password\":\"Parola.123\",\"email\":\"baska$SFX@roommates.local\",\"displayName\":\"X\",\"roles\":[\"ROLE_VIEWER\"]}"
scripts/check.sh 409 POST /api/users "$A" "{\"username\":\"baska$SFX\",\"password\":\"Parola.123\",\"email\":\"$AY@roommates.local\",\"displayName\":\"X\",\"roles\":[\"ROLE_VIEWER\"]}"
scripts/check.sh 400 POST /api/users "$A" '{"username":"x","password":"Parola.123","email":"x@roommates.local","displayName":"X","roles":["ROLE_YOK"]}'
scripts/check.sh 200 GET '/api/users?enabled=false' "$A"; grep -o '"username":"pasif"' /tmp/rm-check-body
scripts/check.sh 200 PATCH /api/users/$U "$A" '{"password":"YeniParola.1"}'
scripts/check.sh 200 GET /api/rooms "$AY:YeniParola.1"
scripts/check.sh 400 PATCH /api/users/$U "$A" '{"id":1}'
# Silinemeyen kullanıcı: kendi odası olan geçici bir editör
scripts/check.sh 201 POST /api/users "$A" "{\"username\":\"$OS\",\"password\":\"Parola.123\",\"email\":\"$OS@roommates.local\",\"displayName\":\"Oda Sahibi\",\"roles\":[\"ROLE_VIEWER\",\"ROLE_EDITOR\"]}"; O=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$OS:Parola.123" '{"name":"Sahipli oda","type":"WATCH","visibility":"PUBLIC","maxMembers":3}'; OR=$(cat /tmp/rm-last-id)
scripts/check.sh 409 DELETE /api/users/$O "$A"
scripts/check.sh 204 DELETE /api/rooms/$OR "$A"
scripts/check.sh 204 DELETE /api/users/$O "$A"
scripts/check.sh 204 DELETE /api/users/$U "$A"
```

Kontroller seed kullanıcılarını değiştirmez ve silmez. Postman parçası da aynı kuralla yazılır. Geçici kullanıcıların adı ve parolası, POST isteğinin Pre-request/Tests betiklerinde `tempUser`/`tempPass` ve `ownerUser`/`ownerPass` koleksiyon değişkenlerine yazılır (`pm.collectionVariables.set`). Onlarla giriş yapan istekler de bu değişkenleri kullanır, kimlik bilgisi elle yazılmaz (M38).

- [ ] **Adım 2: DTO'ları ve servis metotlarını yaz**

- Rol adları `RoleRepository.findByName` ile çözülür. Bilinmeyen rol için `InvalidRequestException("Bilinmeyen rol: <ad>")` fırlatılır.
- Benzersizlik `findByUsername` ve `findByEmail` ile kontrol edilir; bulunan kayıt güncellenen kaydın kendisi değilse `ConflictException("Kullanıcı adı zaten kullanılıyor")` / `ConflictException("E-posta zaten kullanılıyor")` fırlatılır.
- Parola yalnız `passwordEncoder.encode(...)` ile entity'ye yazılır.
- `delete`:
  1. `userRepository.delete(user)`, ardından `userRepository.flush()` çağrılır.
  2. `DataIntegrityViolationException` veya `jakarta.persistence.PersistenceException` yakalanırsa `ConflictException("Kullanıcının odaları veya oturumları olduğu için silinemez")` fırlatılır.

- [ ] **Adım 3: `UserController`'ı yaz**

- `@RequestMapping("/api/users") @Tag(name = "Kullanıcılar")`.
- `GET` metodu `@RequestParam(required = false) Boolean enabled` alır.
- Swagger anotasyonları Görev 8'deki kalıpla yazılır.

- [ ] **Adım 4: Kontrolleri ve "Gözden kaçabilecek durumlar" 5'i çalıştır**

```bash
scripts/check.sh 409 POST /api/users "$A" '{"username":"Admin","password":"Parola.123","email":"buyuk@roommates.local","displayName":"X","roles":["ROLE_VIEWER"]}'
scripts/check.sh 200 GET /api/users "$A"; grep -c '"password"' /tmp/rm-check-body   # 0
```

Beklenen:
- Hepsi PASS. 409 cevabı `ErrorResponse` biçiminde, 500 değil.
- Kullanıcı listesinde `"password"` anahtarı yok (son satır `0`).

- [ ] **Adım 5: Postman klasörünü dışa aktar ve commit et**

`postman/parts/users.postman_collection.json`: kullanıcı istekleri ve Görev 7'deki 401/403 kontrolleri. 401/403 istekleri "Hata senaryoları/401-403" alt klasörüne konur.

```bash
git add -A && git commit -m "feat(user): add admin-only users CRUD with role assignment and conflict checks"
```

### Görev 10 (Kişi B): Video kuyruğu CRUD

**Bağımlılık:** Görev 8.

**Dosyalar:**
- Oluştur (`video/` altında): `VideoQueueItem.java`, `VideoQueueItemRepository.java`, `VideoQueueItemRepositoryImpl.java`, `VideoQueueService.java`, `VideoQueueServiceImpl.java`, `VideoQueueController.java`
- Oluştur (`video/dto/` altında): `VideoCreateRequest.java`, `VideoUpdateRequest.java`, `VideoPatchRequest.java`, `VideoResponse.java`

**Arayüzler:**
- Tüketir: `RoomRepository`, `UserRepository`, `Clock`.
- Üretir (`VideoQueueItem`):
  - Tablo `video_queue_items`.
  - Alanlar: `youtubeVideoId` (20), `title` (200), `position` (`Integer`), `addedAt`, `room` (`room_id`), `addedBy` (`added_by_id`).
- Üretir (`VideoQueueItemRepository`):
  - Temel CRUD: `VideoQueueItem save(VideoQueueItem item)`, `Optional<VideoQueueItem> findById(Long id)`, `VideoQueueItem update(VideoQueueItem item)`, `void delete(VideoQueueItem item)`.
  - Oda bazlı sorgular: `List<VideoQueueItem> findByRoomId(Long roomId)` (`position` artan), `int countByRoomId(Long roomId)`, `int deleteByRoomId(Long roomId)`.
  - Sıra kaydırma: `int shiftPositions(Long roomId, int fromInclusive, int toInclusive, int delta)`, JPQL `UPDATE`.
- Üretir (DTO'lar):
  - `VideoCreateRequest(@NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{11}") String youtubeVideoId, @NotBlank @Size(max=200) String title, @Min(1) Integer position)`.
  - `VideoUpdateRequest`: aynı alanlar, `position` `@NotNull`.
  - `VideoPatchRequest(Long id, …)`.
  - `VideoResponse(Long id, Long roomId, String youtubeVideoId, String title, Integer position, Instant addedAt, String addedByUsername)`.
- Üretir (`VideoQueueService`): `findAll(Long roomId)`, `findById(Long roomId, Long videoId)`, `create(Long roomId, VideoCreateRequest request, String username)`, `update(Long roomId, Long videoId, VideoUpdateRequest request)`, `patch(Long roomId, Long videoId, VideoPatchRequest request)`, `delete(Long roomId, Long videoId)`. Hepsi `VideoResponse` ya da `List<VideoResponse>` / `void` döner.

**Sıra algoritması** (tasarım §5.3; `n` = kuyruktaki video sayısı):
- **Ekleme:** `position` null ise `n+1` kullanılır. `position` `1..n+1` dışındaysa 400. Önce `shiftPositions(room, p, n, +1)`, sonra ekleme.
- **Taşıma** (eski `o`, yeni `p`; `p` `1..n` dışındaysa 400):
  - `p < o` ise `shiftPositions(room, p, o-1, +1)`.
  - `p > o` ise `shiftPositions(room, o+1, p, -1)`.
  - Sonra videonun `position`'ı `p` yapılır.
- **Silme** (`position` = `o`): video silinir, sonra `shiftPositions(room, o+1, n, -1)`.

**İş kuralları:**
- Oda yoksa 404.
- Oda `WATCH` değilse 400 `"Video yalnız izleme odasına eklenebilir"`.
- Video başka bir odaya aitse 404 `"Video bulunamadı: id=<id>"`.

- [ ] **Adım 1: Kontrolleri hazırla**

```bash
source scripts/creds.sh
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Film","type":"WATCH","visibility":"PUBLIC","maxMembers":4}'; R=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Film 2","type":"WATCH","visibility":"PUBLIC","maxMembers":4}'; R2=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Ders","type":"STUDY","visibility":"PUBLIC","maxMembers":4}'; S=$(cat /tmp/rm-last-id)
for t in a b c; do scripts/check.sh 201 POST /api/rooms/$R/videos "$E" "{\"youtubeVideoId\":\"dQw4w9WgXc$t\",\"title\":\"Video $t\"}"; declare "V_$t=$(cat /tmp/rm-last-id)"; done
scripts/check.sh 200 PATCH /api/rooms/$R/videos/$V_c "$E" '{"position":1}'
scripts/check.sh 200 GET /api/rooms/$R/videos "$V"; grep -o '"title":"Video [abc]"' /tmp/rm-check-body | tr '\n' ' '   # Video c, Video a, Video b
scripts/check.sh 204 DELETE /api/rooms/$R/videos/$V_a "$A"
scripts/check.sh 200 GET /api/rooms/$R/videos "$V"; grep -o '"position":[0-9]*' /tmp/rm-check-body | tr '\n' ' '      # 1 2
scripts/check.sh 400 POST /api/rooms/$R/videos "$E" '{"youtubeVideoId":"kisa","title":"x"}'
scripts/check.sh 400 PATCH /api/rooms/$R/videos/$V_b "$E" '{"position":9}'
scripts/check.sh 400 POST /api/rooms/$S/videos "$E" '{"youtubeVideoId":"dQw4w9WgXcQ","title":"x"}'
scripts/check.sh 404 GET /api/rooms/$R2/videos/$V_b "$V"
scripts/check.sh 200 PUT /api/rooms/$R/videos/$V_b "$E" '{"youtubeVideoId":"dQw4w9WgXcQ","title":"Yeni","position":1}'
```

Sıra kaydırma JPQL `UPDATE` ile yapıldığı için persistence context'teki diğer video nesneleri bayatlar. Servis, kaydırmadan sonra yalnız taşınan videoyu değiştirir ve listeyi yeniden sorgular; bayat nesneleri kullanmaz.

- [ ] **Adım 2: Entity'yi, repository'yi ve servisi yaz, ardından controller'ı yaz**

Controller: `@RequestMapping("/api/rooms/{roomId}/videos") @Tag(name = "Video kuyruğu")`. `@Operation` ve `@ApiResponse` anotasyonları Görev 8'deki kalıpla yazılır.

- [ ] **Adım 3: Kontrolleri çalıştır**

Beklenen:
- Hepsi PASS.
- Taşımadan sonraki sıra `Video c, Video a, Video b`.
- Silmeden sonraki konumlar `1 2`, boşluk yok.

- [ ] **Adım 4: Postman klasörünü dışa aktar ve commit et**

```bash
git add -A && git commit -m "feat(video): add per-room video queue CRUD with gapless ordering"
```

### Görev 11 (Kişi C): Çalışma oturumu entity'si, repository'si ve `StudyTimerRegistry`

**Bağımlılık:** Görev 4.

**Dosyalar:**
- Oluştur (`study/` altında): `StudySessionStatus.java`, `StudySession.java`, `StudySessionRepository.java`, `StudySessionRepositoryImpl.java`, `StudyTimerRegistry.java`

**Arayüzler:**
- Üretir (`StudySessionStatus`):
  - `{ PLANNED, RUNNING, FINISHED }`.
  - Metot: `boolean canTransitionTo(StudySessionStatus next)`. `true` döndüğü durumlar: aynı durum, `PLANNED→RUNNING`, `RUNNING→FINISHED`. Diğer her geçiş `false`.
- Üretir (`StudySession`):
  - Tablo `study_sessions`.
  - Alanlar: `title` (100), `focusMinutes`, `breakMinutes`, `rounds` (`Integer`), `status`, `startedAt` (nullable), `room` (`room_id`).
  - Constructor: `StudySession(String title, Integer focusMinutes, Integer breakMinutes, Integer rounds, Room room)`. Başlangıç durumu `PLANNED`.
- Üretir (`StudySessionRepository`):
  - Temel CRUD: `save`, `findById`, `findAll`, `update`, `delete`.
  - Sorgular: `List<StudySession> findByFilter(Long roomId, StudySessionStatus status)`, `List<StudySession> findByStatus(StudySessionStatus status)`, `List<Long> findIdsByRoomId(Long roomId)`.
  - Silme: `int deleteByRoomId(Long roomId)`.
- Üretir (`StudyTimerRegistry`):
  - `@Component`, constructor `(StudySessionRepository repository)`.
  - Yaşam döngüsü: `@PostConstruct void init()`, `@PreDestroy void shutdown()`.
  - Metotlar: `void register(Long sessionId, Instant startedAt)`, `void unregister(Long sessionId)`, `boolean isActive(Long sessionId)`, `int activeCount()`.
  - Sınıf içinde `ConcurrentHashMap<Long, Instant>` tutulur.

- [ ] **Adım 1: Kontrolü hazırla**

Başlangıçta log'da `StudyTimerRegistry: 0 çalışan oturum yüklendi` aranır. Ctrl+C ile kapatınca log'da `StudyTimerRegistry: 0 zamanlayıcı kapatıldı` aranır. Şu an ikisi de yok.

- [ ] **Adım 2: Entity'yi, repository'yi ve registry'yi yaz**

- `init()`, `findByStatus(RUNNING)` ile çalışan oturumları okur ve her birini `startedAt` ile tabloya ekler. Bu metotta LAZY ilişkiye dokunulmaz ve yazma yapılmaz (tasarım §8 M10).
- `shutdown()`, tablodaki sayıyı log'a yazar ve tabloyu temizler.

- [ ] **Adım 3: Doğrula**

Çalıştır: `scripts/run-local.sh`, ardından Ctrl+C.
Beklenen:
- İki log satırı da görünüyor.
- `SHOW CREATE TABLE study_sessions\G` ile tablonun oluştuğu görülüyor.

- [ ] **Adım 4: Commit**

```bash
git add -A && git commit -m "feat(study): add StudySession entity, repository and lifecycle-managed timer registry"
```

### Görev 12 (Kişi C): Çalışma oturumu CRUD ve `@Value` varsayılanları

**Bağımlılık:** Görev 8 ve Görev 11.

**Dosyalar:**
- Oluştur (`study/` altında): `StudySessionService.java`, `StudySessionServiceImpl.java`, `StudySessionController.java`
- Oluştur (`study/dto/` altında): `StudySessionCreateRequest.java`, `StudySessionUpdateRequest.java`, `StudySessionPatchRequest.java`, `StudySessionResponse.java`

**Arayüzler:**
- Tüketir: `StudySessionRepository`, `StudyTimerRegistry`, `RoomRepository`, `Clock`.
- Üretir (DTO'lar):
  - `StudySessionCreateRequest(@NotBlank @Size(max=100) String title, @NotNull Long roomId, @Min(1) @Max(180) Integer focusMinutes, @Min(1) @Max(60) Integer breakMinutes, @NotNull @Min(1) @Max(12) Integer rounds)`. Durum alanı yok; oturum her zaman `PLANNED` başlar.
  - `StudySessionUpdateRequest`: aynı alanlar ve `@NotNull StudySessionStatus status`. Süreler burada `@NotNull`.
  - `StudySessionPatchRequest(Long id, …, StudySessionStatus status)`.
  - `StudySessionResponse(Long id, Long roomId, String title, Integer focusMinutes, Integer breakMinutes, Integer rounds, StudySessionStatus status, Instant startedAt, boolean timerActive)`.
- Üretir (`StudySessionService`): `findAll(Long roomId, StudySessionStatus status)`, `findById(Long id)`, `create(StudySessionCreateRequest request)`, `update(Long id, StudySessionUpdateRequest request)`, `patch(Long id, StudySessionPatchRequest request)`, `void delete(Long id)` ve **`void deleteByRoomId(Long roomId)`** (Görev 15 kullanır).
- `StudySessionServiceImpl` constructor'ı: `(StudySessionRepository repository, StudyTimerRegistry timerRegistry, RoomRepository roomRepository, Clock clock, @Value("${roommates.study.default-focus-minutes}") int defaultFocusMinutes, @Value("${roommates.study.default-break-minutes}") int defaultBreakMinutes)`.

**Durum geçişi** (PUT ve PATCH aynı yardımcıyı kullanır):
- `!current.canTransitionTo(next)` ise 400 `"Geçersiz durum geçişi: <current> → <next>"`.
- `PLANNED → RUNNING` geçişinde `startedAt = Instant.now(clock)` yazılır ve `timerRegistry.register` çağrılır.
- `RUNNING → FINISHED` geçişinde `timerRegistry.unregister` çağrılır.

**Diğer kurallar:**
- `delete` ve `deleteByRoomId` önce veritabanından siler, sonra registry'den çıkarır. `deleteByRoomId` silmeden önce id'leri `findIdsByRoomId` ile alır.
- **Bilinen sınır:** Transaction silmeden sonra geri alınırsa, registry'den çıkarılmış ama veritabanında duran bir `RUNNING` oturum kalabilir. Uygulama yeniden başlatılınca `init()` bunu düzeltir. Vize için kabul edildi.
- Oda `STUDY` değilse 400 `"Çalışma oturumu yalnız çalışma odasında açılabilir"`.

- [ ] **Adım 1: Kontrolleri hazırla**

```bash
source scripts/creds.sh
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Ders","type":"STUDY","visibility":"PUBLIC","maxMembers":4}'; S=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Film","type":"WATCH","visibility":"PUBLIC","maxMembers":4}'; W=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/study-sessions "$E" "{\"title\":\"Vize hazırlık\",\"roomId\":$S,\"rounds\":4}"; T=$(cat /tmp/rm-last-id)
grep -o '"focusMinutes":25' /tmp/rm-check-body; grep -o '"breakMinutes":5' /tmp/rm-check-body   # @Value varsayılanları
scripts/check.sh 400 POST /api/study-sessions "$E" "{\"title\":\"x\",\"roomId\":$W,\"rounds\":1}"
scripts/check.sh 404 POST /api/study-sessions "$E" '{"title":"x","roomId":999999,"rounds":1}'
scripts/check.sh 400 POST /api/study-sessions "$E" "{\"title\":\"x\",\"roomId\":$S,\"rounds\":1,\"focusMinutes\":500}"
scripts/check.sh 200 PATCH /api/study-sessions/$T "$E" '{"status":"RUNNING"}'; grep -o '"timerActive":true' /tmp/rm-check-body
scripts/check.sh 400 PATCH /api/study-sessions/$T "$E" '{"status":"PLANNED"}'
scripts/check.sh 400 PATCH /api/study-sessions/$T "$E" '{"status":"BITTI"}'
scripts/check.sh 200 GET "/api/study-sessions?roomId=$S&status=RUNNING" "$V"
scripts/check.sh 200 PUT /api/study-sessions/$T "$E" "{\"title\":\"Vize\",\"roomId\":$S,\"focusMinutes\":50,\"breakMinutes\":10,\"rounds\":2,\"status\":\"FINISHED\"}"
scripts/check.sh 400 GET /api/study-sessions/abc "$V"
scripts/check.sh 204 DELETE /api/study-sessions/$T "$A"
```

- [ ] **Adım 2: DTO'ları, servisi ve controller'ı yaz**

Controller: `@RequestMapping("/api/study-sessions") @Tag(name = "Çalışma oturumları")`. `@Operation` ve `@ApiResponse` anotasyonları Görev 8'deki kalıpla yazılır.

- [ ] **Adım 3: Kontrolleri çalıştır**

Beklenen: hepsi PASS.

Ardından M10 için yeniden başlatma denemesi:

```bash
scripts/check.sh 201 POST /api/study-sessions "$E" "{\"title\":\"Gece çalışması\",\"roomId\":$S,\"rounds\":2}"; T2=$(cat /tmp/rm-last-id)
scripts/check.sh 200 PATCH /api/study-sessions/$T2 "$E" '{"status":"RUNNING"}'
```

Uygulama Ctrl+C ile kapatılıp yeniden başlatılır.
Beklenen:
- Kapanış log'unda `1 zamanlayıcı kapatıldı`, açılış log'unda `1 çalışan oturum yüklendi` görünüyor.
- `GET /api/study-sessions/$T2` cevabında `"timerActive":true`.

Son olarak `T2` silinir.

- [ ] **Adım 4: Postman klasörünü dışa aktar ve commit et**

`postman/parts/study.postman_collection.json`: oturum istekleri. Ayrıca "Hata senaryoları/400-404" alt klasörü: eksik alan, bozuk JSON, yanlış enum, `/abc`, olmayan id.

```bash
git add -A && git commit -m "feat(study): add study sessions CRUD with @Value defaults and status transitions"
```

### Görev 13 (Kişi D): Soru sağlayıcıları — `@Configuration`, `@Bean`, `@Lazy`, `@Primary`, `@Qualifier`

**Bağımlılık:** Görev 1.

**Dosyalar:**
- Oluştur (`interview/` altında): `QuestionProvider.java`, `TechnicalQuestionProvider.java`, `BehavioralQuestionProvider.java`, `InterviewConfig.java`

**Arayüzler:**
- Üretir (`QuestionProvider`): `String nextQuestion(String topic)`.
- Üretir (`TechnicalQuestionProvider`):
  - Anotasyonsuz düz sınıf.
  - En az 5 Türkçe teknik soru içerir.
  - `nextQuestion`, konuyu soruya ekler: `"<topic> bağlamında: <soru>"`. Soru `ThreadLocalRandom` ile seçilir.
- Üretir (`BehavioralQuestionProvider`):
  - Anotasyonsuz düz sınıf.
  - Constructor'ında `log.info("Davranışsal soru bankası yüklendi")`.
  - En az 5 Türkçe davranışsal soru içerir.
- Üretir (`InterviewConfig`), `@Configuration`:
  - `@Bean @Primary QuestionProvider technicalQuestionProvider()`
  - `@Bean @Lazy @Qualifier("behavioral") QuestionProvider behavioralQuestionProvider()`
  - İki sağlayıcı yalnız burada tanımlanır; sınıflarda `@Component` olmaz.

- [ ] **Adım 1: Sağlayıcıları ve yapılandırmayı yaz**

- [ ] **Adım 2: Doğrula**

Çalıştır: `scripts/run-local.sh`.
Beklenen: açılış log'unda `Davranışsal soru bankası yüklendi` **yok**. (İlk kullanımda görünmesi Görev 14 adım 5'te doğrulanır.)

- [ ] **Adım 3: Commit**

```bash
git add -A && git commit -m "feat(interview): add primary and lazy qualified question providers via @Configuration"
```

### Görev 14 (Kişi D): Mülakat oturumu CRUD

**Bağımlılık:** Görev 8 ve Görev 13.

**Dosyalar:**
- Oluştur (`interview/` altında): `InterviewCategory.java`, `InterviewStatus.java`, `InterviewSession.java`, `InterviewSessionRepository.java`, `InterviewSessionRepositoryImpl.java`, `InterviewSessionService.java`, `InterviewSessionServiceImpl.java`, `InterviewSessionController.java`
- Oluştur (`interview/dto/` altında): `InterviewSessionCreateRequest.java`, `InterviewSessionUpdateRequest.java`, `InterviewSessionPatchRequest.java`, `InterviewSessionResponse.java`

**Arayüzler:**
- Tüketir: `RoomRepository`, `UserRepository`, `QuestionProvider` (iki bean), `Clock`.
- Üretir (enum'lar): `InterviewCategory { TECHNICAL, BEHAVIORAL }`, `InterviewStatus { SCHEDULED, IN_PROGRESS, COMPLETED }`.
- Üretir (`InterviewSession`):
  - Tablo `interview_sessions`.
  - Alanlar: `topic` (100), `category`, `question` (500), `status`, `scheduledAt`, `room` (`room_id`), `interviewer` (`interviewer_id`), `candidate` (`candidate_id`).
- Üretir (`InterviewSessionRepository`):
  - Temel CRUD: `save`, `findById`, `findAll`, `update`, `delete`.
  - Filtre: `List<InterviewSession> findByStatus(InterviewStatus status)`. Null ise tümünü döndürür; JPQL `:status IS NULL OR …` kalıbıyla.
  - Silme: `int deleteByRoomId(Long roomId)`.
- Üretir (DTO'lar):
  - `InterviewSessionCreateRequest(@NotNull Long roomId, @NotNull Long interviewerId, @NotNull Long candidateId, @NotBlank @Size(max=100) String topic, @NotNull InterviewCategory category, @NotNull Instant scheduledAt)`. Soru ve durum sunucuda belirlenir.
  - `InterviewSessionUpdateRequest`: aynı alanlar, ayrıca `@NotBlank @Size(max=500) String question` ve `@NotNull InterviewStatus status`.
  - `InterviewSessionPatchRequest(Long id, …)`: hepsi nullable.
  - `InterviewSessionResponse(Long id, Long roomId, Long interviewerId, String interviewerUsername, Long candidateId, String candidateUsername, String topic, InterviewCategory category, String question, InterviewStatus status, Instant scheduledAt)`.
- Üretir (`InterviewSessionService`): `findAll(InterviewStatus status)`, `findById(Long id)`, `create(...)`, `update(Long id, ...)`, `patch(Long id, ...)`, `void delete(Long id)`.
- `InterviewSessionServiceImpl` constructor'ı: `(InterviewSessionRepository repository, RoomRepository roomRepository, UserRepository userRepository, QuestionProvider technicalQuestionProvider, @Qualifier("behavioral") @Lazy QuestionProvider behavioralQuestionProvider)`.

**İş kuralları** (create, PUT ve PATCH'te ilgili alan değişince):
- Oda yoksa 404. Oda `INTERVIEW` değilse 400 `"Mülakat oturumu yalnız mülakat odasında açılabilir"`.
- Mülakatçı ya da aday yoksa 404 (`"Kullanıcı bulunamadı: id=<id>"`).
- `interviewerId == candidateId` ise 400 `"Mülakatçı ve aday aynı kişi olamaz"`.
- `create`'te soru `category`'ye göre ilgili sağlayıcıdan alınır, durum `SCHEDULED` yapılır.

- [ ] **Adım 1: Kontrolleri hazırla**

```bash
source scripts/creds.sh
U1=$(scripts/user-id.sh admin); U2=$(scripts/user-id.sh editor)
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Mülakat","type":"INTERVIEW","visibility":"PUBLIC","maxMembers":2}'; I=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/rooms "$E" '{"name":"Ders","type":"STUDY","visibility":"PUBLIC","maxMembers":4}'; S=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/interview-sessions "$E" "{\"roomId\":$I,\"interviewerId\":$U1,\"candidateId\":$U2,\"topic\":\"Spring\",\"category\":\"TECHNICAL\",\"scheduledAt\":\"2026-11-20T10:00:00Z\"}"; M=$(cat /tmp/rm-last-id)
scripts/check.sh 201 POST /api/interview-sessions "$E" "{\"roomId\":$I,\"interviewerId\":$U2,\"candidateId\":$U1,\"topic\":\"Takım çalışması\",\"category\":\"BEHAVIORAL\",\"scheduledAt\":\"2026-11-21T10:00:00Z\"}"; MB=$(cat /tmp/rm-last-id)
scripts/check.sh 400 POST /api/interview-sessions "$E" "{\"roomId\":$I,\"interviewerId\":$U1,\"candidateId\":$U1,\"topic\":\"x\",\"category\":\"TECHNICAL\",\"scheduledAt\":\"2026-11-20T10:00:00Z\"}"
scripts/check.sh 400 POST /api/interview-sessions "$E" "{\"roomId\":$S,\"interviewerId\":$U1,\"candidateId\":$U2,\"topic\":\"x\",\"category\":\"TECHNICAL\",\"scheduledAt\":\"2026-11-20T10:00:00Z\"}"
scripts/check.sh 404 POST /api/interview-sessions "$E" "{\"roomId\":$I,\"interviewerId\":$U1,\"candidateId\":999999,\"topic\":\"x\",\"category\":\"TECHNICAL\",\"scheduledAt\":\"2026-11-20T10:00:00Z\"}"
scripts/check.sh 200 GET '/api/interview-sessions?status=SCHEDULED' "$V"
scripts/check.sh 200 PATCH /api/interview-sessions/$M "$E" '{"status":"IN_PROGRESS"}'
scripts/check.sh 400 PATCH /api/interview-sessions/$M "$E" '{"id":99}'
scripts/check.sh 200 PUT /api/interview-sessions/$M "$E" "{\"roomId\":$I,\"interviewerId\":$U1,\"candidateId\":$U2,\"topic\":\"JPA\",\"category\":\"TECHNICAL\",\"question\":\"Lazy loading nedir?\",\"status\":\"COMPLETED\",\"scheduledAt\":\"2026-11-20T10:00:00Z\"}"
scripts/check.sh 204 DELETE /api/interview-sessions/$M "$A"
scripts/check.sh 204 DELETE /api/interview-sessions/$MB "$A"
```

Postman parçasında kullanıcı id'leri `scripts/user-id.sh` ile alınmaz. Parça şu sırayla çalışır:
1. Admin ile iki geçici kullanıcı oluşturur (`POST /api/users`, rastgele sonekli ad) ve id'lerini değişkenlere yazar.
2. Mülakat isteklerinde bu id'leri kullanır.
3. Sonunda **önce** oluşturduğu oturumları siler (ya da INTERVIEW odasını; Görev 15'ten sonra oda silme oturumları da siler), **sonra** kullanıcıları siler. Ters sırada kullanıcı silme foreign key yüzünden 409 verir.

- [ ] **Adım 2: Entity'yi, repository'yi, DTO'ları, servisi ve controller'ı yaz**

Controller: `@RequestMapping("/api/interview-sessions") @Tag(name = "Mülakat oturumları")`. `@Operation` ve `@ApiResponse` anotasyonları Görev 8'deki kalıpla yazılır.

- [ ] **Adım 3: Kontrolleri ve "Gözden kaçabilecek durumlar" 4'ü çalıştır**

```bash
scripts/check.sh 400 POST /api/interview-sessions "$E" "{\"roomId\":$I,\"interviewerId\":$U1,\"candidateId\":$U2,\"topic\":\"x\",\"category\":\"TECHNICAL\",\"scheduledAt\":\"2026-13-45\"}"
```

Beklenen:
- Hepsi PASS.
- `"Davranışsal soru bankası yüklendi"` log'u ilk BEHAVIORAL POST anında görünüyor, uygulama açılışında görünmüyor (M9).
- TECHNICAL oturumun `question` alanı teknik listeden, BEHAVIORAL oturumunki davranışsal listeden geliyor (M11).

- [ ] **Adım 4: Postman klasörünü dışa aktar ve commit et**

```bash
git add -A && git commit -m "feat(interview): add interview sessions CRUD using primary and qualified question providers"
```

### Görev 15 (Kişi B): Oda silmede çok tablolu transaction

**Bağımlılık:** Görev 10, 12 ve 14.

**Dosyalar:**
- Değiştir: `src/main/java/com/roommates/room/RoomServiceImpl.java` (constructor ve `delete`)

**Arayüzler:**
- Tüketir: `VideoQueueItemRepository.deleteByRoomId` (Görev 10), `StudySessionService.deleteByRoomId` (Görev 12), `InterviewSessionRepository.deleteByRoomId` (Görev 14).
- `RoomServiceImpl` constructor'ı genişler: `(RoomRepository, UserRepository, VideoQueueItemRepository, StudySessionService, InterviewSessionRepository, Clock)`.

- [ ] **Adım 1: Kontrolü hazırla (şu an 409 ya da 500)**

Üç oda hazırlanır:
- `R`: WATCH, 2 video.
- `S`: STUDY, 1 çalışma oturumu.
- `I`: INTERVIEW, 1 mülakat oturumu.

Ardından odalar silinir:

```bash
scripts/check.sh 204 DELETE /api/rooms/$R "$A"
scripts/check.sh 404 GET /api/rooms/$R/videos "$V"
mysql -u roommates -p roommates -e "SELECT COUNT(*) FROM video_queue_items WHERE room_id=$R"   # 0
```

Odalar ve çocuk kayıtları Görev 10, 12 ve 14'teki gibi `check.sh` ile oluşturulur; id'ler `/tmp/rm-last-id`'den alınır.

`S` ve `I` odaları için de aynısı yapılır; `study_sessions` ve `interview_sessions` sayıları 0 olmalı.

- [ ] **Adım 2: `delete`'i tasarım §5.3'teki sırayla yaz**

Yöntemin üstüne yorum yazılır: neden tek transaction ve sıranın neden önemli olduğu (önce çocuk kayıtlar, sonra oda; foreign key'ler).

- [ ] **Adım 3: Geri alınmayı doğrula**

`InterviewSessionRepository.deleteByRoomId` çağrısından hemen sonra geçici olarak `throw new IllegalStateException("test")` eklenir ve dolu bir WATCH odası silinmeye çalışılır.
Beklenen:
- 500 dönüyor.
- `video_queue_items` sayısı **değişmemiş** (geri alındı).

Satır kaldırılır.

- [ ] **Adım 4: Kontrolleri çalıştır ve commit et**

Beklenen: Adım 1'deki kontroller PASS.

```bash
git add -A && git commit -m "feat(room): delete room with its videos and sessions in a single transaction"
```

---

## Aşama 3 — Belgeleme, birleştirme, prova

### Görev 16a (Kişi D): README çalıştırma rehberi

**Bağımlılık:** Görev 15.

**Dosyalar:**
- Değiştir: `README.md`

- [ ] **Adım 1: README'yi tasarım §9'daki başlıklarla tamamla**

Başlıklar sırasıyla:
1. Proje
2. Gerekli yazılımlar
3. Veritabanı kurulumu
4. Ortam değişkenleri
5. Çalıştırma (Linux/macOS: `scripts/run-local.sh`; Windows: PowerShell'de ortam değişkenleri ve `mvnw.cmd spring-boot:run`)
6. Adresler (API, Swagger, OpenAPI JSON, health, info)
7. Örnek kullanıcılar: tasarım §7.3 tablosu ve parolaları
8. Yetki matrisi: tasarım §7.2
9. Bilinen sınırlar: PATCH ile alan boşaltılamaz
10. Sık hatalar

- [ ] **Adım 2: Swagger'ın eksiksiz olduğunu doğrula**

Çalıştır: `curl -s localhost:8080/v3/api-docs | grep -o '"/api/[^"]*"' | sort -u`
Beklenen: tam 10 yol. Her kaynağın iki yolu var: koleksiyon ve `/{id}`. Videolar için bunlar `/api/rooms/{roomId}/videos` ve `/api/rooms/{roomId}/videos/{videoId}`.

Swagger UI'da her yolun açıklaması, 4xx cevapları ve istek/cevap şemaları görünüyor (M36).

- [ ] **Adım 3: Commit**

```bash
git add README.md && git commit -m "docs: add run guide"
```

### Görev 16b (Kişi C): Tek Postman koleksiyonu

**Bağımlılık:** Görev 15. Tüm `postman/parts/*.json` dosyaları birleşmiş olmalı.

**Dosyalar:**
- Oluştur: `postman/RoomMates.postman_collection.json`, `postman/RoomMates-local.postman_environment.json`
- Sil: `postman/parts/`

- [ ] **Adım 1: Koleksiyonu birleştir**

- `postman/parts/*.json` dosyaları Postman'e içe aktarılır, klasörler tek koleksiyona taşınır.
- Koleksiyon değişkenleri: `baseUrl`, `adminUser`/`adminPass`, `editorUser`/`editorPass`, `viewerUser`/`viewerPass`, `pasifUser`/`pasifPass`. Ortam dosyası bunların yerel değerlerini içerir. Hiçbir istekte kimlik bilgisi elle yazılmaz (M38).
- Koleksiyon düzeyinde auth: Basic `{{editorUser}}`/`{{editorPass}}`.
- Klasör sırası: Odalar, Video kuyruğu, Çalışma oturumları, Mülakat oturumları, Kullanıcılar, Hata senaryoları (400, 404, 401, 403), Actuator. Her parça kendi kendine yettiği için sıra sonucu değiştirmez; bu sıra sunum akışına uyuyor.
- Her isteğin Tests sekmesinde beklenen durum kodu yazılı: `pm.response.to.have.status(…)`.

- [ ] **Adım 2: Doğrula**

Boş bir veritabanında uygulama başlatılır, Postman Collection Runner ile tüm koleksiyon **iki kez** çalıştırılır.
Beklenen:
- İki çalıştırmada da tüm testler yeşil.
- Seed kullanıcıları hâlâ 4 ve değişmemiş.

```bash
git add -A && git commit -m "docs: add merged Postman collection with environment"
```

### Görev 17 (hep birlikte): 38 maddelik prova

- [ ] **Adım 1: Kod taraması**

Çalıştır: `grep -rnE "@Autowired|JpaRepository|CrudRepository|PersistenceContext|jakarta.transaction.Transactional|com.fasterxml.jackson.databind" src/main/java`
Beklenen: çıktı yok.

Çalıştır: `grep -rlE "Repository|EntityManager" $(grep -rl "@RestController" src/main/java)`
Beklenen: çıktı yok. Controller'lar repository'ye ya da EntityManager'a dokunmuyor (M4).

Entity dosyaları gözle kontrol edilir. Her entity'de şunlar olmalı (M16, M17):
- `@Table`
- her kalıcı alanda `@Column` ya da `@JoinColumn`
- `protected` parametresiz constructor
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`

- [ ] **Adım 2: Temiz kurulum**

README'yi yazmamış bir üye `DROP DATABASE roommates`, ardından README'deki adımları izleyerek kurar.
Beklenen: Postman koleksiyonu baştan sona yeşil.

Ardından tüm görev kontrolleri sırayla yeniden çalıştırılır:

```bash
for f in scripts/checks/g*.sh; do echo "== $f"; bash -e "$f" || break; done
```

Beklenen: her dosya FAIL olmadan bitiyor.

- [ ] **Adım 3: Kalıcılık**

Bir oda oluşturulur, uygulama durdurulup başlatılır, oda GET ile alınır (M15).

- [ ] **Adım 4: 38 madde tablosunu doldur**

Tasarım §12'deki tabloyu her üye kendi maddeleri için doldurur. "Gösterim" sütununa dosya:satır veya Postman istek adı yazılır. Sunum sırası bu tabloya göre yapılır.

- [ ] **Adım 5: Commit**

```bash
git add -A && git commit -m "docs: record checklist evidence for midterm presentation"
```
