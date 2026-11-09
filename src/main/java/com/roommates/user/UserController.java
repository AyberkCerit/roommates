package com.roommates.user;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.roommates.common.error.ErrorResponse;
import com.roommates.user.dto.UserCreateRequest;
import com.roommates.user.dto.UserPatchRequest;
import com.roommates.user.dto.UserResponse;
import com.roommates.user.dto.UserUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * KULLANICI CONTROLLER'I: /api/users adresine gelen istekleri karşılar (M4, M21).
 *
 * Görevi sadece şunlar:
 *   1) İsteği al, formu doğrula (@Valid)
 *   2) İşi servise ver
 *   3) Cevabı doğru HTTP koduyla döndür
 * Veritabanına DOKUNMAZ, iş kuralı içermez. Sadece UserService arayüzünü tanır (M3).
 *
 * Bu adreslerin hepsine sadece ADMIN girebilir (SecurityConfig'te tanımlı).
 *
 * @Operation, @ApiResponse, @Tag: Swagger belgesi için açıklamalar (M36).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Kullanıcılar", description = "Kullanıcı yönetimi (yalnız ADMIN)")
public class UserController {

    private final UserService kullaniciServisi;

    // Constructor injection (M7)
    public UserController(UserService kullaniciServisi) {
        this.kullaniciServisi = kullaniciServisi;
    }

    /**
     * GET /api/users           → bütün kullanıcılar
     * GET /api/users?enabled=false → sadece pasifler (M19 filtresi)
     */
    @GetMapping
    @Operation(summary = "Kullanıcıları listele (enabled ile filtrelenebilir)")
    @ApiResponse(responseCode = "200", description = "Liste döndü")
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<UserResponse> kullanicilariListele(@RequestParam(required = false) Boolean enabled) {
        return kullaniciServisi.findAll(enabled);
    }

    /** GET /api/users/5 → 5 numaralı kullanıcı (M22). */
    @GetMapping("/{id}")
    @Operation(summary = "Kullanıcıyı numarasıyla getir")
    @ApiResponse(responseCode = "200", description = "Kullanıcı bulundu")
    @ApiResponse(responseCode = "404", description = "Kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UserResponse kullaniciGetir(@PathVariable Long id) {
        return kullaniciServisi.findById(id);
    }

    /**
     * POST /api/users → yeni kullanıcı (M23).
     * 201 Created döner ve "Location" başlığına yeni kaydın adresini yazar.
     */
    @PostMapping
    @Operation(summary = "Yeni kullanıcı ekle")
    @ApiResponse(responseCode = "201", description = "Kullanıcı oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Kullanıcı adı veya e-posta zaten var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserResponse> kullaniciEkle(@Valid @RequestBody UserCreateRequest istek) {
        UserResponse yeniKullanici = kullaniciServisi.create(istek);

        // Yeni kaydın adresi. Örnek: http://localhost:8080/api/users/5
        URI adres = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(yeniKullanici.id())
                .toUri();

        return ResponseEntity.created(adres).body(yeniKullanici);
    }

    /** PUT /api/users/5 → tam güncelleme (M24). */
    @PutMapping("/{id}")
    @Operation(summary = "Kullanıcıyı tamamen güncelle (bütün alanlar zorunlu)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Kullanıcı adı veya e-posta zaten var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UserResponse kullaniciGuncelle(@PathVariable Long id,
                                          @Valid @RequestBody UserUpdateRequest istek) {
        return kullaniciServisi.update(id, istek);
    }

    /** PATCH /api/users/5 → kısmi güncelleme (M25). Gövdede id varsa 400. */
    @PatchMapping("/{id}")
    @Operation(summary = "Kullanıcıyı kısmen güncelle (sadece gönderilen alanlar değişir)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya gövdede id var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Kullanıcı adı veya e-posta zaten var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UserResponse kullaniciKismenGuncelle(@PathVariable Long id,
                                                @Valid @RequestBody UserPatchRequest istek) {
        return kullaniciServisi.patch(id, istek);
    }

    /** DELETE /api/users/5 → sil (M26). Başarılıysa 204 No Content (gövde yok). */
    @DeleteMapping("/{id}")
    @Operation(summary = "Kullanıcıyı sil")
    @ApiResponse(responseCode = "204", description = "Silindi")
    @ApiResponse(responseCode = "404", description = "Kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Kullanıcının odası veya oturumu var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> kullaniciSil(@PathVariable Long id) {
        kullaniciServisi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
