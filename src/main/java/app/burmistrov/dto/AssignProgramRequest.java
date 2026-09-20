package app.burmistrov.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Назначает программу клиенту. Расписание — решение о множестве, поэтому его собирает клиент
 * целиком и присылает готовым: какая тренировка программы на какую дату встаёт.
 *
 * <p>Список не обязан покрывать всю программу — тренер вправе исключить отдельные тренировки из
 * конкретного назначения, сама программа при этом не меняется.
 */
public record AssignProgramRequest(
        @NotNull Long clientId,
        @NotNull Long programId,
        @NotEmpty List<@Valid @NotNull Placement> placements
) {
    public record Placement(@NotNull Long workoutId, @NotNull LocalDate date) {
    }
}
