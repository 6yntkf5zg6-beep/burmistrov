package app.burmistrov.dto;

import app.burmistrov.domain.ClientProfile;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClientProfileResponse(
        Long id,
        Long userId,
        LocalDate birthDate,
        ClientProfile.Gender gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        String goal,
        String healthNotes
) {
    public static ClientProfileResponse from(ClientProfile profile) {
        return new ClientProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getBirthDate(),
                profile.getGender(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getGoal(),
                profile.getHealthNotes()
        );
    }
}
