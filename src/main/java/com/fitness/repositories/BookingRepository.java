package com.fitness.repositories;

import com.fitness.enums.BookingStatus;
import com.fitness.models.Booking;
import com.fitness.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdAndStatusInAndTimeSlot_DateAfter(
            Long userId,
            List<BookingStatus> statuses,
            LocalDate date
    );

    // 1) уникальные клиенты за период (дата аренды между start и end)
    @Query("""
      SELECT DISTINCT b.user
      FROM Booking b
      WHERE b.timeSlot.studio.id = :studioId
        AND b.timeSlot.date BETWEEN :startDate AND :endDate
    """)
    List<User> findDistinctUsersByStudioAndPeriod(
            @Param("studioId")    Long studioId,
            @Param("startDate")   LocalDate startDate,
            @Param("endDate")     LocalDate endDate
    );

    // 2) заполняемость по дням: date → count
    @Query("""
      SELECT b.timeSlot.date AS day, COUNT(b)
      FROM Booking b
      WHERE b.timeSlot.studio.id = :studioId
        AND b.timeSlot.date BETWEEN :startDate AND :endDate
      GROUP BY b.timeSlot.date
    """)
    List<Object[]> countBookingsPerDate(
            @Param("studioId")    Long studioId,
            @Param("startDate")   LocalDate startDate,
            @Param("endDate")     LocalDate endDate
    );
// проверить наличие пробного занятия за последний год
boolean existsByUserIdAndTimeSlot_TrialTrueAndTimeSlot_DateAfter(Long userId, LocalDate date);

    boolean existsByTimeSlotIdAndStatusNot(Long timeSlotId, BookingStatus status);
    /**
     * Берёт все Booking на заданную дату (по свойству timeSlot.date),
     * у которых статус НЕ отменён.
     */
    List<Booking> findByTimeSlot_DateAndStatusNot(LocalDate date, BookingStatus status);
}
