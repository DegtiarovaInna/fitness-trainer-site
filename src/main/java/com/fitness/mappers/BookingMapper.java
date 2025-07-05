package com.fitness.mappers;

import com.fitness.dto.BookingDTO;
import com.fitness.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingMapper {


    // Маппинг из Booking в BookingDTO
    @Mapping(target = "userId",     source = "user.id")
    @Mapping(target = "timeSlotId", source = "timeSlot.id")
    @Mapping(target = "status",     source = "status")
    @Mapping(target = "createdAt",  source = "createdAt")
    BookingDTO bookingToBookingDTO(Booking booking);

    // Маппинг из BookingDTO в Booking
  //  Booking bookingDTOToBooking(BookingDTO bookingDTO);
}
