package com.fitness.controllers;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
import com.fitness.services.interfaces.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Studio", description = "Studio Management")
@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @Operation(summary = "Create studio (admin)")
    @ApiResponse(responseCode = "200", description = "Created",
            content = @Content(schema = @Schema(implementation = StudioDTO.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> createStudio(@Valid @RequestBody StudioCreateUpdateDTO dto) {
        StudioDTO studioDTO = studioService.createStudio(dto);
        return ResponseEntity.ok(studioDTO);
    }

    @Operation(summary = "Get studio by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = StudioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> getStudio(@PathVariable Long id) {
        StudioDTO studioDTO = studioService.getStudio(id);
        return ResponseEntity.ok(studioDTO);
    }

    @Operation(summary = "List studios")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudioDTO.class))))
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<StudioDTO>> getAllStudios() {
        return ResponseEntity.ok(studioService.getAllStudios());
    }

    @Operation(summary = "Update studio (admin)")
    @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(schema = @Schema(implementation = StudioDTO.class)))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<StudioDTO> updateStudio(@PathVariable Long id, @Valid @RequestBody StudioCreateUpdateDTO dto) {
        StudioDTO studioDTO = studioService.updateStudio(id, dto);
        return ResponseEntity.ok(studioDTO);
    }

    @Operation(summary = "Delete studio (admin)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Void> deleteStudio(@PathVariable Long id) {
        studioService.deleteStudio(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unique clients count (admin)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/{studioId}/unique-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Long> getUniqueClients(@PathVariable Long studioId,
                                                 @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                 @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(studioService.countUniqueClients(studioId, start, end));
    }

    @Operation(summary = "Occupancy by day (trainer/admin)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/{studioId}/occupancy")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<Map<LocalDate, Integer>> getOccupancy(@PathVariable Long studioId,
                                                                @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                                @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(studioService.getOccupancy(studioId, start, end));
    }

    @Operation(summary = "List unique clients by studio (trainer/admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    @GetMapping("/{studioId}/clients")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<UserDTO>> getUniqueClientsByStudio(@PathVariable Long studioId) {
        return ResponseEntity.ok(studioService.getUniqueClientsByStudio(studioId));
    }

    @Operation(summary = "Assign studio admin (admin)")
    @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(schema = @Schema(implementation = StudioDTO.class)))
    @PutMapping("/{studioId}/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<StudioDTO> assignAdmin(
            @PathVariable Long studioId,
            @PathVariable Long userId) {
        StudioDTO updated = studioService.assignAdminToStudio(studioId, userId);
        return ResponseEntity.ok(updated);
    }
}
