package com.roommates.room;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import com.roommates.room.dto.RoomCreateRequest;
import com.roommates.room.dto.RoomPatchRequest;
import com.roommates.room.dto.RoomResponse;
import com.roommates.room.dto.RoomUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Odalar", description = "İzleme, çalışma ve mülakat odaları")
public class RoomController {
    private final RoomService odaServisi;

    public RoomController(RoomService odaServisi) {
        this.odaServisi = odaServisi;
    }

    @GetMapping
    @Operation(summary = "Odaları listele (type ve visibility ile filtrelenebilir)")
    @ApiResponse(responseCode = "200", description = "Liste döndü")
    @ApiResponse(responseCode = "400", description = "Geçersiz filtre değeri",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<RoomResponse> odalariListele(@RequestParam(required = false) RoomType type,
                                             @RequestParam(required = false) RoomVisibility visibility) {
        return odaServisi.findAll(type, visibility);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Odayı numarasıyla getir")
    @ApiResponse(responseCode = "200", description = "Oda bulundu")
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public RoomResponse odaGetir(@PathVariable Long id) {
        return odaServisi.findById(id);
    }

    @PostMapping
    @Operation(summary = "Yeni oda aç (sahip, giriş yapan kullanıcıdır)")
    @ApiResponse(responseCode = "201", description = "Oda oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<RoomResponse> odaEkle(@Valid @RequestBody RoomCreateRequest istek,
                                                Authentication girisYapan) {
        RoomResponse yeniOda = odaServisi.create(istek, girisYapan.getName());

        URI adres = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(yeniOda.id())
                .toUri();

        return ResponseEntity.created(adres).body(yeniOda);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Odayı tamamen güncelle (bütün zorunlu alanlar gönderilir)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public RoomResponse odaGuncelle(@PathVariable Long id, @Valid @RequestBody RoomUpdateRequest istek) {
        return odaServisi.update(id, istek);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Odayı kısmen güncelle (sadece gönderilen alanlar değişir; alan boşaltmak için PUT kullanılır)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya gövdede id var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public RoomResponse odaKismenGuncelle(@PathVariable Long id, @Valid @RequestBody RoomPatchRequest istek) {
        return odaServisi.patch(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Odayı sil (videoları ve oturumlarıyla birlikte)")
    @ApiResponse(responseCode = "204", description = "Silindi")
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> odaSil(@PathVariable Long id) {
        odaServisi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
