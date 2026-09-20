package app.burmistrov.dto;

import app.burmistrov.domain.ProgramAssignment;

import java.time.Instant;
import java.util.List;

public record ProgramAssignmentResponse(
        Long id,
        Long clientId,
        Long sourceProgramId,
        String programName,
        Instant assignedAt,
        List<ProgramAssignment.AssignedWorkout> workouts
) {
    public static ProgramAssignmentResponse from(ProgramAssignment assignment) {
        return new ProgramAssignmentResponse(
                assignment.getId(),
                assignment.getClient().getId(),
                assignment.getSourceProgramId(),
                assignment.getProgramName(),
                assignment.getAssignedAt(),
                assignment.getWorkouts() != null ? assignment.getWorkouts().items() : List.of()
        );
    }
}
