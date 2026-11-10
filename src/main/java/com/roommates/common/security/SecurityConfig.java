package com.roommates.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final JsonAuthenticationEntryPoint girisHatasiYazici;
    private final JsonAccessDeniedHandler yetkiHatasiYazici;

    public SecurityConfig(JsonAuthenticationEntryPoint girisHatasiYazici,
                          JsonAccessDeniedHandler yetkiHatasiYazici) {
        this.girisHatasiYazici = girisHatasiYazici;
        this.yetkiHatasiYazici = yetkiHatasiYazici;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.formLogin(form -> form.disable());

        http.sessionManagement(oturum -> oturum.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.httpBasic(basic -> basic.authenticationEntryPoint(girisHatasiYazici));
        http.exceptionHandling(hata -> hata
                .authenticationEntryPoint(girisHatasiYazici)
                .accessDeniedHandler(yetkiHatasiYazici));

        http.authorizeHttpRequests(kurallar -> kurallar

                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()

                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")

                .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("ROLE_ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("ROLE_EDITOR")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAuthority("ROLE_EDITOR")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasAuthority("ROLE_EDITOR")

                .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("ROLE_VIEWER")

                .anyRequest().denyAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
