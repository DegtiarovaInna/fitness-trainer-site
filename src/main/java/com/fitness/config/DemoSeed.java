package com.fitness.config;

import com.fitness.dto.StudioCreateUpdateDTO;
import com.fitness.dto.StudioDTO;
import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.services.interfaces.StudioService;
import com.fitness.services.interfaces.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DemoSeed implements CommandLineRunner {
    private final StudioService studioService;
    private final TimeSlotService timeSlotService;

    @Override
    public void run(String... args) {
        if (studioService.getAllStudios().isEmpty()) {
            var s = new StudioCreateUpdateDTO();
            s.setName("Fitness Pro Studio");
            s.setAddress("New York, 10 Alls");
            StudioDTO studio = studioService.createStudio(s);


            var now = java.time.LocalDate.now();
            timeSlotService.createTimeSlot(new TimeSlotCreateDTO(now, java.time.LocalTime.of(13, 0), java.time.LocalTime.of(14, 0), studio.getId(), 5000L));
            timeSlotService.createTimeSlot(new TimeSlotCreateDTO(now.plusDays(1), java.time.LocalTime.of(18, 0), java.time.LocalTime.of(19, 0), studio.getId(), 6000L));
        }
    }
}