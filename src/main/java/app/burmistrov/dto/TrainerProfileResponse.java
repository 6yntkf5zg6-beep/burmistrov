package app.burmistrov.dto;

import app.burmistrov.domain.TrainerProfile;

public record TrainerProfileResponse(
        Long id,
        Long userId,
        String bio,
        String specialization,
        Integer experienceYears
) {
    public static TrainerProfileResponse from(TrainerProfile profile) {
        return new TrainerProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getBio(),
                profile.getSpecialization(),
                profile.getExperienceYears()
        );
    }
}
