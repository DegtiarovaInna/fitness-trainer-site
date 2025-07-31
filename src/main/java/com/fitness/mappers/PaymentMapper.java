package com.fitness.mappers;

import com.fitness.dto.PaymentDTO;
import com.fitness.models.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "clientSecret", ignore = true)
    PaymentDTO paymentToPaymentDTO(Payment p);
}
