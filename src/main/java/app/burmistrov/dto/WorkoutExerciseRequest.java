package app.burmistrov.dto;

import jakarta.validation.constraints.NotNull;

public record WorkoutExerciseRequest(
        @NotNull Long exerciseId,
        Integer orderIndex,
        String notes
) {
}
