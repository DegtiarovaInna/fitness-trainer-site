package com.fitness.services.interfaces;

public interface PromoService {
    long calculateDiscountAmount(String promoCode, long originalAmount);
}
