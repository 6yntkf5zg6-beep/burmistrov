package app.burmistrov.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Перенос дня на другую дату. */
public record ScheduledWorkoutDateRequest(
        @NotNull LocalDate date
) {
}
