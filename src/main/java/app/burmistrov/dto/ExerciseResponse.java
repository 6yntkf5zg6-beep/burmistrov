package app.burmistrov.dto;

import app.burmistrov.domain.Exercise;

public record ExerciseResponse(
        Long id,
        String name,
        String description,
        String muscleGroup,
        String equipment,
        String videoUrl,
        String imageUrl,
        Long createdBy
) {
    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getMuscleGroup(),
                exercise.getEquipment(),
                exercise.getVideoUrl(),
                exercise.getImageUrl(),
                exercise.getCreatedBy() != null ? exercise.getCreatedBy().getId() : null
        );
    }
}
