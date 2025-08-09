package com.fitness.controllers;

import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;
import com.fitness.services.interfaces.TimeSlotService;
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

@Tag(name = "TimeSlot", description = "TimeSlot Management")
@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    @Operation(summary = "Create timeslot (admin)")
    @ApiResponse(responseCode = "200", description = "Created",
            content = @Content(schema = @Schema(implementation = TimeSlotDTO.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<TimeSlotDTO> createTimeSlot(@Valid @RequestBody TimeSlotCreateDTO dto) {
        TimeSlotDTO created = timeSlotService.createTimeSlot(dto);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Get timeslot by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = TimeSlotDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<TimeSlotDTO> getTimeSlot(@PathVariable Long id) {
        TimeSlotDTO timeSlotDTO = timeSlotService.getTimeSlot(id);
        return ResponseEntity.ok(timeSlotDTO);
    }

    @Operation(summary = "List all timeslots (admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeSlotDTO.class))))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<List<TimeSlotDTO>> getAllTimeSlots() {
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots());
    }

    @Operation(summary = "Update timeslot (admin)")
    @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(schema = @Schema(implementation = TimeSlotDTO.class)))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<TimeSlotDTO> updateTimeSlot(@PathVariable Long id,
                                                      @Valid @RequestBody TimeSlotUpdateDTO dto) {
        TimeSlotDTO updated = timeSlotService.updateTimeSlot(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete timeslot (admin)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List timeslots by studio (trainer/admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeSlotDTO.class))))
    @GetMapping("/studio/{studioId}")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByStudio(@PathVariable Long studioId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByStudio(studioId));
    }

    @Operation(summary = "Available slots by studio within date range")
    @ApiResponse(
            responseCode = "200",
            description = "List of available slots",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeSlotDTO.class)))
    )
    @GetMapping("/studio/{studioId}/available")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(@PathVariable Long studioId,
                                                               @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                               @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlotsByStudio(studioId, start, end));
    }

    @Operation(summary = "Timeslots by studio and date range (trainer/admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeSlotDTO.class))))
    @GetMapping("/studio/{studioId}/dates")
    @PreAuthorize("hasAnyRole('USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByStudioAndDateRange(@PathVariable Long studioId,
                                                                              @RequestParam @Schema(type = "string", example = "2025-07-01") LocalDate start,
                                                                              @RequestParam @Schema(type = "string", example = "2025-07-31") LocalDate end) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByStudioAndDateRange(studioId, start, end));
    }
}