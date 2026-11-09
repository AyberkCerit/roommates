package com.roommates.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * GÜVENLİK AYARLARI: API'nin "kapı görevlisi" (M31, M34, M14)
 *
 * Her istek controller'a ulaşmadan önce buradaki kurallardan geçer:
 *   1) Kim bu? (kullanıcı adı + parola, HTTP Basic)
 *   2) Bu kişinin bu işi yapmaya yetkisi var mı? (rol kontrolü)
 *
 * YETKİ TABLOSU:
 *   İstek                          | VIEWER | EDITOR | ADMIN | Giriş yok
 *   GET /api/**                    |   ✓    |   ✓    |   ✓   |   401
 *   POST, PUT, PATCH /api/**       |  403   |   ✓    |   ✓   |   401
 *   DELETE /api/**                 |  403   |  403   |   ✓   |   401
 *   /api/users/** (hepsi)          |  403   |  403   |   ✓   |   401
 *   /actuator/health, /info        |   ✓    |   ✓    |   ✓   |    ✓
 *   diğer /actuator/** (metrics..) |  403   |  403   |   ✓   |   401
 *   Swagger                        |   ✓    |   ✓    |   ✓   |    ✓
 *
 * Not: "x -> x.ayar()" yazımı (lambda) Spring Security'nin zorunlu yazım biçimi.
 *      Okunuşu: "x ayarını şöyle yap".
 */
@Configuration
public class SecurityConfig {

    private final JsonAuthenticationEntryPoint girisHatasiYazici;   // 401 cevabını yazar
    private final JsonAccessDeniedHandler yetkiHatasiYazici;        // 403 cevabını yazar

    // Constructor injection (M7)
    public SecurityConfig(JsonAuthenticationEntryPoint girisHatasiYazici,
                          JsonAccessDeniedHandler yetkiHatasiYazici) {
        this.girisHatasiYazici = girisHatasiYazici;
        this.yetkiHatasiYazici = yetkiHatasiYazici;
    }

    /**
     * Güvenlik kurallarının tamamı (M31). @Bean: Spring bu nesneyi oluşturup kullanır (M8).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // ----- Genel ayarlar -----

        // CSRF korumasını kapat: tarayıcı çerezi kullanan formlar için gerekir,
        // bizim API'miz her istekte kullanıcı adı + parola aldığı için gerekmez.
        http.csrf(csrf -> csrf.disable());

        // Giriş formu sayfası yok, sadece HTTP Basic var.
        http.formLogin(form -> form.disable());

        // Oturum (session) tutma: her istek kendi kullanıcı adı + parolasını getirir.
        http.sessionManagement(oturum -> oturum.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // ----- 401 ve 403 cevapları -----
        // 401 yazıcısını İKİ yere kaydediyoruz:
        //   1) httpBasic içinde → parola YANLIŞ olduğunda
        //   2) exceptionHandling içinde → hiç kullanıcı adı / parola GÖNDERİLMEDİĞİNDE
        // Birini unutursak o durumda JSON'suz, boş bir 401 döner.
        http.httpBasic(basic -> basic.authenticationEntryPoint(girisHatasiYazici));
        http.exceptionHandling(hata -> hata
                .authenticationEntryPoint(girisHatasiYazici)
                .accessDeniedHandler(yetkiHatasiYazici));

        // ----- Yetki kuralları (M34) -----
        // SIRA ÖNEMLİ: Spring kuralları yukarıdan aşağı okur, ilk uyan kural geçerli olur.
        // Bu yüzden özel kurallar (/api/users) genel kurallardan (/api/**) önce yazılır.
        http.authorizeHttpRequests(kurallar -> kurallar

                // 1) Herkese açık: hata sayfası, Swagger, sağlık ve bilgi
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()

                // 2) Diğer yönetim adresleri (metrics vb.) sadece ADMIN (M14)
                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")

                // 3) Kullanıcı yönetimi sadece ADMIN
                .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")

                // 4) Silme sadece ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("ROLE_ADMIN")

                // 5) Ekleme ve güncelleme EDITOR (PATCH de güncelleme sayılır)
                .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("ROLE_EDITOR")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAuthority("ROLE_EDITOR")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasAuthority("ROLE_EDITOR")

                // 6) Okuma VIEWER
                .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("ROLE_VIEWER")

                // 7) Listede olmayan her şey yasak
                .anyRequest().denyAll());

        return http.build();
    }

    /**
     * Parola şifreleyici: Argon2id (M33).
     *
     * Kullanıcı kaydedilirken parola bununla şifrelenir: "Parola.123" → "$argon2id$v=19$..."
     * Giriş sırasında Spring, gelen parolayı bununla veritabanındakiyle karşılaştırır.
     * Şifreli parola geri çözülemez; veritabanını gören biri bile parolayı öğrenemez.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
