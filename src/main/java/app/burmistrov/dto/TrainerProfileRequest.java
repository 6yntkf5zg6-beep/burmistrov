package app.burmistrov.dto;

public record TrainerProfileRequest(
        String bio,
        String specialization,
        Integer experienceYears
) {
}
