package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainingProgramRequest(
        @NotBlank String name,
        String description
) {
}
