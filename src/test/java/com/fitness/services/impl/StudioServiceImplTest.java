package com.fitness.services.impl;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
import com.fitness.exceptions.StudioAlreadyExistsException;
import com.fitness.exceptions.StudioNotFoundException;
import com.fitness.exceptions.UserNotFoundException;
import com.fitness.mappers.StudioMapper;
import com.fitness.mappers.UserMapper;
import com.fitness.models.Studio;
import com.fitness.models.User;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.StudioRepository;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudioServiceImplTest {
    private StudioRepository studioRepo;
    private StudioMapper studioMapper;
    private BookingRepository bookingRepo;
    private UserRepository userRepo;
    private UserMapper userMapper;
    private SecurityService securityService;
    private StudioServiceImpl service;

    @BeforeEach
    void setUp() {
        studioRepo      = mock(StudioRepository.class);
        studioMapper    = mock(StudioMapper.class);
        bookingRepo     = mock(BookingRepository.class);
        userRepo        = mock(UserRepository.class);
        userMapper      = mock(UserMapper.class);
        securityService = mock(SecurityService.class);
        doNothing().when(securityService).requireAdminOrDev();

        service = new StudioServiceImpl(
                studioRepo, studioMapper, bookingRepo, userRepo, userMapper, securityService
        );
    }

    // createStudio
    @Test
    void createStudio_whenExists_throwsAlreadyExists() {
        var dto = new StudioCreateUpdateDTO("Gym", "Addr");
        when(studioRepo.existsByName("Gym")).thenReturn(true);
        assertThrows(StudioAlreadyExistsException.class, () -> service.createStudio(dto));
        verify(studioRepo, never()).save(any());
    }

    @Test
    void createStudio_success_returnsDto() {
        var dto = new StudioCreateUpdateDTO("GymX", "AddrX");
        when(studioRepo.existsByName("GymX")).thenReturn(false);

        var entity = new Studio(); entity.setId(1L); entity.setName("GymX"); entity.setAddress("AddrX");
        when(studioRepo.save(any(Studio.class))).thenReturn(entity);

        var outDto = new StudioDTO(); outDto.setId(1L);
        when(studioMapper.studioToStudioDTO(entity)).thenReturn(outDto);

        var result = service.createStudio(dto);
        verify(securityService).requireAdminOrDev();
        verify(studioRepo).save(any());
        assertSame(outDto, result);
    }

    // getStudio
    @Test
    void getStudio_missing_throwsNotFound() {
        when(studioRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(StudioNotFoundException.class, () -> service.getStudio(5L));
    }

    @Test
    void getStudio_exists_returnsDto() {
        var studio = new Studio(); studio.setId(2L);
        when(studioRepo.findById(2L)).thenReturn(Optional.of(studio));
        var dto = new StudioDTO(); dto.setId(2L);
        when(studioMapper.studioToStudioDTO(studio)).thenReturn(dto);
        assertSame(dto, service.getStudio(2L));
    }

    // getAllStudios
    @Test
    void getAllStudios_mapsSorted() {
        var s1 = new Studio(); s1.setId(1L);
        var s2 = new Studio(); s2.setId(2L);
        when(studioRepo.findAll(Sort.by("id"))).thenReturn(List.of(s1, s2));
        var d1 = new StudioDTO(); d1.setId(1L);
        var d2 = new StudioDTO(); d2.setId(2L);
        when(studioMapper.studioToStudioDTO(s1)).thenReturn(d1);
        when(studioMapper.studioToStudioDTO(s2)).thenReturn(d2);
        var list = service.getAllStudios();
        assertEquals(List.of(d1, d2), list);
    }

    // updateStudio
    @Test
    void updateStudio_missing_throwsNotFound() {
        var dto = new StudioCreateUpdateDTO("A","B");
        when(studioRepo.findById(3L)).thenReturn(Optional.empty());
        assertThrows(StudioNotFoundException.class, () -> service.updateStudio(3L, dto));
    }

    @Test
    void updateStudio_nameConflict_throwsAlreadyExists() {
        var studio = new Studio(); studio.setId(4L); studio.setName("Old");
        when(studioRepo.findById(4L)).thenReturn(Optional.of(studio));
        when(studioRepo.existsByName("New")).thenReturn(true);
        var dto = new StudioCreateUpdateDTO("New","Addr");
        assertThrows(StudioAlreadyExistsException.class, () -> service.updateStudio(4L, dto));
    }

    @Test
    void updateStudio_success_returnsDto() {
        var studio = new Studio(); studio.setId(5L); studio.setName("Old");
        when(studioRepo.findById(5L)).thenReturn(Optional.of(studio));
        when(studioRepo.existsByName("New")).thenReturn(false);
        var dto = new StudioCreateUpdateDTO("New","Addr");
        var saved = new Studio(); saved.setId(5L);
        when(studioRepo.save(studio)).thenReturn(saved);
        var out = new StudioDTO(); out.setId(5L);
        when(studioMapper.studioToStudioDTO(saved)).thenReturn(out);
        assertSame(out, service.updateStudio(5L, dto));
    }

    // deleteStudio
    @Test
    void deleteStudio_missing_throwsNotFound() {
        when(studioRepo.findById(6L)).thenReturn(Optional.empty());
        assertThrows(StudioNotFoundException.class, () -> service.deleteStudio(6L));
    }

    @Test
    void deleteStudio_existing_deletes() {
        var s = new Studio(); s.setId(7L);
        when(studioRepo.findById(7L)).thenReturn(Optional.of(s));
        service.deleteStudio(7L);
        verify(securityService).requireAdminOrDev();
        verify(studioRepo).delete(s);
    }

    // countUniqueClients
    @Test
    void countUniqueClients_callsRepo() {
        when(bookingRepo.findDistinctUsersByStudioAndPeriod(8L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,31)))
                .thenReturn(List.of(new User(), new User()));
        var count = service.countUniqueClients(8L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,31));
        verify(securityService).requireStudioOwnerOrAdminOrDev(8L);
        assertEquals(2L, count);
    }

    // getOccupancy
    @Test
    void getOccupancy_mapsCorrectly() {
        Object[] row = new Object[]{LocalDate.of(2025,2,2), 5L};
        when(bookingRepo.countBookingsPerDate(9L,
                LocalDate.of(2025,2,1),
                LocalDate.of(2025,2,28)
        )).thenReturn(List.<Object[]>of(row));
        var map = service.getOccupancy(9L, LocalDate.of(2025,2,1), LocalDate.of(2025,2,28));
        verify(securityService).requireStudioOwnerOrAdminOrDev(9L);
        assertEquals(Map.of(LocalDate.of(2025,2,2), 5), map);
    }

    // getUniqueClientsByStudio
    @Test
    void getUniqueClientsByStudio_mapsUsers() {
        var u = new User(); u.setId(3L);
        when(userRepo.findDistinctUsersByStudioId(10L)).thenReturn(List.of(u));
        var dto = new UserDTO(); dto.setId(3L);
        when(userMapper.userToUserDTO(u)).thenReturn(dto);
        var list = service.getUniqueClientsByStudio(10L);
        verify(securityService).requireStudioOwnerOrAdminOrDev(10L);
        assertEquals(List.of(dto), list);
    }

    // assignAdminToStudio
    @Test
    void assignAdminStudio_missingStudio_throwsNotFound() {
        when(studioRepo.findById(11L)).thenReturn(Optional.empty());
        assertThrows(StudioNotFoundException.class, () -> service.assignAdminToStudio(11L, 1L));
    }

    @Test
    void assignAdminStudio_missingUser_throwsNotFound() {
        var st = new Studio(); st.setId(12L);
        when(studioRepo.findById(12L)).thenReturn(Optional.of(st));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.assignAdminToStudio(12L, 2L));
    }

    @Test
    void assignAdminStudio_success_returnsDto() {
        var st = new Studio(); st.setId(13L);
        when(studioRepo.findById(13L)).thenReturn(Optional.of(st));
        var user = new User(); user.setId(4L);
        when(userRepo.findById(4L)).thenReturn(Optional.of(user));
        var saved = new Studio(); saved.setId(13L); saved.setAdmin(user);
        when(studioRepo.save(st)).thenReturn(saved);
        var out = new StudioDTO(); out.setId(13L);
        when(studioMapper.studioToStudioDTO(saved)).thenReturn(out);

        assertSame(out, service.assignAdminToStudio(13L, 4L));
        verify(securityService).requireStudioOwnerOrAdminOrDev(13L);
    }
}
