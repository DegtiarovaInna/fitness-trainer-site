package com.fitness.services.impl;
import com.fitness.services.interfaces.PromoService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromoServiceImpl implements PromoService{
    private final Map<String,Integer> discounts = Map.of(
            "SLEEP10", 10,
            "FIT20",   20
    );

    @Override
    public long calculateDiscountAmount(String promoCode, long originalAmount) {
        if (promoCode == null) return 0;
        Integer pct = discounts.get(promoCode.toUpperCase());
        if (pct == null || pct <= 0) return 0;
        return originalAmount * pct / 100;
    }
}
