# RoomMates — Yazılım Gereksinimleri Belirtimi (SRS)

| | |
|---|---|
| **Sürüm** | 1.0 (taslak) |
| **Tarih** | 2026-11-08 |
| **Ders** | YZMSEC319 Sunucu Uygulama Geliştirme (Dr. Öğr. Üyesi Samet Tonyalı) |
| **Ekip** | Şevval (A), Pınar (B), Adem (C), Ayberk (D) |
| **Yapı** | IEEE Std 830-1998 bölümlerine göre |

---

## 1. Giriş

### 1.1 Amaç

Bu belge RoomMates sunucu uygulamasının ne yapması gerektiğini tanımlar. Ekip üyeleri, ders sorumlusu ve ileride sisteme istemci yazacak geliştiriciler için yazılmıştır. Nasıl yapılacağı (sınıflar, paketler, bağımlılıklar) vize aşaması için [tasarım belgesinde](superpowers/specs/2026-11-03-roommates-vize-design.md) anlatılır. Bu belge ürünün tamamını kapsar ve vize tasarımından daha geniştir.

### 1.2 Kapsam

RoomMates, öğrencilerin internet üzerinden üç tür odada buluştuğu bir platformun **REST API'sidir**:

- **İzleme odası:** YouTube videolarını ortak bir kuyruğa ekleyip birlikte izleme ve sohbet.
- **Çalışma odası:** Ortak pomodoro zamanlayıcısıyla birlikte çalışma ve mola verme, o an kimlerin çalıştığını görme.
- **Mülakat odası:** İki kullanıcının sırayla mülakatçı ve aday olduğu iki turlu görüşme, sonunda karşılıklı puanlı geri bildirim.

Sistem kullanıcı hesaplarını, odaları, oda üyeliklerini ve davetleri, sohbet mesajlarını, video kuyruklarını, çalışma ve mülakat oturumlarını yönetir. Finalde gerçek zamanlı senkronu (WebSocket) ve dil modeli destekli bir özelliği ekler.

**Kapsam dışı:** Ön yüz (HTML, şablon motoru, mobil uygulama). Ders bir REST API dersidir ve Bologna belgesi ön yüz beklemez. API, Swagger UI ve Postman ile kullanılır ve gösterilir. Video dosyası barındırma, görüntülü/sesli görüşme ve ödeme de kapsam dışıdır.

### 1.3 Sürüm etiketleri

Her gereksinim, hangi aşamada karşılanacağını gösteren bir etiket taşır:

| Etiket | Anlamı | Ölçüt |
|---|---|---|
| **[V]** | Vize | Teknik Gereklilikler Kontrol Listesi (38 madde) |
| **[F]** | Final | Bologna öğrenme çıktıları Ö01–Ö09 |
| **[S]** | Sonra | Dönem sonrası, gerçek kullanıma geçerken |

Bir gereksinim vizede kısmen, finalde tamamen karşılanıyorsa iki etiket birlikte yazılır ve fark açıklanır (ör. **[V→F]**).

### 1.4 Tanımlar ve kısaltmalar

| Terim | Anlamı |
|---|---|
| Oda | Kullanıcıların bir araya geldiği alan. Tipi WATCH (izleme), STUDY (çalışma) veya INTERVIEW (mülakat) olur. |
| Sahip | Odayı oluşturan kullanıcı. Her odanın tek sahibi vardır. |
| Moderatör | Sahibin, odayı yönetmesi için yetki verdiği üye. |
| Üye | Odaya katılmış kullanıcı. Sahip ve moderatörler de üyedir. |
| Açık oda / özel oda | Açık (PUBLIC) odaya her kayıtlı kullanıcı katılabilir. Özel (PRIVATE) odaya yalnız davet koduyla katılınır. |
| Davet kodu | Özel odaya katılmayı sağlayan, tahmin edilemez, isteğe bağlı süreli ve iptal edilebilir kod. |
| Etkinlik | ODA-08'deki kapalı listede sayılan işlemler. Odada yalnız bulunmak (çevrimiçi olmak) etkinlik sayılmaz. |
| Pomodoro | Odak süresi ve mola süresinden oluşan turlarla çalışma yöntemi. |
| Tur (mülakat) | Mülakatın bir yarısı. Birinci turda A mülakatçı, B aday; ikinci turda tersi. |
| Platform rolü | Hesabın sistem genelindeki rolü: USER veya ADMIN (finalde). Vizede VIEWER, EDITOR, ADMIN. |
| Oda rolü | Kullanıcının bir odadaki rolü: OWNER, MODERATOR, MEMBER. |
| DTO | İstemciyle alınıp verilen veri nesnesi. Veritabanı varlıkları dışarı açılmaz. |
| JWT | JSON Web Token; finaldeki durumsuz kimlik doğrulama yöntemi. |
| RAG | Retrieval-Augmented Generation; dil modeline ilgili belge parçalarını ekleyerek yanıt ürettirme. |
| STOMP | WebSocket üzerinde yayın/abonelik protokolü. |

### 1.5 Kaynaklar

1. `Proje/proje-onerisi.md` — proje önerisi.
2. `Proje/Proje_Teknik_Gereklilikler_Kontrol_Listesi.pdf` — vize değerlendirme kriterleri (M1–M38).
3. `Proje/YZMSEC319 - Bologna.pdf` — ders içeriği ve öğrenme çıktıları (Ö01–Ö09).
4. [Vize tasarım belgesi](superpowers/specs/2026-11-03-roommates-vize-design.md) ve [uygulama planı](superpowers/plans/2026-11-03-roommates-vize.md).
5. `Proje/w2g-analiz/RAPOR.md` — w2g.tv senkron davranışı analizi.
6. IEEE Std 830-1998, *Recommended Practice for Software Requirements Specifications*.

### 1.6 Belgenin düzeni

Bölüm 2 ürünü genel hatlarıyla anlatır. Bölüm 3 işlevsel gereksinimleri modül modül sıralar. Bölüm 4 dış arayüzleri ve API sözleşmesini, Bölüm 5 veri gereksinimlerini, Bölüm 6 kalite gereksinimlerini verir. Bölüm 7 kullanım senaryolarını, Bölüm 8 izlenebilirliği, Bölüm 9 açık konuları içerir.

---

## 2. Genel tanım

### 2.1 Ürün perspektifi

RoomMates, w2g.tv'deki "oda aç, linki paylaş, birlikte izle" fikrinden çıktı ve bunu ders çalışma ve mülakat pratiğiyle genişletir. Bağımsız bir sunucu uygulamasıdır:

```
İstemci (Swagger UI, Postman, ileride web/mobil)
      │  HTTPS + JSON (REST)        │  WebSocket + STOMP  [F]
      ▼                              ▼
RoomMates API (Spring Boot 4) ── Dil modeli sağlayıcısı  [F]
      │
      ▼
MySQL 8
```

Sunucu içinde katmanlar Controller → Service → Repository → Veritabanı sırasıyla ayrılır. İş kuralları servis katmanındadır.

### 2.2 Ürün işlevlerinin özeti

1. Hesap: kayıt, giriş, profil, parola, hesap silme, geçmiş.
2. Oda: oluşturma, listeleme ve filtreleme, ayarlar, silme, etkinliği olmayan odaların otomatik silinmesi.
3. Üyelik: katılma, ayrılma, davet kodu, moderatör atama, üye çıkarma.
4. Sohbet: oda içi mesaj yazma, düzenleme, silme, sayfalı listeleme.
5. İzleme: video kuyruğu ve sırası; finalde eş zamanlı oynatma.
6. Çalışma: pomodoro oturumu, durum geçişleri, katılımcı durumu.
7. Mülakat: iki turlu oturum, soru sağlama, karşılıklı kriterli geri bildirim.
8. Yapay zekâ destekli özellik (konusu açık, bkz. §9).
9. Yönetim: platform yöneticisi işlemleri, sağlık denetimi, API belgeleri.

### 2.3 Kullanıcı sınıfları

