package com.fitness.repositories;

import com.fitness.models.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByStudioIdAndDate(Long studioId, LocalDate date);
    List<TimeSlot> findByAvailableTrue();
    List<TimeSlot> findByDate(LocalDate date);

    List<TimeSlot> findByStudioId(Long studioId);
    List<TimeSlot> findByStudioIdAndDateBetween(Long studioId, LocalDate startDate, LocalDate endDate);
    List<TimeSlot> findByStudioIdAndDateBetweenAndAvailableTrue(Long studioId, LocalDate startDate, LocalDate endDate);
    /**
     * Проверяет, существует ли в студии слот на ту же дату-период,
     * исключая (при обновлении) слот с id = excludeSlotId.
     */
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
}
