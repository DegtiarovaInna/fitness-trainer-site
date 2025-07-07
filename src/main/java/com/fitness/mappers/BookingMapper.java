package com.fitness.mappers;

import com.fitness.dto.BookingDTO;
import com.fitness.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId",     source = "user.id")
    @Mapping(target = "timeSlotId", source = "timeSlot.id")
    @Mapping(target = "status",     source = "status")
    @Mapping(target = "createdAt",  source = "createdAt")
    BookingDTO bookingToBookingDTO(Booking booking);


}
