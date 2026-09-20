package app.burmistrov.dto;

import app.burmistrov.domain.ExerciseComment;

import java.time.Instant;

public record ExerciseCommentResponse(
        Long id,
        Long scheduledWorkoutId,
        String exerciseUid,
        String exerciseName,
        Long authorId,
        String text,
        Instant createdAt
) {
    public static ExerciseCommentResponse from(ExerciseComment comment) {
        return new ExerciseCommentResponse(
                comment.getId(),
                comment.getScheduledWorkoutId(),
                comment.getExerciseUid(),
                comment.getExerciseName(),
                comment.getAuthor().getId(),
                comment.getText(),
                comment.getCreatedAt());
    }
}
