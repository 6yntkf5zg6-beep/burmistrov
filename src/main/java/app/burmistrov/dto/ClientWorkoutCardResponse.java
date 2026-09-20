package app.burmistrov.dto;

import app.burmistrov.service.WorkoutAttendanceService.AccessState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Карточка дня в кабинете клиента — без содержимого тренировки.
 *
 * <p>Упражнения сюда не кладутся сознательно: доступ должен закрывать данные, а не кнопку.
 * Если бы список отдавал документ целиком, спрятать его на экране было бы лишь видимостью защиты.
 */
public record ClientWorkoutCardResponse(
        Long id,
        LocalDate scheduledDate,
        String name,
        int exerciseCount,
        AccessState state,
        Instant attendedAt,
        Instant expiresAt,
        /** Вес, названный при открытии этой тренировки. Из этих точек строится график. */
        BigDecimal weightKg
) {
}
