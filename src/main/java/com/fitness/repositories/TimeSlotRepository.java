package com.fitness.repositories;

import com.fitness.models.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
//    List<TimeSlot> findByStudioIdAndDate(Long studioId, LocalDate date);
//    List<TimeSlot> findByAvailableTrue();
//    List<TimeSlot> findByDate(LocalDate date);

    List<TimeSlot> findByStudioId(Long studioId);
    List<TimeSlot> findByStudioIdAndDateBetween(Long studioId, LocalDate startDate, LocalDate endDate);
    List<TimeSlot> findByStudioIdAndDateBetweenAndAvailableTrue(Long studioId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TimeSlot t
        WHERE t.studio.id = :studioId
          AND t.date      = :date
                 AND (:excludeSlotId IS NULL OR t.id <> :excludeSlotId)
              AND t.startTime < :endTime
          AND t.endTime   > :startTime
    """)
    boolean existsOverlapInStudio(
            @Param("studioId")      Long studioId,
            @Param("date")          LocalDate date,
            @Param("startTime")     LocalTime startTime,
            @Param("endTime")       LocalTime endTime,
            @Param("excludeSlotId") Long excludeSlotId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") Long id);
}
