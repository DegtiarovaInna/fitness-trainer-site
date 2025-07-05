package com.fitness.mappers;

import com.fitness.dto.TimeSlotDTO;
import com.fitness.models.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TimeSlotMapper {


    // Маппинг из TimeSlot в TimeSlotDTO
    @Mapping(target = "studioId", expression = "java(timeSlot.getStudio() != null ? timeSlot.getStudio().getId() : null)")
    TimeSlotDTO timeSlotToTimeSlotDTO(TimeSlot timeSlot);

    // Маппинг из TimeSlotDTO в TimeSlot

  //  TimeSlot timeSlotDTOToTimeSlot(TimeSlotDTO timeSlotDTO);
}
