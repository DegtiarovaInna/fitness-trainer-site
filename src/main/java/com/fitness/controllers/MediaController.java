package com.fitness.controllers;

import com.fitness.services.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload-url")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> getUploadUrl(@RequestParam String filename) {
        String key = UUID.randomUUID() + "-" + filename;
        String url = mediaService.generateUploadUrl(key).toString();
        return Map.of("key", key, "url", url);
    }

    @GetMapping("/download-url")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> getDownloadUrl(@RequestParam String key) {
        String url = mediaService.generateDownloadUrl(key).toString();
        return Map.of("url", url);
    }
}
