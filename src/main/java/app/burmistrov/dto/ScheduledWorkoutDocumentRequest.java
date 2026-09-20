package app.burmistrov.dto;

import app.burmistrov.domain.WorkoutDocument;
import jakarta.validation.constraints.NotNull;

/** Replaces the workout of one already-scheduled day. */
public record ScheduledWorkoutDocumentRequest(@NotNull WorkoutDocument workout) {
}
