package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param orderIndex      position inside the program; ignored for a catalog workout
 * @param sourceWorkoutId optional catalog workout to copy the exercises from when creating a
 *                        program day; ignored on update
 */
public record WorkoutRequest(
        @NotBlank String name,
        String description,
        Integer orderIndex,
        Long sourceWorkoutId
) {
}
