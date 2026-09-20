package app.burmistrov.dto;

import app.burmistrov.domain.Workout;

public record WorkoutResponse(
        Long id,
        Long trainerId,
        String name,
        String description,
        Long trainingProgramId,
        Integer orderIndex
) {
    public static WorkoutResponse from(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getTrainer().getId(),
                workout.getName(),
                workout.getDescription(),
                workout.belongsToProgram() ? workout.getTrainingProgram().getId() : null,
                workout.getOrderIndex()
        );
    }
}
