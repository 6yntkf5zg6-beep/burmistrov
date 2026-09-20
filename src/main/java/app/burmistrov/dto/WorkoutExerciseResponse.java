package app.burmistrov.dto;

import app.burmistrov.domain.WorkoutExercise;

public record WorkoutExerciseResponse(
        Long id,
        Long workoutId,
        Long exerciseId,
        String exerciseName,
        String exerciseDescription,
        Integer orderIndex,
        String notes
) {
    public static WorkoutExerciseResponse from(WorkoutExercise we) {
        return new WorkoutExerciseResponse(
                we.getId(),
                we.getWorkout().getId(),
                we.getExercise().getId(),
                we.getExercise().getName(),
                we.getExercise().getDescription(),
                we.getOrderIndex(),
                we.getNotes()
        );
    }
}
