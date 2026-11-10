package com.roommates.video;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.roommates.common.error.ErrorResponse;
import com.roommates.video.dto.VideoCreateRequest;
import com.roommates.video.dto.VideoPatchRequest;
import com.roommates.video.dto.VideoResponse;
import com.roommates.video.dto.VideoUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/rooms/{roomId}/videos")
@Tag(name = "Video kuyruğu", description = "İzleme odasındaki sıralı video listesi")
public class VideoQueueController {
    private final VideoQueueService videoServisi;

    public VideoQueueController(VideoQueueService videoServisi) {
        this.videoServisi = videoServisi;
    }

    @GetMapping
    @Operation(summary = "Odanın video kuyruğunu sırasıyla listele")
    @ApiResponse(responseCode = "200", description = "Liste döndü")
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<VideoResponse> videolariListele(@PathVariable Long roomId) {
        return videoServisi.findAll(roomId);
    }

    @GetMapping("/{videoId}")
    @Operation(summary = "Kuyruktaki tek videoyu getir")
    @ApiResponse(responseCode = "200", description = "Video bulundu")
    @ApiResponse(responseCode = "404", description = "Oda veya video yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public VideoResponse videoGetir(@PathVariable Long roomId, @PathVariable Long videoId) {
        return videoServisi.findById(roomId, videoId);
    }

    @PostMapping
    @Operation(summary = "Kuyruğa video ekle (position verilmezse sona eklenir)")
    @ApiResponse(responseCode = "201", description = "Video eklendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya oda izleme odası değil",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<VideoResponse> videoEkle(@PathVariable Long roomId,
                                                   @Valid @RequestBody VideoCreateRequest istek,
                                                   Authentication girisYapan) {
        VideoResponse yeniVideo = videoServisi.create(roomId, istek, girisYapan.getName());

        URI adres = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(yeniVideo.id())
                .toUri();

        return ResponseEntity.created(adres).body(yeniVideo);
    }

    @PutMapping("/{videoId}")
    @Operation(summary = "Videoyu tamamen güncelle (sırası değişirse diğerleri kayar)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya sıra",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda veya video yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public VideoResponse videoGuncelle(@PathVariable Long roomId, @PathVariable Long videoId,
                                       @Valid @RequestBody VideoUpdateRequest istek) {
        return videoServisi.update(roomId, videoId, istek);
    }

    @PatchMapping("/{videoId}")
    @Operation(summary = "Videoyu kısmen güncelle (ör. sadece sırasını değiştir)")
    @ApiResponse(responseCode = "200", description = "Güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek, sıra veya gövdede id var",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Oda veya video yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public VideoResponse videoKismenGuncelle(@PathVariable Long roomId, @PathVariable Long videoId,
                                             @Valid @RequestBody VideoPatchRequest istek) {
        return videoServisi.patch(roomId, videoId, istek);
    }

    @DeleteMapping("/{videoId}")
    @Operation(summary = "Videoyu kuyruktan sil (sonrakiler bir yukarı kayar)")
    @ApiResponse(responseCode = "204", description = "Silindi")
    @ApiResponse(responseCode = "404", description = "Oda veya video yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Giriş yapılmamış",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Yetki yok",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> videoSil(@PathVariable Long roomId, @PathVariable Long videoId) {
        videoServisi.delete(roomId, videoId);
        return ResponseEntity.noContent().build();
    }
}
