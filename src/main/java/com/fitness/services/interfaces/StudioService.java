package com.fitness.services.interfaces;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StudioService {
    StudioDTO createStudio(StudioCreateUpdateDTO dto);
    StudioDTO getStudio(Long id);
    List<StudioDTO> getAllStudios();
    StudioDTO updateStudio(Long id, StudioCreateUpdateDTO dto);
    void deleteStudio(Long id);

    Long countUniqueClients(Long studioId, LocalDate start, LocalDate end);
    Map<LocalDate, Integer> getOccupancy(Long studioId, LocalDate start, LocalDate end);
    List<UserDTO> getUniqueClientsByStudio(Long studioId);

    StudioDTO assignAdminToStudio(Long studioId, Long userId);
}
