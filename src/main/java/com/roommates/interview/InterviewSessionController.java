package com.roommates.interview;

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
import com.roommates.interview.dto.InterviewSessionCreateRequest;
import com.roommates.interview.dto.InterviewSessionPatchRequest;
import com.roommates.interview.dto.InterviewSessionResponse;
import com.roommates.interview.dto.InterviewSessionUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/interview-sessions")
@Tag(name = "Mülakat oturumları", description = "Mülakat odalarındaki mülakatçı ve aday eşleşmeleri")
public class InterviewSessionController {
    private final InterviewSessionService mulakatServisi;

    public InterviewSessionController(InterviewSessionService mulakatServisi) {
        this.mulakatServisi = mulakatServisi;
    }

    @GetMapping
    @Operation(summary = "Mülakat oturumlarını listele (status ile filtrelenebilir)")
    @ApiResponse(responseCode = "200", description = "Liste döndü")
    @ApiResponse(responseCode = "400", description = "Geçersiz filtre değeri",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<InterviewSessionResponse> mulakatlariListele(@RequestParam(required = false) InterviewStatus status) {
        return mulakatServisi.findAll(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Mülakat oturumunu numarasıyla getir")
    @ApiResponse(responseCode = "200", description = "Oturum bulundu")
    @ApiResponse(responseCode = "404", description = "Oturum yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public InterviewSessionResponse mulakatGetir(@PathVariable Long id) {
        return mulakatServisi.findById(id);
    }

    @PostMapping
    @Operation(summary = "Yeni mülakat oturumu aç (soru kategoriye göre sunucuda seçilir)")
    @ApiResponse(responseCode = "201", description = "Oturum oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek, oda mülakat odası değil veya mülakatçı ile aday aynı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda veya kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<InterviewSessionResponse> mulakatEkle(@Valid @RequestBody InterviewSessionCreateRequest istek) {
        InterviewSessionResponse yeniMulakat = mulakatServisi.create(istek);

        URI adres = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(yeniMulakat.id())
                .toUri();

        return ResponseEntity.created(adres).body(yeniMulakat);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mülakat oturumunu tamamen güncelle")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oturum, oda veya kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public InterviewSessionResponse mulakatGuncelle(@PathVariable Long id,
                                                    @Valid @RequestBody InterviewSessionUpdateRequest istek) {
        return mulakatServisi.update(id, istek);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mülakat oturumunu kısmen güncelle (ör. sadece durumunu değiştir)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya gövdede id var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oturum, oda veya kullanıcı yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public InterviewSessionResponse mulakatKismenGuncelle(@PathVariable Long id,
                                                          @Valid @RequestBody InterviewSessionPatchRequest istek) {
        return mulakatServisi.patch(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Mülakat oturumunu sil")
    @ApiResponse(responseCode = "204", description = "Silindi")
    @ApiResponse(responseCode = "404", description = "Oturum yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> mulakatSil(@PathVariable Long id) {
        mulakatServisi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
