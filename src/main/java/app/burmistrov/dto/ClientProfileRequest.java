package app.burmistrov.dto;

import app.burmistrov.domain.ClientProfile;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClientProfileRequest(
        LocalDate birthDate,
        ClientProfile.Gender gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        String goal,
        String healthNotes
) {
}
