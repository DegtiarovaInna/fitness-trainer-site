package com.fitness.repositories;


import com.fitness.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
 //   boolean existsByEmail(String email);
 void deleteAllByEnabledFalseAndCreatedAtBefore(LocalDateTime cutoff);
    @Query("SELECT DISTINCT b.user FROM Booking b WHERE b.timeSlot.studio.id = :studioId")
    List<User> findDistinctUsersByStudioId(@Param("studioId") Long studioId);
}
