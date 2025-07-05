package com.fitness.controllers;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
import com.fitness.services.interfaces.StudioService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Tag(name = "Studio", description = "Управление студиями")
@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

  private final StudioService studioService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> createStudio(@Valid @RequestBody StudioCreateUpdateDTO dto) {
        StudioDTO studioDTO = studioService.createStudio(dto);
        return ResponseEntity.ok(studioDTO);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> getStudio(@PathVariable Long id) {
        StudioDTO studioDTO = studioService.getStudio(id);
        return ResponseEntity.ok(studioDTO);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<StudioDTO>> getAllStudios() {
        return ResponseEntity.ok(studioService.getAllStudios());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> updateStudio(@PathVariable Long id, @Valid @RequestBody StudioCreateUpdateDTO dto) {
        StudioDTO studioDTO = studioService.updateStudio(id, dto);
        return ResponseEntity.ok(studioDTO);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Void> deleteStudio(@PathVariable Long id) {
        studioService.deleteStudio(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{studioId}/unique-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Long> getUniqueClients(@PathVariable Long studioId,
                                                 @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                 @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(studioService.countUniqueClients(studioId, start, end));
    }

    @GetMapping("/{studioId}/occupancy")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<Map<LocalDate, Integer>> getOccupancy(@PathVariable Long studioId,
                                                                @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                                @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(studioService.getOccupancy(studioId, start, end));
    }

    @GetMapping("/{studioId}/clients")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<UserDTO>> getUniqueClientsByStudio(@PathVariable Long studioId) {
        return ResponseEntity.ok(studioService.getUniqueClientsByStudio(studioId));
    }
    @PutMapping("/{studioId}/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<StudioDTO> assignAdmin(
            @PathVariable Long studioId,
            @PathVariable Long userId) {
        StudioDTO updated = studioService.assignAdminToStudio(studioId, userId);
        return ResponseEntity.ok(updated);
    }
}
