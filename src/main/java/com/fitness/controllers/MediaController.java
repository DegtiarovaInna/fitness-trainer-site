package com.fitness.controllers;

import com.fitness.services.interfaces.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Media", description = "Pre-signed URLs for uploads/downloads")
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @Operation(summary = "Create pre-signed PUT URL to upload file")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(example = "{\"key\":\"<uuid-filename.jpg>\",\"url\":\"https://...\"}")))
    @PostMapping("/upload-url")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> getUploadUrl(@RequestParam String filename) {
        String key = UUID.randomUUID() + "-" + filename;
        String url = mediaService.generateUploadUrl(key).toString();
        return Map.of("key", key, "url", url);
    }

    @Operation(summary = "Create pre-signed GET URL to download file")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(example = "{\"url\":\"https://...\"}")))
    @GetMapping("/download-url")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> getDownloadUrl(@RequestParam String key) {
        String url = mediaService.generateDownloadUrl(key).toString();
        return Map.of("url", url);
    }
}
