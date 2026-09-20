package app.burmistrov.dto;

import app.burmistrov.domain.ScheduledWorkout;
import app.burmistrov.domain.WorkoutDocument;

import java.time.LocalDate;
import java.util.List;

public record ScheduledWorkoutResponse(
        Long id,
        Long clientId,
        LocalDate scheduledDate,
        Long sourceWorkoutId,
        String name,
        WorkoutDocument workout,
        /** Посещение и окно доступа; {@code null}, если тренировку ещё не открывали. */
        WorkoutAttendanceResponse attendance,
        /** Комментарии клиента к упражнениям этого дня. */
        List<ExerciseCommentResponse> comments
) {
    public static ScheduledWorkoutResponse from(ScheduledWorkout sw) {
        return from(sw, null, List.of());
    }

    public static ScheduledWorkoutResponse from(ScheduledWorkout sw,
                                                WorkoutAttendanceResponse attendance,
                                                List<ExerciseCommentResponse> comments) {
        return new ScheduledWorkoutResponse(
                sw.getId(),
                sw.getClient().getId(),
                sw.getScheduledDate(),
                sw.getSourceWorkoutId(),
                sw.getWorkout() != null ? sw.getWorkout().name() : null,
                sw.getWorkout(),
                attendance,
                comments != null ? comments : List.of());
    }
}
