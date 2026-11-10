package com.roommates.study;

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
import com.roommates.study.dto.StudySessionCreateRequest;
import com.roommates.study.dto.StudySessionPatchRequest;
import com.roommates.study.dto.StudySessionResponse;
import com.roommates.study.dto.StudySessionUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/study-sessions")
@Tag(name = "Çalışma oturumları", description = "Çalışma odalarındaki ortak zamanlayıcılı oturumlar")
public class StudySessionController {
    private final StudySessionService oturumServisi;

    public StudySessionController(StudySessionService oturumServisi) {
        this.oturumServisi = oturumServisi;
    }

    @GetMapping
    @Operation(summary = "Çalışma oturumlarını listele (roomId ve status ile filtrelenebilir)")
    @ApiResponse(responseCode = "200", description = "Liste döndü")
    @ApiResponse(responseCode = "400", description = "Geçersiz filtre değeri",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<StudySessionResponse> oturumlariListele(@RequestParam(required = false) Long roomId,
                                                        @RequestParam(required = false) StudySessionStatus status) {
        return oturumServisi.findAll(roomId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Çalışma oturumunu numarasıyla getir")
    @ApiResponse(responseCode = "200", description = "Oturum bulundu")
    @ApiResponse(responseCode = "400", description = "id sayı değil",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oturum yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public StudySessionResponse oturumGetir(@PathVariable Long id) {
        return oturumServisi.findById(id);
    }

    @PostMapping
    @Operation(summary = "Yeni çalışma oturumu aç (süre verilmezse varsayılan 25/5 dakika)")
    @ApiResponse(responseCode = "201", description = "Oturum oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya oda çalışma odası değil",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<StudySessionResponse> oturumEkle(@Valid @RequestBody StudySessionCreateRequest istek) {
        StudySessionResponse yeniOturum = oturumServisi.create(istek);

        URI adres = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(yeniOturum.id())
                .toUri();

        return ResponseEntity.created(adres).body(yeniOturum);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Çalışma oturumunu tamamen güncelle (durum yalnız PLANNED → RUNNING → FINISHED ilerler)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya durum geçişi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oturum veya oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public StudySessionResponse oturumGuncelle(@PathVariable Long id,
                                               @Valid @RequestBody StudySessionUpdateRequest istek) {
        return oturumServisi.update(id, istek);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Çalışma oturumunu kısmen güncelle (ör. sadece durumunu RUNNING yap)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek, durum geçişi veya gövdede id var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oturum veya oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public StudySessionResponse oturumKismenGuncelle(@PathVariable Long id,
                                                     @Valid @RequestBody StudySessionPatchRequest istek) {
        return oturumServisi.patch(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Çalışma oturumunu sil")
    @ApiResponse(responseCode = "204", description = "Silindi")
    @ApiResponse(responseCode = "404", description = "Oturum yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> oturumSil(@PathVariable Long id) {
        oturumServisi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
