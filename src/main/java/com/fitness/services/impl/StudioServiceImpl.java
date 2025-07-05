package com.fitness.services.impl;

import com.fitness.config.security.AccessValidator;
import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
import com.fitness.enums.Role;
import com.fitness.exceptions.StudioAlreadyExistsException;
import com.fitness.exceptions.StudioNotFoundException;
import com.fitness.exceptions.UserNotFoundException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.StudioMapper;
import com.fitness.mappers.UserMapper;
import com.fitness.models.Studio;
import com.fitness.models.User;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.StudioRepository;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.AuthService;
import com.fitness.services.interfaces.StudioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fitness.services.interfaces.SecurityService;

@Service
@RequiredArgsConstructor
public class StudioServiceImpl implements StudioService {

    private final StudioRepository studioRepository;


    private final StudioMapper studioMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
    //private final AccessValidator accessValidator;
    private final SecurityService securityService;
    @Override
    // Создание новой студии
    public StudioDTO createStudio(StudioCreateUpdateDTO dto) {
        securityService.requireAdminOrDev();
        if (studioRepository.existsByName(dto.getName())) {
            throw new StudioAlreadyExistsException(ErrorMessage.STUDIO_ALREADY_EXISTS);
        }
        Studio studio = new Studio();
        studio.setName(dto.getName());
        studio.setAddress(dto.getAddress());
        //studio.setAdmin(authService.getCurrentUser());
        studio = studioRepository.save(studio);
        return studioMapper.studioToStudioDTO(studio);
    }

    // Получение студии по ID
    @Override
    public StudioDTO getStudio(Long id) {
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));
        return studioMapper.studioToStudioDTO(studio);
    }

    // Обновление студии
    @Override
    public StudioDTO updateStudio(Long id, StudioCreateUpdateDTO dto) {
        securityService.requireAdminOrDev();
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));

        if (studioRepository.existsByName(dto.getName()) && !studio.getName().equals(dto.getName())) {
            throw new StudioAlreadyExistsException(ErrorMessage.STUDIO_ALREADY_EXISTS);
        }

        studio.setName(dto.getName());
        studio.setAddress(dto.getAddress());

        studio = studioRepository.save(studio);
        return studioMapper.studioToStudioDTO(studio);
    }

    // Удаление студии
    @Override
    public void deleteStudio(Long id) {
        securityService.requireAdminOrDev();
        // Проверка существования студии
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));

        // Удаление студии
        studioRepository.delete(studio);
    }
    @Override
    public List<StudioDTO> getAllStudios() {
        return studioRepository.findAll(Sort.by("id")).stream()
                .map(studioMapper::studioToStudioDTO)
                .collect(Collectors.toList());
    }

    //  Уникальные клиенты за период
    @Override
    public Long countUniqueClients(Long studioId, LocalDate start, LocalDate end) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        List<User> users = bookingRepository.findDistinctUsersByStudioAndPeriod(
                studioId, start, end
        );
        return (long) users.size();
    }

    // Заполняемость по дням
    @Override
    public Map<LocalDate, Integer> getOccupancy(Long studioId, LocalDate start, LocalDate end) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
//        Role currentRole = authService.getCurrentUserRole();
//        if (currentRole == Role.USER_PRO) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED_NOT_YOUR_STUDIO);
//            }
//        }
        List<Object[]> results = bookingRepository.countBookingsPerDate(
                studioId,
                start,
                end
        );

        Map<LocalDate, Integer> occupancyMap = new HashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Integer count = ((Number) row[1]).intValue();
            occupancyMap.put(date, count);
        }

        return occupancyMap;
    }

    // Уникальные клиенты студии (вне зависимости от даты)
    @Override
    public List<UserDTO> getUniqueClientsByStudio(Long studioId) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
//        Role currentRole = authService.getCurrentUserRole();
//        if (currentRole == Role.USER_PRO) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED_NOT_YOUR_STUDIO);
//            }
//        }
        List<User> users = userRepository.findDistinctUsersByStudioId(studioId);
        return users.stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Спец-метод: назначить существующему пользователю роль admin для конкретной студии.
     */
    @Override
    @Transactional
    public StudioDTO assignAdminToStudio(Long studioId, Long userId) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        // Находим студию
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));

//        // Получаем роль текущего пользователя
//        Role current = authService.getCurrentUserRole();
//
//        // Если текущий пользователь не DEV и не глобальный ADMIN,
//        // то он должен быть владельцем студии, иначе — отказ в доступе
//        if (current != Role.DEV && current != Role.ADMIN) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED_NOT_YOUR_STUDIO);
//            }
//        }

        // Находим пользователя, которого назначаем админом
        User newAdmin = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // Устанавливаем и сохраняем
        studio.setAdmin(newAdmin);
        Studio saved = studioRepository.save(studio);

        // Маппим в DTO и возвращаем
        return studioMapper.studioToStudioDTO(saved);
    }
}
