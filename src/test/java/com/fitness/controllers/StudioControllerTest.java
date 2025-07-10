package com.fitness.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
import com.fitness.exceptions.StudioNotFoundException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.StudioService;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudioController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudioControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StudioService studioService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /api/studios — успешное создание студии")
    void createStudio_success() throws Exception {
        var req = new StudioCreateUpdateDTO("Yoga Place", "123 Main St");

        var dto = new StudioDTO();
        dto.setId(1L);
        dto.setName("Yoga Place");
        dto.setAddress("123 Main St");

        when(studioService.createStudio(req)).thenReturn(dto);

        mvc.perform(post("/api/studios")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Yoga Place"))
                .andExpect(jsonPath("$.address").value("123 Main St"));
    }

    @Test
    @DisplayName("GET /api/studios/{id} — успешное получение студии")
    void getStudio_success() throws Exception {
        var dto = new StudioDTO();
        dto.setId(2L);
        dto.setName("Pilates Hub");
        dto.setAddress("456 Elm St");

        when(studioService.getStudio(2L)).thenReturn(dto);

        mvc.perform(get("/api/studios/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Pilates Hub"))
                .andExpect(jsonPath("$.address").value("456 Elm St"));
    }

    @Test
    @DisplayName("GET /api/studios/{id} — студия не найдена")
    void getStudio_notFound() throws Exception {
        when(studioService.getStudio(99L))
                .thenThrow(new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));

        mvc.perform(get("/api/studios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("STUDIO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.STUDIO_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/studios — успешное получение списка студий")
    void getAllStudios_success() throws Exception {
        var dto1 = new StudioDTO(); dto1.setId(3L);
        var dto2 = new StudioDTO(); dto2.setId(4L);

        when(studioService.getAllStudios()).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/api/studios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4));
    }

    @Test
    @DisplayName("PUT /api/studios/{id} — успешное обновление студии")
    void updateStudio_success() throws Exception {
        var req = new StudioCreateUpdateDTO("New Name", "789 Oak St");

        var dto = new StudioDTO();
        dto.setId(5L);
        dto.setName("New Name");
        dto.setAddress("789 Oak St");

        when(studioService.updateStudio(5L, req)).thenReturn(dto);

        mvc.perform(put("/api/studios/5")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.address").value("789 Oak St"));
    }

    @Test
    @DisplayName("PUT /api/studios/{id} — студия не найдена при обновлении")
    void updateStudio_notFound() throws Exception {
        var req = new StudioCreateUpdateDTO("X", "Y");

        doThrow(new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND))
                .when(studioService).updateStudio(eq(6L), any());

        mvc.perform(put("/api/studios/6")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("STUDIO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.STUDIO_NOT_FOUND));
    }

    @Test
    @DisplayName("DELETE /api/studios/{id} — успешное удаление студии")
    void deleteStudio_success() throws Exception {
        doNothing().when(studioService).deleteStudio(7L);

        mvc.perform(delete("/api/studios/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/studios/{id} — студия не найдена при удалении")
    void deleteStudio_notFound() throws Exception {
        doThrow(new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND))
                .when(studioService).deleteStudio(8L);

        mvc.perform(delete("/api/studios/8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("STUDIO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.STUDIO_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/studios/{studioId}/unique-clients — успешный подсчет уникальных клиентов")
    void getUniqueClients_success() throws Exception {
        when(studioService.countUniqueClients(
                eq(9L),
                eq(LocalDate.parse("2025-07-01")),
                eq(LocalDate.parse("2025-07-31"))
        )).thenReturn(42L);

        mvc.perform(get("/api/studios/9/unique-clients")
                        .param("start", "2025-07-01")
                        .param("end", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("GET /api/studios/{studioId}/occupancy — успешное получение заполняемости")
    void getOccupancy_success() throws Exception {
        var occupancy = Map.of(
                LocalDate.parse("2025-07-01"), 5,
                LocalDate.parse("2025-07-02"), 3
        );
        when(studioService.getOccupancy(
                eq(10L),
                eq(LocalDate.parse("2025-07-01")),
                eq(LocalDate.parse("2025-07-31"))
        )).thenReturn(occupancy);

        mvc.perform(get("/api/studios/10/occupancy")
                        .param("start", "2025-07-01")
                        .param("end", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2025-07-01']").value(5))
                .andExpect(jsonPath("$.['2025-07-02']").value(3));
    }

    @Test
    @DisplayName("GET /api/studios/{studioId}/clients — успешное получение списка клиентов")
    void getUniqueClientsByStudio_success() throws Exception {
        var user = new UserDTO();
        user.setId(11L);
        user.setName("Alice");

        when(studioService.getUniqueClientsByStudio(11L))
                .thenReturn(List.of(user));

        mvc.perform(get("/api/studios/11/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    @DisplayName("PUT /api/studios/{studioId}/admin/{userId} — успешное назначение администратора")
    void assignAdmin_success() throws Exception {
        var dto = new StudioDTO();
        dto.setId(12L);
        dto.setName("Boxing Gym");
        dto.setAddress("321 Pine St");

        when(studioService.assignAdminToStudio(12L, 99L)).thenReturn(dto);

        mvc.perform(put("/api/studios/12/admin/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.name").value("Boxing Gym"))
                .andExpect(jsonPath("$.address").value("321 Pine St"));
    }
}
