package app.burmistrov.dto;

import app.burmistrov.domain.TrainingProgram;

public record TrainingProgramResponse(
        Long id,
        Long trainerId,
        String name,
        String description
) {
    public static TrainingProgramResponse from(TrainingProgram program) {
        return new TrainingProgramResponse(
                program.getId(),
                program.getTrainer().getId(),
                program.getName(),
                program.getDescription()
        );
    }
}
