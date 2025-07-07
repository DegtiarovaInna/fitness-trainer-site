package com.fitness.scheduling;

import com.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UnconfirmedUserCleanup {
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeOldUnconfirmedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        userRepository.deleteAllByEnabledFalseAndCreatedAtBefore(cutoff);
    }
}
