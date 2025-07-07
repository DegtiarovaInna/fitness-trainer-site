package com.fitness.services.impl;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.UserDTO;
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
import com.fitness.services.interfaces.StudioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    private final SecurityService securityService;
    @Override
    public StudioDTO createStudio(StudioCreateUpdateDTO dto) {
        securityService.requireAdminOrDev();
        if (studioRepository.existsByName(dto.getName())) {
            throw new StudioAlreadyExistsException(ErrorMessage.STUDIO_ALREADY_EXISTS);
        }
        Studio studio = new Studio();
        studio.setName(dto.getName());
        studio.setAddress(dto.getAddress());
        studio = studioRepository.save(studio);
        return studioMapper.studioToStudioDTO(studio);
    }

    @Override
    public StudioDTO getStudio(Long id) {
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));
        return studioMapper.studioToStudioDTO(studio);
    }

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

    @Override
    public void deleteStudio(Long id) {
        securityService.requireAdminOrDev();
        Studio studio = studioRepository.findById(id)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));

        studioRepository.delete(studio);
    }
    @Override
    public List<StudioDTO> getAllStudios() {
        return studioRepository.findAll(Sort.by("id")).stream()
                .map(studioMapper::studioToStudioDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countUniqueClients(Long studioId, LocalDate start, LocalDate end) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        List<User> users = bookingRepository.findDistinctUsersByStudioAndPeriod(
                studioId, start, end
        );
        return (long) users.size();
    }

    @Override
    public Map<LocalDate, Integer> getOccupancy(Long studioId, LocalDate start, LocalDate end) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
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

    @Override
    public List<UserDTO> getUniqueClientsByStudio(Long studioId) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);

        List<User> users = userRepository.findDistinctUsersByStudioId(studioId);
        return users.stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public StudioDTO assignAdminToStudio(Long studioId, Long userId) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));


        User newAdmin = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));


        studio.setAdmin(newAdmin);
        Studio saved = studioRepository.save(studio);


        return studioMapper.studioToStudioDTO(saved);
    }
}
