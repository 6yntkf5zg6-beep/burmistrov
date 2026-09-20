package app.burmistrov.dto;

import app.burmistrov.domain.WorkoutDocument;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Открытая тренировка: содержимое, срок окна и комментарии клиента к упражнениям. */
public record ClientWorkoutResponse(
        Long id,
        LocalDate scheduledDate,
        String name,
        WorkoutDocument workout,
        Instant expiresAt,
        List<ExerciseCommentResponse> comments
) {
}