| Sınıf | Tanım | Yaptıkları |
|---|---|---|
| Anonim ziyaretçi | Giriş yapmamış istemci | Kayıt olur, giriş yapar, sağlık ve API belgelerine bakar. |
| Kayıtlı kullanıcı (USER) | Hesabı olan öğrenci | Oda açar, açık odalara katılır, davetle özel odalara girer, kendi hesabını yönetir. |
| Oda üyesi | Bir odaya katılmış kullanıcı | Mesaj yazar, video ekler, oturumlara katılır. |
| Moderatör | Sahibin yetki verdiği üye | Odanın içeriğini ve üyelerini yönetir. |
| Oda sahibi | Odayı oluşturan kullanıcı | Moderatörün yaptıklarına ek olarak ayarları değiştirir, moderatör atar, odayı siler. |
| Platform yöneticisi (ADMIN) | Sistemi işleten ekip | Kullanıcı hesaplarını yönetir, kurallara aykırı odayı siler, yönetim uçlarını görür. |

Hedef kitle üniversite öğrencileri, özellikle yazılım öğrencileridir. İlk kullanıcılar ekibin kendi bölümünden ve okulun öğrenci topluluklarından gelecektir.

### 2.4 İşletim ortamı

- Java 25, Spring Boot 4.1.2, Maven Wrapper.
- MySQL 8.x.
- Geliştirme: Linux, Windows ve macOS. Finalde Docker ile tek komutla çalışma.
- İstemci olarak HTTP ve WebSocket konuşabilen her araç.

### 2.5 Tasarım ve uygulama kısıtları

