# RoomMates

## 1. Proje

RoomMates, öğrencilerin üç tür odada buluştuğu bir platformun REST API'sidir:

- **İzleme odası (WATCH):** YouTube videoları sıralı bir kuyruğa eklenir.
- **Çalışma odası (STUDY):** Ortak zamanlayıcılı çalışma oturumları (varsayılan 25 dk çalışma, 5 dk mola).
- **Mülakat odası (INTERVIEW):** Mülakatçı ve aday eşleşmeleri; soru, kategoriye göre sunucuda seçilir.

Gümüşhane Üniversitesi YZMSEC319 Sunucu Uygulama Geliştirme dersi vize projesidir. Ön yüz yoktur; API, Swagger UI ve Postman ile kullanılır.

**Teknolojiler:** Java 25, Spring Boot 4.1.2, Spring Security (HTTP Basic, Argon2id), Hibernate / JPA (`EntityManager` ile elle yazılmış repository'ler, Spring Data yok), MySQL 8, springdoc-openapi 3.2.0, Maven Wrapper.

**Mimari:** Her modülde `Controller → Service (arayüz + @Service) → Repository (arayüz + EntityManager kullanan @Repository) → MySQL`.

| Modül | Paket | Uç noktalar |
|---|---|---|
| Kullanıcılar ve güvenlik | `user`, `common.security` | `/api/users` |
| Odalar | `room` | `/api/rooms` |
| Video kuyruğu | `video` | `/api/rooms/{roomId}/videos` |
| Çalışma oturumları | `study` | `/api/study-sessions` |
| Mülakat oturumları | `interview` | `/api/interview-sessions` |
| Ortak hata modeli | `common.error` | — |
| Yapılandırma | `common.config` | — |

## 2. Gerekli yazılımlar

| Yazılım | Sürüm | Not |
|---|---|---|
| JDK | 25 veya üstü | `java -version` ile kontrol edin |
| MySQL Server | 8.x | MySQL 8.4 ile denendi |
| Maven | Gerekmez | Projedeki Maven Wrapper (`mvnw`, `mvnw.cmd`) kullanılır |
| Postman | Güncel sürüm | Koleksiyonu çalıştırmak için (isteğe bağlı) |
| Git Bash | — | Windows'ta `scripts/*.sh` betikleri için (isteğe bağlı) |

## 3. Veritabanı kurulumu

1. MySQL sunucusunun çalıştığından emin olun (varsayılan port `3306`).
2. [docs/sql/01-create-db-user.sql](docs/sql/01-create-db-user.sql) dosyasındaki `BURAYA_PAROLA` yer tutucusunu kendi seçtiğiniz parolayla değiştirin.
3. Betiği root kullanıcısıyla çalıştırın:

   ```bash
   mysql -u root -p < docs/sql/01-create-db-user.sql
   ```

Betik `roommates` veritabanını `utf8mb4` / `utf8mb4_0900_ai_ci` ile ve yalnız bu veritabanına yetkili `roommates` kullanıcısını oluşturur. Tablolar uygulama ilk açıldığında Hibernate tarafından otomatik oluşturulur (`spring.jpa.hibernate.ddl-auto=update`); veriler yeniden başlatmada korunur.

## 4. Ortam değişkenleri

Bağlantı bilgileri ortam değişkenlerinden okunur:

| Değişken | Varsayılan | Açıklama |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/roommates` | JDBC adresi |
| `DB_USERNAME` | `roommates` | Veritabanı kullanıcısı |
| `DB_PASSWORD` | boş | 3. adımda seçtiğiniz parola |

[.env.example](.env.example) dosyasını `.env.local` adıyla kopyalayıp `DB_PASSWORD` değerini doldurun. `.env.local` git'e girmez.

Örnek kullanıcıların parolaları da isterseniz `SEED_ADMIN_PASSWORD`, `SEED_EDITOR_PASSWORD`, `SEED_VIEWER_PASSWORD`, `SEED_INACTIVE_PASSWORD` değişkenleriyle değiştirilebilir. Verilmezse 7. bölümdeki varsayılanlar kullanılır.

## 5. Çalıştırma

**Linux / macOS / Windows Git Bash:**

```bash
scripts/run-local.sh
```

Betik `.env.local` dosyasını okuyup `./mvnw spring-boot:run` çalıştırır.

**Windows PowerShell:**

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/roommates"
$env:DB_USERNAME = "roommates"
$env:DB_PASSWORD = "seçtiğiniz-parola"
.\mvnw.cmd spring-boot:run
```

**IntelliJ IDEA / Spring Tools:** IDE `.env.local` dosyasını okumaz. Run Configuration içindeki *Environment variables* alanına `DB_URL`, `DB_USERNAME` ve `DB_PASSWORD` değerlerini girip `RoomMatesApplication` sınıfını çalıştırın.

Uygulama açıldığında log'da şu satırlar görünür:

```
StudyTimerRegistry: 0 çalışan oturum yüklendi
Started RoomMatesApplication in ... seconds
```

## 6. Adresler

| Ne | Adres | Erişim |
|---|---|---|
| API | `http://localhost:8080/api/...` | Giriş gerekli |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | Herkese açık ("Authorize" ile giriş yapılır) |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | Herkese açık |
| Sağlık | `http://localhost:8080/actuator/health` | Herkese açık |
| Uygulama bilgisi | `http://localhost:8080/actuator/info` | Herkese açık |
| Metrikler | `http://localhost:8080/actuator/metrics` | Yalnız ADMIN |
| Postman koleksiyonu | [postman/RoomMates.postman_collection.json](postman/RoomMates.postman_collection.json) | Ortam: [postman/RoomMates-local.postman_environment.json](postman/RoomMates-local.postman_environment.json) |

Postman'de: *Import* → iki dosyayı seçin → sağ üstten **RoomMates Local** ortamını seçin → koleksiyonda *Run*.

## 7. Örnek kullanıcılar

Uygulama ilk açıldığında bu dört kullanıcı ve üç rol otomatik oluşturulur. Zaten varlarsa dokunulmaz. Parolalar veritabanında Argon2id ile şifreli saklanır.

| Kullanıcı | Parola | Roller | Durum | Ne için |
|---|---|---|---|---|
| `admin` | `Admin.2026!` | VIEWER, EDITOR, ADMIN | aktif | Silme, kullanıcı yönetimi, actuator |
| `editor` | `Editor.2026!` | VIEWER, EDITOR | aktif | Ekleme ve güncelleme (POST, PUT, PATCH) |
| `viewer` | `Viewer.2026!` | VIEWER | aktif | Yalnız okuma (GET) |
| `pasif` | `Pasif.2026!` | VIEWER | **pasif** | Doğru parolayla bile 401 |

Bunlar yalnız yerel değerlendirme için test verileridir.

## 8. Yetki matrisi

Yetkilendirme HTTP metodu ve adrese göre `SecurityConfig` sınıfında yapılır.

| İstek | VIEWER | EDITOR | ADMIN | Giriş yok |
|---|---|---|---|---|
| `GET /api/**` | ✓ | ✓ | ✓ | 401 |
| `POST`, `PUT`, `PATCH /api/**` | 403 | ✓ | ✓ | 401 |
| `DELETE /api/**` | 403 | 403 | ✓ | 401 |
| `/api/users/**` (tüm metotlar) | 403 | 403 | ✓ | 401 |
| `GET /actuator/health`, `GET /actuator/info` | ✓ | ✓ | ✓ | ✓ |
| Diğer `/actuator/**` | 403 | 403 | ✓ | 401 |
| Swagger UI, `/v3/api-docs` | ✓ | ✓ | ✓ | ✓ |

Bütün hatalar aynı JSON biçimiyle döner:

```json
{
  "timestamp": "2026-11-20T14:03:12.481Z",
  "status": 404,
  "error": "Not Found",
  "message": "Oda bulunamadı: id=42",
  "path": "/api/rooms/42"
}
```

Doğrulama hatalarında ayrıca `fieldErrors` listesi bulunur.

## 9. Bilinen sınırlar

- **PATCH ile alan boşaltılamaz:** PATCH'te `null` "gönderilmedi" anlamına gelir. `description`, `closesAt` gibi isteğe bağlı bir alanı boşaltmak için PUT kullanılır.
- **PATCH'te id gönderilemez:** Gövdede `id` varsa 400 döner (`Birincil anahtar (id) PATCH ile değiştirilemez`).
- **POST'ta id yok sayılır:** Gövdede `id` gönderilse bile her zaman yeni kayıt oluşur.
- **Odası, videosu veya mülakatı olan kullanıcı silinemez:** 409 döner. Önce ilgili odalar silinmelidir.
- **Oda silinince** o odanın videoları, çalışma oturumları ve mülakat oturumları da silinir.
- **Çalışma oturumu durumu** yalnız `PLANNED → RUNNING → FINISHED` yönünde ilerler. Bir oda silindiğinde o odanın çalışan oturumları zamanlayıcı listesinden uygulama yeniden başlatılınca düşer.
- Oda sahipliğine göre yetki (örneğin "odayı yalnız sahibi silebilir") vize kapsamında yoktur.

## 10. Sık hatalar

| Belirti | Sebep | Çözüm |
|---|---|---|
| `Access denied for user 'roommates'@'localhost'` | `DB_PASSWORD` yanlış veya boş | `.env.local` / ortam değişkenini kontrol edin |
| `Communications link failure` | MySQL çalışmıyor veya port farklı | MySQL servisini başlatın, `DB_URL`'deki portu kontrol edin |
| `Unknown database 'roommates'` | SQL betiği çalıştırılmamış | 3. bölümü uygulayın |
| IntelliJ'den açınca veritabanına bağlanamıyor | IDE `.env.local`'ı okumuyor | Ortam değişkenlerini Run Configuration'a girin |
| Parolanın sonunda görünmez karakter | `.env.local` Windows satır sonuyla (CRLF) kaydedilmiş | `scripts/` betikleri bunu kendisi temizler; başka araçla okutacaksanız dosyayı LF ile kaydedin |
| Her istekte 401 | Kullanıcı adı / parola yanlış veya `pasif` kullanıcı | 7. bölümdeki kullanıcıları kullanın |
| `Port 8080 was already in use` | Uygulama zaten açık | Açık olanı kapatın |
| Türkçe karakterler `Ã§` gibi görünüyor | İstemci gövdeyi UTF-8 göndermiyor | İsteği `Content-Type: application/json` ve UTF-8 ile gönderin |
