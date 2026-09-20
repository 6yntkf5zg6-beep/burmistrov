package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;

public record ExerciseRequest(
        @NotBlank String name,
        String description,
        String muscleGroup,
        String equipment,
        String videoUrl,
        String imageUrl
) {
}