| # | Kısıt | Kaynak | Etiket |
|---|---|---|---|
| K1 | Spring Boot 4.1.2. | M1 | [V] |
| K2 | Vizede Spring Data JPA, Spring Data REST ve hazır repository arayüzleri kullanılmaz; repository'ler `EntityManager` ile elle yazılır. Finalde Spring Data JPA'ya geçilip geçilmeyeceği ders sorumlusuna sorulacak (Bologna Ö04 Spring Data JPA'dan söz eder). | M2, Ö04 | [V], [F] açık |
| K3 | Yalnız constructor injection; alan veya setter enjeksiyonu yok. | M7 | [V] |
| K4 | Katmanlar ayrıdır; controller veritabanına erişmez. | M3–M6 | [V] |
| K5 | Veritabanı varlıkları API'den dışarı çıkmaz; tüm giriş ve çıkış DTO ile yapılır. | Ö03 | [V] |
| K6 | Vizede HTTP Basic, finalde JWT ile durumsuz kimlik doğrulama. | M31, Ö05 | [V→F] |
| K7 | Parolalar Argon2id ile saklanır. | M33 | [V] |
| K8 | Sunucu HTML üretmez. | Bologna | [V] |
| K9 | Commit mesajları İngilizce, Conventional Commits biçiminde. | ekip kuralı | [V] |
| K10 | Vize aşamasındaki alan sınırları, DTO'lar ve durum kodları için [vize tasarımı](superpowers/specs/2026-11-03-roommates-vize-design.md) ve [uygulama planı](superpowers/plans/2026-11-03-roommates-vize.md) bağlayıcıdır. Bu belgedeki [V] kurallar onlarla aynıdır; çelişki görülürse plan geçerlidir ve bu belge düzeltilir. | ekip kuralı | [V] |

### 2.6 Varsayımlar ve bağımlılıklar

- Videolar YouTube'dan oynatılır; sunucu yalnız YouTube video kimliğini (11 karakter) ve başlığı saklar. Videonun oynatılması istemcinin işidir.
- Saat bilgisi sunucunun saatinden (UTC) alınır; istemcinin gönderdiği zamanlara güvenilmez.
- Dil modeli özelliği için dışarıdan bir sağlayıcı (veya yerel model) kullanılacaktır; seçimi açıktır (§9).
- E-posta gönderimi (doğrulama, parola sıfırlama) bu sürümde yoktur.

---

## 3. İşlevsel gereksinimler

Gereksinimler `MODÜL-NN` biçiminde numaralanır. "Sistem ... -malıdır" ifadesi zorunluluk bildirir. Her modülün sonunda iş kuralları ve hata durumları yer alır; hata kodlarının ortak biçimi §4.4'tedir. Etiketsiz iş kuralı her iki aşamada da geçerlidir.

**Genel CRUD gereksinimi (GNL-01) [V]:** Vizedeki her kaynak (kullanıcılar, odalar, video kuyruğu, çalışma oturumları, mülakat oturumları) şu uç noktaların hepsini sunmalıdır: liste (GET), tek kayıt (GET `/{id}`), oluşturma (POST), tam güncelleme (PUT), kısmi güncelleme (PATCH), silme (DELETE). Uyulacak kurallar §4.5'tedir.

### 3.1 Hesap ve kimlik doğrulama (HSP)

| No | Gereksinim | Etiket |
|---|---|---|
| HSP-01 | Sistem, kullanıcıları, parolalarını, aktiflik durumlarını ve rollerini veritabanında saklamalı ve kimlik doğrulamayı veritabanındaki bu bilgilere göre yapmalıdır. | [V] |
| HSP-02 | Platform yöneticisi `/api/users` üzerinden kullanıcı oluşturabilmeli, listeleyebilmeli (aktifliğe göre filtreli), tek kullanıcıyı getirebilmeli, güncelleyebilmeli ve silebilmelidir. Finalde de yalnız ADMIN'e açık kalır; kullanıcılar kendi hesaplarını `/api/me` ile yönetir. | [V] |
| HSP-03 | Anonim ziyaretçi kullanıcı adı, e-posta, görünen ad ve parolayla kendi hesabını açabilmelidir. Yeni hesap USER rolüyle ve aktif olarak açılır. | [F] |
| HSP-04 | Kullanıcı, kullanıcı adı ve parolasıyla giriş yapıp süreli bir JWT almalıdır. Sonraki isteklerde kimlik bu token ile doğrulanır. | [F] |
| HSP-05 | Kullanıcı kendi profilini (görünen ad, e-posta) görüntüleyip düzenleyebilmelidir. | [F] |
| HSP-06 | Kullanıcı, mevcut parolasını doğrulayarak parolasını değiştirebilmelidir. | [F] |
| HSP-07 | Kullanıcı, parolasını doğrulayarak hesabını silebilmelidir (silme sonrası veriler için HSP-10). | [F] |
| HSP-08 | Kullanıcı kendi geçmişini görebilmelidir: katıldığı mülakat oturumları, aldığı ve verdiği geri bildirimler, katıldığı çalışma oturumları ve toplam odak süresi. | [F] |
| HSP-09 | Platform yöneticisi bir hesabı devre dışı bırakabilmelidir. Devre dışı hesap doğru parolayla bile giriş yapamaz. | [V] |
| HSP-10 | Hesap silindiğinde kişisel veriler (kullanıcı adı, e-posta, görünen ad, parola hash'i) silinmeli ve kullanıcının diğer kayıtlarına aşağıdaki tabloya göre davranılmalıdır. Hepsi tek transaction içinde yapılır. | [F] |

**Hesap silinince kayıtlar (HSP-10)**

| Kayıt | Ne olur |
|---|---|
| Sahibi olduğu odalar | Oda ODA-07'ye göre silinir (A2 açık) |
| Başka odalardaki üyelik ve moderatörlük | Silinir |
| Yazdığı mesajlar | Kalır; yazar "Silinmiş kullanıcı" olarak gösterilir |
| Eklediği videolar | Kalır; ekleyen boşaltılır |
| Ürettiği davet kodları | İptal edilir |
| Çalışma katılımları | Silinir |
| Mülakatçı veya aday olduğu oturumlar | Kalır; ilgili kişi "Silinmiş kullanıcı" olur |
| Verdiği ve aldığı geri bildirimler | Kalır; silinen tarafın kimliği boşaltılır |

**İş kuralları**
- [V] Kullanıcı adı en fazla 50, e-posta ve görünen ad en fazla 100 karakterdir. [F] Kayıtta kullanıcı adı ayrıca en az 3 karakter olur ve yalnız harf, rakam, `.`, `_`, `-` içerir.
- Kullanıcı adı ve e-posta benzersizdir; çakışma 409 döner.
- Parola 8–72 karakterdir. Parola hiçbir API cevabında ve hiçbir log kaydında görünmez.
- [F] Parola değiştirme veya hesap silmede mevcut parola yanlışsa 400 döner (401 değil; istemcinin oturumu düşmesin diye).
- [F] JWT'nin geçerlilik süresi yapılandırılabilir; varsayılan 60 dakikadır.
- [F] Devre dışı bırakılan hesabın mevcut token'ı da sonraki istekte reddedilir (401).
- [V] Bir kullanıcının odası veya oturumu varsa silinmesi 409 döner. Finalde HSP-10 geçerlidir.

### 3.2 Odalar (ODA)

| No | Gereksinim | Etiket |
|---|---|---|
| ODA-01 | Kullanıcı ad, açıklama, tip (WATCH, STUDY, INTERVIEW), görünürlük (PUBLIC, PRIVATE) ve en fazla üye sayısı vererek oda oluşturabilmelidir. Odanın sahibi isteği yapan kullanıcıdır; gövdeden alınmaz. Vizede isteğe bağlı bir kapanış zamanı (`closesAt`, gelecekte olmalı) da alınır; finalde bu alan kaldırılır, yerini ODA-08 ve ODA-09 alır. | [V] |
| ODA-02 | Sistem odaları listeleyebilmeli; tip ve görünürlüğe göre filtreleyebilmelidir. | [V] |
| ODA-03 | Oda listesi sayfalı ve sıralanabilir olmalıdır (`page`, `size`, `sort`). Ada göre arama yapılabilmelidir. | [F] |
| ODA-04 | Liste, kullanıcıya yalnız açık odaları ve üyesi olduğu özel odaları göstermelidir. | [F] |
| ODA-05 | Tek bir odanın ayrıntısı id ile getirilebilmelidir. **Vizede:** her kimliği doğrulanmış kullanıcı görür. **Finalde:** özel odanın ayrıntısını yalnız üyeleri görür. | [V→F] |
| ODA-06 | Odanın adı, açıklaması, görünürlüğü ve en fazla üye sayısı tamamen (PUT) veya kısmen (PATCH) güncellenebilmelidir. **Vizede:** EDITOR/ADMIN günceller; tip de değiştirilebilir. **Finalde:** yalnız sahip günceller; tip oluşturulduktan sonra değiştirilemez (400). | [V→F] |
| ODA-07 | Oda silinebilmelidir. Silme; odanın üyeliklerini, davetlerini, mesajlarını, video kuyruğunu, çalışma ve mülakat oturumlarını tek bir transaction içinde siler. Herhangi bir adım başarısız olursa hiçbir şey silinmez. Kullanıcı geçmişine giren kayıtlar için ODA-11 geçerlidir. **Vizede:** silen ADMIN'dir; silinenler video kuyruğu ve oturumlardır. **Finalde:** sahip veya ADMIN siler. | [V→F] |
| ODA-08 | Sistem her odanın son etkinlik zamanını tutmalıdır. Etkinlik sayılan işlemler: üyenin katılması, mesaj yazılması veya düzenlenmesi, video eklenmesi veya sıranın değişmesi, oynatma olayı (oynat, duraklat, atla), oturum oluşturulması veya durum değişikliği, geri bildirim bırakılması. | [F] |
| ODA-09 | Son etkinliğinin üzerinden yapılandırılabilir süre (varsayılan 30 gün) geçmiş odalar sistem tarafından günde bir kez çalışan bir görevle, ODA-07'deki gibi silinmelidir. Silinen oda sayısı log'a yazılır. | [F] |
| ODA-10 | Platform yöneticisi herhangi bir odayı silebilmelidir. | [F] |
| ODA-11 | Oda silindiğinde kullanıcı geçmişi (HSP-08) korunmalıdır: tamamlanmış mülakat oturumları, geri bildirimleri ve çalışma katılımlarının odak süresi silinmez; yalnız odayla bağlantıları boşaltılır ve oda adı kayıtta metin olarak kalır (A8). | [F] |

**İş kuralları**
- Oda adı boş olamaz ve en fazla 100, açıklama en fazla 500 karakterdir. [F] Oda adı ayrıca en az 3 karakterdir.
- En fazla üye sayısı 2–50 arasıdır. [F] Mülakat odası için 2'dir ve değiştirilemez; mevcut üye sayısının altına düşürülemez (400).
- [F] Oda PRIVATE'tan PUBLIC'e geçince açık davet kodları iptal edilir; PUBLIC'ten PRIVATE'a geçince mevcut üyeler kalır.
- Vizede oda içi roller yoktur. Sahiplik yalnız kaydedilir; güncelleme ve silme yetkisi platform rolüne bağlıdır (§3.10). Finalde yetki sahiplik ve oda rolüne geçer.

### 3.3 Üyelik ve davet (UYE)

| No | Gereksinim | Etiket |
|---|---|---|
| UYE-01 | Oda oluşturulduğunda sahip, OWNER rolüyle otomatik olarak üye olmalıdır. | [F] |
| UYE-02 | Kullanıcı açık bir odaya serbestçe katılabilmelidir. Katılma, odanın üyeler kaynağına kayıt eklemektir (`POST /api/rooms/{roomId}/members`). | [F] |
| UYE-03 | Sahip veya moderatör özel oda için davet kodu üretebilmelidir. Kodun son kullanma zamanı isteğe bağlıdır. | [F] |
| UYE-04 | Kullanıcı geçerli bir davet koduyla özel odaya katılabilmelidir; kod katılma isteğinin gövdesinde (`inviteCode`) gönderilir. Kodsuz, süresi geçmiş veya iptal edilmiş kod reddedilir. | [F] |
| UYE-05 | Sahip veya moderatör odanın davet kodlarını listeleyebilmeli ve iptal edebilmelidir. | [F] |
| UYE-06 | Üyeler odanın üye listesini rolleriyle birlikte görebilmelidir. | [F] |
| UYE-07 | Üye odadan ayrılabilmelidir. Sahip ayrılamaz; odayı silmesi gerekir. | [F] |
| UYE-08 | Sahip bir üyeyi moderatör yapabilmeli ve moderatörlüğü geri alabilmelidir. | [F] |
| UYE-09 | Sahip veya moderatör bir üyeyi odadan çıkarabilmelidir. Moderatör sahibi veya başka bir moderatörü çıkaramaz. | [F] |

**İş kuralları**
- Oda dolduysa (üye sayısı = en fazla üye sayısı) katılım 409 döner.
- Aynı kullanıcı bir odaya iki kez üye olamaz; zaten üyeyse katılım isteği mevcut üyeliği döndürür (idempotent).
- Davet kodu en az 128 bit rastgelelikle üretilir ve tahmin edilemez. Kod URL'de taşınabilir biçimdedir.
- Süresi geçmiş veya iptal edilmiş kodla katılma 400, kodsuz özel odaya katılma 403 döner.
- Odadan çıkarılan kullanıcı o odaya bir daha katılamaz (403); sahip veya moderatör bu engeli kaldırabilir. Kendi isteğiyle ayrılan kullanıcı yeniden katılabilir.

### 3.4 Sohbet (MSJ)

| No | Gereksinim | Etiket |
|---|---|---|
| MSJ-01 | Üye, odaya metin mesajı yazabilmelidir. | [F] |
| MSJ-02 | Üyeler odanın mesajlarını en yeniden eskiye, sayfalı olarak listeleyebilmelidir. | [F] |
| MSJ-03 | Mesajın yazarı mesajını düzenleyebilmelidir. Düzenlenen mesaj düzenlenme zamanını taşır. | [F] |
| MSJ-04 | Mesajın yazarı, odanın moderatörleri ve sahibi mesajı silebilmelidir. | [F] |
| MSJ-05 | Mesajlar oda silinene kadar saklanmalıdır. | [F] |
| MSJ-06 | Yeni, düzenlenen ve silinen mesajlar odadaki bağlı istemcilere anında iletilmelidir (§3.8). | [F] |

**İş kuralları**
- Mesaj 1–1000 karakterdir; yalnız boşluktan oluşamaz.
- Üye olmayan kullanıcı mesaj yazamaz ve okuyamaz (403).

### 3.5 İzleme odası ve video kuyruğu (VID)

| No | Gereksinim | Etiket |
|---|---|---|
| VID-01 | İzleme odasının kuyruğuna YouTube video kimliği ve başlıkla video eklenebilmelidir. Ekleyen kişi isteği yapan kullanıcıdır. **Vizede:** EDITOR/ADMIN ekler. **Finalde:** odanın her üyesi ekler. | [V→F] |
| VID-02 | Kuyruk, sıra numarasına göre listelenebilmelidir. | [V] |
| VID-03 | Sıra numarası verilmezse video kuyruğun sonuna eklenmelidir. | [V] |
| VID-04 | Bir videonun sırası değiştirilebilmeli; aradaki videolar kaydırılmalıdır. | [V] |
| VID-05 | Video silindiğinde sonraki videoların sırası bir yukarı kaymalıdır. | [V] |
| VID-06 | Kendi eklediği videoyu üye, her videoyu moderatör ve sahip silebilmelidir. Sıra değiştirme yalnız moderatör ve sahipte olmalıdır. | [F] |
| VID-07 | Video yalnız WATCH tipindeki odaya eklenebilmelidir; değilse 400. | [V] |
| VID-08 | Sistem odanın oynatma durumunu (çalan video, oynuyor/duraklatıldı, konum ve bu konumun ölçüldüğü sunucu zamanı) tutmalı ve değişiklikleri bağlı istemcilere yayınlamalıdır (§3.8). | [F] |

**İş kuralları**
- Sıra numarası 1'den başlar ve boşluksuzdur. Eklemede geçerli sıra 1 ile kuyruk boyu + 1 arası, taşımada 1 ile kuyruk boyu arasıdır; dışındaki değer 400 döner. Kaydırma işlemleri tek transaction içindedir.
- YouTube video kimliği 11 karakterdir (`[A-Za-z0-9_-]`). Başlık 1–200 karakterdir.
- Başka odaya ait bir video, bu odanın yolu üzerinden istenirse 404 döner.

### 3.6 Çalışma odası (CAL)

| No | Gereksinim | Etiket |
|---|---|---|
| CAL-01 | Çalışma odasında başlık, odak süresi, mola süresi ve tur sayısıyla çalışma oturumu oluşturulabilmelidir. Tur sayısı zorunludur. | [V] |
| CAL-02 | Odak ve mola süresi gönderilmezse sistem yapılandırmadaki varsayılanları (25 ve 5 dakika) kullanmalıdır. | [V] |
| CAL-03 | Oturum durumu yalnız PLANNED → RUNNING → FINISHED yönünde ilerlemelidir. Geri dönüş ve atlama reddedilir. | [V] |
| CAL-04 | Oturum RUNNING'e geçtiğinde başlangıç zamanı sunucu saatiyle yazılmalıdır. | [V] |
| CAL-05 | Oturumlar oda ve duruma göre filtrelenerek listelenebilmelidir. | [V] |
| CAL-06 | Sistem çalışan oturumları bellekte izlemeli, yeniden başlatmada veritabanından geri yüklemelidir. | [V] |
| CAL-07 | Oda üyesi bir oturuma katılabilmeli ve ayrılabilmelidir. | [F] |
| CAL-08 | Oturumun katılımcıları listelenebilmelidir. Her katılımcı için durum ACTIVE (oturumda) veya LEFT (ayrıldı) olarak döner. Çalışan oturum için yanıt ayrıca oturumun o anki evresini (FOCUS/BREAK), kaçıncı turda olunduğunu ve evrenin bitmesine kalan süreyi içermelidir. Evre oturumun tamamı için ortaktır; ACTIVE katılımcılar o an bu evrededir. | [F] |
| CAL-09 | Son turun odak süresi bittiğinde oturum otomatik olarak FINISHED olmalıdır. | [F] |
| CAL-10 | Oturumu yalnız moderatör veya sahip başlatıp bitirebilmelidir. | [F] |
| CAL-11 | Evre değişimleri ve katılım değişiklikleri bağlı istemcilere yayınlanmalıdır (§3.8). | [F] |
| CAL-12 | Kullanıcının toplam odak süresi hesaplanıp geçmişte gösterilmelidir (HSP-08). Toplam odak süresi, kullanıcının ACTIVE olduğu aralıklarla oturumların FOCUS evrelerinin kesişiminin dakika cinsinden toplamıdır; yarım kalan evrenin geçen kısmı da sayılır. | [F] |

**İş kuralları**
- Başlık boş olamaz ve en fazla 100 karakterdir. Odak süresi 1–180, mola süresi 1–60 dakika, tur sayısı 1–12'dir.
- Oturum yalnız STUDY tipindeki odada oluşturulur; değilse 400.
- [F] Bir odada aynı anda en fazla bir RUNNING oturum olur; ikincisini başlatma 409 döner. Bir kullanıcı aynı anda en fazla bir oturumda ACTIVE olur; yeni oturuma katılınca eskisinden ayrılmış sayılır.
- [F] Ayrılıp yeniden katılan kullanıcı ACTIVE olur; aralıkları ayrı ayrı sayılır.
- Evre ve kalan süre istemciden alınmaz; başlangıç zamanı, süreler ve sunucu saatinden hesaplanır.
- FINISHED oturuma katılım 400 döner.

### 3.7 Mülakat odası (MUL)

| No | Gereksinim | Etiket |
|---|---|---|
| MUL-01 | Mülakat odasında konu, kategori (TECHNICAL, BEHAVIORAL), planlanan zaman, mülakatçı ve aday belirtilerek oturum oluşturulabilmelidir. | [V] |
| MUL-02 | Sistem oturum için kategoriye uygun bir soru sağlamalıdır. Teknik ve davranışsal sorular ayrı kaynaklardan gelir. | [V] |
| MUL-03 | Oturumlar duruma göre filtrelenerek listelenebilmelidir. | [V] |
| MUL-04 | Oturum iki turdan oluşmalıdır. Birinci turda belirlenen mülakatçı soru sorar; birinci tur bitince roller yer değiştirir ve ikinci tur başlar. | [F] |
| MUL-05 | Sistem her tur için ayrı soru sağlamalıdır. | [F] |
| MUL-06 | Oturum durumu SCHEDULED → ROUND_1 → ROUND_2 → COMPLETED yönünde ilerlemelidir; geri dönüş ve atlama reddedilir. Tur geçişini o turun mülakatçısı yapar. | [F] |
| MUL-07 | Oturum COMPLETED olduktan sonra her katılımcı diğerine bir geri bildirim bırakabilmelidir: teknik bilgi, iletişim ve problem çözme için 1–5 arası puan ve serbest yorum. | [F] |
| MUL-08 | Bir geri bildirimi yalnız onu veren ve alan kişi görebilmelidir. | [F] |
| MUL-09 | Mülakatçı ve aday odanın üyesi olmalıdır. Mülakat odasının iki üyesinden her biri oturum oluşturabilir. | [F] |

**İş kuralları**
- Oturum yalnız INTERVIEW tipindeki odada oluşturulur; değilse 400.
- Mülakatçı ile aday aynı kişi olamaz (400). Kullanıcılardan biri yoksa 404.
- Konu boş olamaz ve en fazla 100, soru en fazla 500 karakterdir.
- [F] Bir katılımcı aynı oturum için en fazla bir geri bildirim bırakır; ikincisi 409 döner. Geri bildirimi yalnız onu yazan kişi, gönderdikten sonraki 24 saat içinde düzenleyebilir.
- [F] Yorum en fazla 2000 karakterdir.
- Vizede oturum tek turludur ve durumlar SCHEDULED, IN_PROGRESS, COMPLETED'dir. Finalde MUL-06'daki durumlara geçilir.

### 3.8 Gerçek zamanlı senkron (ZMN)

| No | Gereksinim | Etiket |
|---|---|---|
| ZMN-01 | Sistem, kimliği doğrulanmış istemcilerin WebSocket (STOMP) ile bağlanıp üyesi oldukları odaların kanallarına abone olmasına izin vermelidir. Üye olmayanın aboneliği reddedilir. | [F] |
| ZMN-02 | Odanın durumu sunucuda tutulmalıdır (sunucu otoritesi). İstemciler olay gönderir; sunucu yetkiyi denetler, durumu günceller ve sonucu tüm abonelere yayınlar. | [F] |
| ZMN-03 | İzleme odasında oynat, duraklat ve konuma atla olayları yalnız moderatör ve sahipten kabul edilmelidir. | [F] |
| ZMN-04 | Yayınlanan oynatma durumu, istemcinin kendi konumunu hesaplayabilmesi için sunucu zaman damgası taşımalıdır. | [F] |
| ZMN-05 | Yeni bağlanan istemci, abone olduğu anda odanın güncel durumunu almalıdır. | [F] |
| ZMN-06 | REST ile yapılan değişiklikler (mesaj, video kuyruğu, oturum durumu, üyelik) ilgili kanala da yayınlanmalıdır. REST veri yazar, WebSocket haber verir. | [F] |
| ZMN-07 | Sistem odadaki çevrimiçi üyeleri izleyip listeleyebilmelidir. Çevrimiçi üye, o odanın kanalına açık WebSocket aboneliği olan üyedir. Liste üye listesinde (UYE-06) `online` alanıyla döner; giriş ve çıkışlar kanala yayınlanır. | [F] |

### 3.9 Yapay zekâ destekli özellik (YZ)

Özelliğin konusu henüz seçilmedi (§9, A1). Seçilen özellik ne olursa olsun şu gereksinimleri karşılamalıdır:

| No | Gereksinim | Etiket |
|---|---|---|
| YZ-01 | Özellik, Spring AI ChatClient kullanan bir REST uç noktası olarak sunulmalıdır. | [F] |
| YZ-02 | Yanıt, kendi belge deposundan bulunan parçalara dayanmalı (RAG) ve kullandığı kaynakları yanıtta belirtmelidir. | [F] |
| YZ-03 | Yanıtlar yapılandırılmış (şemalı JSON) olarak dönmelidir. | [F] |
| YZ-04 | Kaynak desteği, önceden hazırlanmış bir test soru setiyle ölçülmeli ve sonuç raporlanmalıdır. | [F] |
| YZ-05 | Kullanıcı girdisi sistem talimatından ayrılmalı; istem enjeksiyonuna karşı önlem alınmalı ve test edilmelidir. | [F] |
| YZ-06 | Dil modeline kişisel veri (e-posta, kullanıcı adı) gönderilmemelidir. | [F] |
| YZ-07 | Kullanıcı başına günlük istek sınırı olmalı (yapılandırılabilir, varsayılan 20); sınır aşılırsa 429 dönmelidir. | [F] |
| YZ-09 | Her dil modeli çağrısının süresi ve kullanılan token sayısı log'a yazılmalı; ortalama gecikme ve tahmini maliyet final raporunda verilmelidir. | [F] |
| YZ-08 | Dil modeli sağlayıcısı yanıt vermezse veya zaman aşımına uğrarsa uç nokta anlaşılır bir hata (503) dönmeli, uygulamanın geri kalanı etkilenmemelidir. | [F] |

Aday konular: mülakat sorusu üretme, mülakat geri bildirimlerini özetleme, çalışma notlarından soru-cevap, video özeti.

### 3.10 Yetkilendirme (YTK)

| No | Gereksinim | Etiket |
|---|---|---|
| YTK-01 | Kimliği doğrulanmamış veya doğrulaması başarısız istek 401, yetkisi olmayan istek 403 almalıdır. Her ikisinin gövdesi ortak hata modelindedir. | [V] |
| YTK-02 | Vizede yetki HTTP metodu ve yola göre platform rolüyle verilmelidir (tablo aşağıda). PATCH, güncelleme yetkisi ister. | [V] |
| YTK-03 | Finalde platform rolleri USER ve ADMIN'e indirgenmeli; oda içindeki yetki oda rolüne (OWNER, MODERATOR, MEMBER) ve kayıt sahipliğine göre servis katmanında denetlenmelidir. | [F] |
| YTK-04 | Yönetim bilgisi veren Actuator uçları yalnız ADMIN'e açık olmalıdır; `health` ve `info` herkese açıktır. | [V] |
| YTK-05 | Tarayıcı istemcileri için izin verilen kaynaklar (CORS) yapılandırmadan okunmalıdır. | [F] |
| YTK-06 | CSRF koruması kapalıdır. Gerekçe: vizede HTTP Basic, finalde `Authorization` başlığında Bearer token kullanılır; kimlik çerezle taşınmadığı için tarayıcı isteği kendiliğinden kimlik taşımaz. | [V] |
| YTK-07 | Oda içi yetki denetimleri servis metotlarında, metot güvenliği (`@PreAuthorize`) veya açık sahiplik kontrolüyle yapılmalıdır. | [F] |

**Vize yetki matrisi**

| İstek | VIEWER | EDITOR | ADMIN | Anonim |
|---|---|---|---|---|
| `GET /api/**` (users hariç) | ✓ | ✓ | ✓ | 401 |
| `POST`, `PUT`, `PATCH /api/**` (users hariç) | 403 | ✓ | ✓ | 401 |
| `DELETE /api/**` (users hariç) | 403 | 403 | ✓ | 401 |
| `/api/users/**` | 403 | 403 | ✓ | 401 |
| `/actuator/health`, `/actuator/info`, Swagger | ✓ | ✓ | ✓ | ✓ |
| Diğer `/actuator/**` | 403 | 403 | ✓ | 401 |
| `/error` | ✓ | ✓ | ✓ | ✓ |
| Diğer her yol | reddedilir | | | |

**Final oda yetki matrisi**

| İşlem | Üye olmayan | MEMBER | MODERATOR | OWNER |
|---|---|---|---|---|
| Açık odayı görme, katılma | ✓ | ✓ | ✓ | ✓ |
| Özel odayı görme | 403 | ✓ | ✓ | ✓ |
| Üye listesini görme, odadan ayrılma | 403 | ✓ | ✓ | ✓ (ayrılamaz) |
| Mesaj okuma/yazma, video ekleme, oynatma durumunu okuma | 403 | ✓ | ✓ | ✓ |
| Kendi mesajını düzenleme/silme, kendi videosunu silme | — | ✓ | ✓ | ✓ |
| Başkasının mesajını/videosunu silme, sırayı değiştirme | 403 | 403 | ✓ | ✓ |
| Oynatma kontrolü (oynat, duraklat, atla) | 403 | 403 | ✓ | ✓ |
| Çalışma oturumu: görme, katılımcıları görme, katılma/ayrılma | 403 | ✓ | ✓ | ✓ |
| Çalışma oturumu: oluşturma, güncelleme, başlatma, bitirme, silme | 403 | 403 | ✓ | ✓ |
| Mülakat oturumu: oluşturma, görme | 403 | ✓ | ✓ | ✓ |
| Mülakat oturumu: tur geçişi | 403 | yalnız o turun mülakatçısı | | |
| Mülakat oturumu: güncelleme, silme | 403 | oturumun iki katılımcısı | | |
| Geri bildirim yazma, okuma, düzenleme | 403 | MUL-07, MUL-08 ve iş kurallarına göre | | |
| Davet kodu üretme, listeleme, iptal; üye çıkarma | 403 | 403 | ✓ | ✓ |
| Moderatör atama, oda ayarları | 403 | 403 | 403 | ✓ |
| Odayı silme | 403 | 403 | 403 | ✓ |

Mülakat odasında yalnız iki üye olduğundan mülakat satırları oda rolüne değil, oturumdaki role bakar. "—" o işlemin o kullanıcı için anlamlı olmadığını gösterir.

**Platform yöneticisi (ADMIN) [F]:** Oda içeriğine karışmaz; mesaj, video ve oturumları yönetmez, özel odaların içeriğini görmez. Yapabildikleri: `/api/users` ile kullanıcı yönetimi (HSP-02), hesabı devre dışı bırakma (HSP-09), herhangi bir odayı silme (ODA-10), yönetim Actuator uçları (YTK-04).

### 3.11 Platform ve işletim (PLT)

| No | Gereksinim | Etiket |
|---|---|---|
| PLT-01 | Sistem sağlık durumunu ve uygulama bilgisini (ad, açıklama, sürüm) Actuator ile sunmalıdır. | [V] |
| PLT-02 | API, OpenAPI ile belgelenmeli ve Swagger UI'dan denenebilmelidir; tüm uç noktalar, istek ve cevap modelleri ve hata cevapları görünmelidir. | [V] |
| PLT-03 | Veriler MySQL'de kalıcı olmalı, uygulama yeniden başlatıldığında korunmalıdır. | [V] |
| PLT-04 | Bağlantı bilgileri ve sırlar kaynak koddan değil ortam değişkenlerinden okunmalıdır. Tek istisna, yalnız yerel geliştirme için olan örnek kullanıcı parolalarıdır: `application.properties`'te varsayılan olarak durur ve ortam değişkeniyle ezilebilir. Canlı profilde bu varsayılanlar kullanılmaz. | [V] |
| PLT-05 | Geliştirme ve canlı ortam için ayrı yapılandırma profilleri olmalıdır. | [F] |
| PLT-06 | Uygulama ve veritabanı Docker Compose ile tek komutla ayağa kalkabilmelidir. | [F] |
| PLT-07 | Uygulama yapılandırılmış log yazmalı; parola, token ve kişisel veri log'a yazılmamalıdır. | [F] |
| PLT-08 | Çalıştırma rehberi (gerekli yazılımlar, veritabanı kurulumu, başlatma, adresler, örnek kullanıcılar ve rolleri) teslim edilmelidir. | [V] |
| PLT-09 | Postman koleksiyonu teslim edilmelidir. Koleksiyon tüm CRUD isteklerini ve 400, 404, 401, 403 senaryolarını içerir; `baseUrl` ve kimlik bilgileri değişkendir. | [V] |
| PLT-10 | Final tesliminde bir değerlendirme raporu verilmelidir: test sonuçları ve bulguları, bilinen riskler, sürüm ve bağımlılık kararları, kaynak tüketimi (bellek, veritabanı boyutu, dil modeli maliyeti) ve YZ sınırlılıkları. | [F] |

---

## 4. Dış arayüz gereksinimleri

### 4.1 Kullanıcı arayüzü

Sistemin kendi kullanıcı arayüzü yoktur. API, Swagger UI (`/swagger-ui.html`) ve Postman koleksiyonu ile kullanılır.

### 4.2 Yazılım arayüzleri

| Arayüz | Kullanım | Etiket |
|---|---|---|
| MySQL 8 | Kalıcı veri, JPA/Hibernate üzerinden | [V] |
| YouTube | Yalnız video kimliği saklanır; sunucu YouTube'a istek atmaz | [V] |
| Dil modeli sağlayıcısı | Spring AI ChatClient ve embedding modeli | [F] |
| Vektör deposu | RAG için belge parçaları | [F] |

### 4.3 İletişim arayüzleri

- REST: HTTP/1.1, JSON gövdeler (`application/json`), UTF-8. Zamanlar ISO 8601, UTC.
- Kimlik: vizede `Authorization: Basic`, finalde `Authorization: Bearer <JWT>`.
- WebSocket [F]: `/ws` uç noktası, STOMP. Abonelik kanalları `/topic/rooms/{roomId}/...`, istemci olayları `/app/rooms/{roomId}/...`.

### 4.4 Ortak hata modeli

Her hata, durum kodundan bağımsız olarak aynı gövdeyle döner:

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

`fieldErrors` yalnız doğrulama hatalarında yer alır. Mesajlar Türkçedir. Kullanılan durum kodları:

| Kod | Ne zaman |
|---|---|
| 400 | Doğrulama hatası, okunamayan gövde, geçersiz parametre, iş kuralı ihlali, PATCH ile id değiştirme denemesi |
| 401 | Kimlik yok, yanlış parola, devre dışı hesap, geçersiz veya süresi dolmuş token |
| 403 | Kimlik doğru ama yetki yok |
| 404 | Kayıt veya yol yok |
| 405 / 406 / 415 | Desteklenmeyen metot, kabul türü, gövde türü |
| 409 | Benzersizlik çakışması, dolu oda, ikinci geri bildirim, silinemeyen kayıt |
| 429 | Yapay zekâ istek sınırı aşıldı [F] |
| 500 | Beklenmeyen hata (ayrıntı yalnız log'da) |
| 503 | Dil modeli sağlayıcısına ulaşılamadı [F] |

### 4.5 REST kuralları

- Kaynaklar çoğul isimle, fiilsiz adlandırılır.
- GET listeler (200), GET `/{id}` tek kaydı getirir (200/404), POST oluşturur (201 + `Location` başlığı + oluşan kayıt), PUT tamamen günceller (200), PATCH kısmen günceller (200), DELETE siler (204).
- POST gövdesinde gönderilen `id` yok sayılır ve var olan bir kaydı asla değiştirmez.
- PATCH yalnız gönderilen alanları değiştirir; gövdede `id` varsa 400 döner.
- Katılma, ayrılma, tur geçişi gibi durum değiştiren işlemler mümkünse alt kaynak oluşturma/silme veya durum alanının PATCH'i olarak modellenir.

### 4.6 Uç nokta özeti

| Kaynak | Yol | Etiket |
|---|---|---|
| Kayıt ve giriş | `POST /api/auth/register`, `POST /api/auth/login` | [F] |
| Kendi hesabım | `GET/PATCH/DELETE /api/me`, `PUT /api/me/password`, `GET /api/me/history` | [F] |
| Kullanıcılar (yönetim) | `/api/users`, `/api/users/{id}` | [V] |
| Odalar | `/api/rooms`, `/api/rooms/{id}` | [V] |
| Üyeler | `/api/rooms/{roomId}/members`, `/api/rooms/{roomId}/members/{userId}` | [F] |
| Davetler | `/api/rooms/{roomId}/invites`, `/api/rooms/{roomId}/invites/{inviteId}` (katılma `POST /api/rooms/{roomId}/members` ile, gövdede `inviteCode`) | [F] |
| Mesajlar | `/api/rooms/{roomId}/messages`, `/api/rooms/{roomId}/messages/{messageId}` | [F] |
| Video kuyruğu | `/api/rooms/{roomId}/videos`, `/api/rooms/{roomId}/videos/{videoId}` | [V] |
| Oynatma durumu | `GET /api/rooms/{roomId}/playback` + WebSocket olayları | [F] |
| Çalışma oturumları | `/api/study-sessions`, `/api/study-sessions/{id}` | [V] |
| Çalışma katılımcıları | `/api/study-sessions/{id}/participants`, `/api/study-sessions/{id}/participants/{userId}` | [F] |
| Mülakat oturumları | `/api/interview-sessions`, `/api/interview-sessions/{id}` | [V] |
| Mülakat geri bildirimi | `/api/interview-sessions/{id}/feedback`, `/api/interview-sessions/{id}/feedback/{feedbackId}` | [F] |
| Yapay zekâ | konuya göre belirlenecek (§9) | [F] |
| İşletim | `/actuator/health`, `/actuator/info`, `/v3/api-docs`, `/swagger-ui.html` | [V] |

Uç noktaların ayrıntılı şeması Swagger'da tutulur; bu tablo yalnız kaynakların sınırlarını gösterir.

---

## 5. Veri gereksinimleri

### 5.1 Varlıklar

| Varlık | Temel alanlar | İlişkiler | Etiket |
|---|---|---|---|
| User | kullanıcı adı, parola hash'i, e-posta, görünen ad, aktif mi, oluşturulma zamanı | rolleri (çoka çok) | [V] |
| Role | ad (vize: ROLE_VIEWER, ROLE_EDITOR, ROLE_ADMIN; final: ROLE_USER, ROLE_ADMIN) | — | [V→F] |
| Room | ad, açıklama, tip, görünürlük, en fazla üye, oluşturulma zamanı; vizede kapanış zamanı (`closes_at`), finalde onun yerine son etkinlik zamanı | sahip (User) | [V→F] |
| RoomBan | engellenme zamanı | oda, kullanıcı (UYE-09) | [F] |
| RoomMembership | oda rolü, katılma zamanı | oda, kullanıcı (çift benzersiz) | [F] |
| RoomInvite | kod, son kullanma zamanı, iptal edildi mi, oluşturulma zamanı | oda, oluşturan | [F] |
| Message | içerik, gönderilme zamanı, düzenlenme zamanı | oda, yazar (silinmişse boş) | [F] |
| VideoQueueItem | YouTube kimliği, başlık, sıra, eklenme zamanı | oda, ekleyen | [V] |
| PlaybackState | çalan video, oynuyor mu, konum (sn), ölçüm zamanı | oda (bire bir) | [F] |
| StudySession | başlık, odak/mola süresi, tur, durum, başlangıç zamanı | oda | [V] |
| StudyParticipant | katılma zamanı, ayrılma zamanı | oturum, kullanıcı | [F] |
| InterviewSession | konu, kategori, durum, planlanan zaman, tur soruları | oda, mülakatçı, aday | [V→F] |
| InterviewFeedback | teknik bilgi, iletişim, problem çözme puanı, yorum, oluşturulma zamanı | oturum, veren, alan | [F] |

### 5.2 Veri kuralları

- Her varlığın tablo ve sütun adları açıkça belirtilir; birincil anahtarlar veritabanında otomatik artar.
- Oda silinince ona bağlı kayıtlar silinir (ODA-07); geçmiş kayıtları ODA-11'e göre kalır. Kullanıcı silinince HSP-10 uygulanır.
- Finalde `closes_at` sütunu kaldırılır ve yerine son etkinlik zamanı eklenir; mevcut odaların son etkinlik zamanı geçiş anına ayarlanır.
- Şema değişiklikleri veriyi silmeden yapılır; tabloları her açılışta yeniden oluşturan ayarlar yasaktır.
- Tüm zamanlar UTC saklanır.

### 5.3 Saklama süreleri

| Veri | Süre |
|---|---|
| Oda ve içeriği | Sahip silene veya etkinlik olmadan geçen süre dolana kadar (yapılandırılabilir, varsayılan 30 gün) |
| Mülakat, geri bildirim ve odak süresi geçmişi | Kullanıcı hesabını silene kadar (ODA-11, HSP-10) |
| Mesajlar | Oda silinene kadar |
| Kullanıcı hesabı | Kullanıcı veya yönetici silene kadar |
| Davet kodu | Oda silinene kadar (süresi geçmiş olsa da kayıt kalır) |
| Log kayıtları | Ortama göre yapılandırılır; kişisel veri içermez |

---

## 6. Kalite gereksinimleri

### 6.1 Güvenlik (GUV)

| No | Gereksinim | Etiket |
|---|---|---|
| GUV-01 | Parolalar Argon2id ile hash'lenir; düz parola hiçbir yerde saklanmaz, loglanmaz, cevapta dönmez. | [V] |
| GUV-02 | API durumsuzdur; sunucu oturum çerezi tutulmaz. | [V] |
| GUV-03 | JWT imzalı ve süreli olur; imza anahtarı ortam değişkeninden okunur. | [F] |
| GUV-04 | Kayıt sahipliği servis katmanında denetlenir; yalnız URL'ye bakarak yetki verilmez. | [F] |
| GUV-05 | Bir kullanıcı, yetkisi olmadığı bir kaydı (özel oda, geri bildirim, başkasının hesabı) id tahmin ederek okuyamaz veya değiştiremez. | [F] |
| GUV-06 | Canlı ortamda trafik HTTPS ile taşınır. | [S] |
| GUV-07 | Giriş uç noktası kaba kuvvet denemelerine karşı sınırlandırılır. | [S] |

### 6.2 Performans (PRF)

Ölçümler geliştirme makinesinde, yerel MySQL ile, 1 dakikalık ısınmadan sonra bir yük testi aracıyla (ör. k6 veya JMeter) yapılır. Veri hacmi: 1.000 kullanıcı, 500 oda, oda başına 200 mesaj.

| No | Gereksinim | Etiket |
|---|---|---|
| PRF-01 | Tekil kayıt ve sayfalı liste isteklerinin %95'i 300 ms altında yanıtlanır (50 eşzamanlı kullanıcı, 5 dakika, dil modeli hariç). | [F] |
| PRF-02 | Sayfalı listeler (odalar, mesajlar, kullanıcılar, çalışma ve mülakat oturumları, geçmiş) tek istekte en fazla 100 kayıt döndürür; varsayılan sayfa boyu 20'dir. Video kuyruğu en fazla 200 videoyla sınırlıdır ve tek parça döner. | [F] |
| PRF-03 | WebSocket olayının aynı odadaki abonelere iletilmesi yerel ağda 500 ms'yi aşmaz. | [F] |
| PRF-04 | Bir oda en az 50 eşzamanlı WebSocket bağlantısını kaldırır. | [F] |

### 6.3 Güvenilirlik (GVN)

| No | Gereksinim | Etiket |
|---|---|---|
| GVN-01 | Birden fazla kaydı değiştiren işlemler transaction içindedir; yarıda kalan işlem hiçbir değişiklik bırakmaz. | [V] |
| GVN-02 | Beklenmeyen hatalar 500 ve ortak hata gövdesiyle döner; istemciye yığın izi gösterilmez. | [V] |
| GVN-03 | Uygulama yeniden başlatıldığında çalışan çalışma oturumları ve odaların oynatma durumu kaybolmaz. | [V→F] |
| GVN-04 | Dış servisin (dil modeli) hatası yalnız ilgili uç noktayı etkiler. | [F] |

### 6.4 Sürdürülebilirlik ve test (SRD)

| No | Gereksinim | Etiket |
|---|---|---|
| SRD-01 | Kod modül bazlı paketlenir (kullanıcı, oda, video, çalışma, mülakat, ortak). | [V] |
| SRD-02 | Servis ve repository'ler arayüz arkasındadır; implementasyon değişince çağıranlar değişmez. | [V] |
| SRD-03 | Her modülün iş kuralları birim testleriyle doğrulanır (durum geçişleri, sıra kaydırma, rol denetimleri, doğrulama sınırları). | [F] |
| SRD-04 | API davranışı ve 401/403 senaryoları MockMvc entegrasyon testleriyle doğrulanır. | [F] |
| SRD-05 | Postman koleksiyonu, yalnız örnek kullanıcıların bulunduğu temiz veritabanında art arda iki kez çalıştırıldığında tamamen başarılı olur. Koleksiyon kendi oluşturduğu kayıtları siler veya benzersiz adlar üretir, örnek kullanıcıları değiştirmez. | [V] |

### 6.5 Taşınabilirlik ve işletim (TSN)

| No | Gereksinim | Etiket |
|---|---|---|
| TSN-01 | Uygulama Linux, Windows ve macOS'ta Maven Wrapper ile derlenip çalışır. | [V] |
| TSN-02 | Docker imajı ve Compose dosyası ile uygulama ve veritabanı tek komutla kalkar. | [F] |
| TSN-03 | Sağlık denetimi veritabanı bağlantısını da kapsar. | [V] |

### 6.6 Gizlilik ve etik (ETK)

| No | Gereksinim | Etiket |
|---|---|---|
| ETK-01 | Yalnız hizmet için gereken kişisel veri toplanır: kullanıcı adı, e-posta, görünen ad. | [F] |
| ETK-02 | Kullanıcı hesabını ve kişisel verisini silebilir (HSP-07, HSP-10). | [F] |
| ETK-03 | Mülakat geri bildirimleri yalnız ilgili iki katılımcıya görünür. | [F] |
| ETK-04 | Yapay zekâ özelliği için gizlilik, maliyet, kaynak tüketimi ve kullanılan belgelerin lisansı değerlendirilip raporlanır. | [F] |
| ETK-05 | Yapay zekâ çıktısının hatalı olabileceği yanıtta belirtilir. | [F] |

---

## 7. Kullanım senaryoları

### SN-1 Birlikte video izleme [F]

1. Ayşe kayıt olur ve giriş yapar (HSP-03, HSP-04).
2. "Cuma film gecesi" adlı özel bir izleme odası açar; otomatik olarak sahip olur (ODA-01, UYE-01).
3. 24 saat geçerli bir davet kodu üretir ve kodu arkadaşlarına gönderir (UYE-03).
4. Mehmet kodla odaya katılır (UYE-04) ve kuyruğa iki video ekler (VID-01).
5. Ayşe Mehmet'i moderatör yapar (UYE-08). Mehmet ilk videoyu başlatır; sunucu oynatma durumunu herkese yayınlar (ZMN-03, ZMN-04).
6. Sonradan katılan Can bağlandığında güncel konumu alır ve videoya aynı yerden katılır (ZMN-05).
7. Odada 30 gün kimse bir şey yapmaz; sistem odayı siler (ODA-09).

**Hata yolları:** Süresi geçmiş kodla katılma → 400. Dolu oda → 409. Üye olmayanın mesaj okuması → 403.

### SN-2 Birlikte çalışma [F]

1. Zeynep açık bir çalışma odası açar ve odak ve mola süresi vermeden 4 turluk bir oturum oluşturur; süreler 25/5 dakika olur (CAL-01, CAL-02).
2. Üç kişi odaya ve oturuma katılır (UYE-02, CAL-07).
3. Zeynep oturumu başlatır (CAL-03, CAL-04, CAL-10). Katılımcılar kimlerin çalıştığını ve molaya kaç dakika kaldığını görür (CAL-08).
4. Dört tur bitince oturum kendiliğinden FINISHED olur (CAL-09). Katılımcıların toplam odak süresi geçmişlerine eklenir (CAL-12).

**Hata yolları:** FINISHED oturumu RUNNING yapma → 400. Üyenin oturumu başlatması → 403.

### SN-3 Mülakat pratiği [F]

1. Emre bir mülakat odası açar ve davet koduyla Elif'i çağırır.
2. Emre mülakatçı, Elif aday olarak teknik bir oturum oluşturur; sistem soruyu sağlar (MUL-01, MUL-02).
3. Emre birinci turu yönetir ve turu bitirir; roller yer değiştirir, ikinci tur için yeni soru gelir (MUL-04, MUL-05, MUL-06).
4. Oturum tamamlanınca ikisi de birbirine üç kriterde puan ve yorum bırakır (MUL-07).
5. Elif geçmişinde aldığı geri bildirimi görür (HSP-08, MUL-08). Oda iki ay sonra etkinliksizlikten silinse de bu geri bildirim geçmişinde kalır (ODA-11).

**Hata yolları:** Kendine mülakat → 400. İkinci geri bildirim → 409. Üçüncü kişinin geri bildirimleri okuması → 403.

### SN-4 Vize gösterimi [V]

1. `editor` kullanıcısı Basic kimlikle oda oluşturur; gövdeye `"id": 1` koysa da yeni kayıt oluşur (ODA-01, §4.5).
2. `viewer` aynı isteği yapar → 403; kimliksiz istek → 401 (YTK-01, YTK-02).
3. `admin` odayı siler; videoları ve oturumları da tek transaction'da gider (ODA-07).
4. Uygulama yeniden başlatılır; diğer veriler yerinde durur (PLT-03).

---

## 8. İzlenebilirlik

### 8.1 Bologna öğrenme çıktıları → gereksinimler

| Çıktı | Kısaca | Karşılayan gereksinimler |
|---|---|---|
| Ö01 | Gereksinim çıkarma, REST sözleşmesi | Bu belge; §4.5, §4.6; §7 |
| Ö02 | IoC, bean yönetimi, katman ayrımı | K3, K4; SRD-01, SRD-02; MUL-02 (birden fazla soru sağlayıcı); CAL-02 (yapılandırma değeri); CAL-06 (bean yaşam döngüsü) |
| Ö03 | JSON, DTO, doğrulama, tutarlı hata | K5; GNL-01; §4.4; tüm modüllerin iş kuralları |
| Ö04 | İlişkisel veri modeli, CRUD, sorgu, transaction | §5; GNL-01; ODA-02, ODA-03, ODA-07, ODA-11; VID-04, VID-05; GVN-01 |
| Ö05 | JWT, rol tabanlı yetki, kaynak sahipliği, CORS, CSRF | HSP-04; YTK-01–YTK-07; GUV-03–GUV-05; §3.10 matrisleri |
| Ö06 | Birim ve entegrasyon testleri | SRD-03, SRD-04; PLT-10 (test bulguları) |
| Ö07 | Spring AI, RAG, kaynak desteğinin ölçülmesi | YZ-01–YZ-09 |
| Ö08 | OpenAPI, ortam yapılandırması, profiller, sağlık, Docker | PLT-01, PLT-02, PLT-04–PLT-07, PLT-10; TSN-02, TSN-03 |
| Ö09 | Gizlilik, yetki, lisans, YZ etiği ve maliyeti | ETK-01–ETK-05; HSP-10; YZ-06, YZ-09; PLT-10 |

### 8.2 Vize kontrol listesi

Kontrol listesindeki 38 maddenin her biri vize tasarım belgesinin 12. bölümündeki tabloda koda ve gösterime bağlanmıştır. Bu belgedeki [V] etiketli gereksinimler o tablonun işlevsel karşılığıdır:

| Madde grubu | Gereksinimler |
|---|---|
| M1–M7 Mimari ve DI | K1–K4, SRD-01, SRD-02 |
| M8–M14 Spring çekirdeği ve Actuator | MUL-02, CAL-02, CAL-06, PLT-01, YTK-04 |
| M15–M20 Kalıcılık | PLT-03, §5, ODA-02, CAL-05, MUL-03, HSP-02, ODA-07, GVN-01 |
| M21–M27 REST | GNL-01, §4.5, §4.6 |
| M28–M30 Hata yönetimi | §4.4, GVN-02 |
| M31–M35 Güvenlik | HSP-01, HSP-09, GUV-01, YTK-01, YTK-02, YTK-06 |
| M36–M38 Belgeleme | PLT-02, PLT-08, PLT-09, SRD-05 |

---

## 9. Açık konular

| # | Konu | Durum |
|---|---|---|
| A1 | Yapay zekâ özelliğinin konusu (mülakat sorusu üretme, geri bildirim özeti, çalışma notlarından soru-cevap, video özeti) | Sonra karar verilecek. §3.9 her durumda geçerli. |
| A2 | Hesap silinince sahibi olunan odalar silinsin mi, bir moderatöre devredilsin mi | Varsayım: silinir (HSP-10). Teyit gerekiyor. |
| A3 | Finalde Spring Data JPA'ya geçiş | Ders sorumlusuna sorulacak (K2). |
| A4 | Vizedeki VIEWER/EDITOR rollerinin finalde USER'a taşınması | Geçişte mevcut kullanıcılar USER'a atanacak; ayrıntısı final tasarımında. |
| A5 | Çalışma ve mülakat oturumlarının yolları `/api/rooms/{roomId}/...` altına taşınsın mı | Vizede düz yollar kalır; finalde tutarlılık için değerlendirilecek. |
| A6 | Dil modeli sağlayıcısı (bulut veya yerel) | A1 ile birlikte seçilecek; maliyet ve gizlilik ETK-04'te değerlendirilecek. |
| A7 | Ekip kararı olmadan taslağa konan final değerleri: kayıtta kullanıcı adı ve oda adı alt sınırları, mesaj (1000) ve yorum (2000) uzunlukları, JWT süresi 60 dk, geri bildirimin 24 saat düzenlenebilmesi, YZ günlük sınırı 20, video kuyruğu sınırı 200, PRF-01–PRF-04 değerleri. [V] değerler uygulama planından alındı (K10). | Ekip gözden geçirip onaylayacak. |
| A8 | Oda silinince geçmiş kayıtlarının korunması (ODA-11) ve hesap silmedeki kayıt tablosu (HSP-10) taslağın önerisidir | Ekip onaylayacak. |
| A9 | Odadan çıkarılan üyenin kalıcı engellenmesi (UYE-09, RoomBan) taslağın önerisidir; aksi halde açık odada üye çıkarmanın etkisi olmaz | Ekip onaylayacak. |

---

## Ek A. Sürüm geçmişi

| Sürüm | Tarih | Değişiklik |
|---|---|---|
| 1.0 | 2026-11-08 | İlk taslak: proje önerisi, vize tasarımı ve ekip kararlarından (oda rolleri, davet, sohbet, iki turlu mülakat, 30 gün etkinlik kuralı, final senkron) derlendi. |
