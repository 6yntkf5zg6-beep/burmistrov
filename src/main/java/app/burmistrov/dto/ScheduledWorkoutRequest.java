package app.burmistrov.dto;

import app.burmistrov.domain.WorkoutDocument;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Schedules a workout for a client. Exactly one of the two content fields decides what lands on
 * the day:
 * <ul>
 *   <li>{@code workout} — the document as the trainer arranged it, used verbatim;</li>
 *   <li>{@code sourceWorkoutId} alone — the server snapshots that workout as it is right now.</li>
 * </ul>
 * Both may be sent together: the document wins, and the id is kept as the soft link it grew from.
 */
public record ScheduledWorkoutRequest(
        @NotNull Long clientId,
        @NotNull LocalDate scheduledDate,
        Long sourceWorkoutId,
        WorkoutDocument workout
) {
}
