package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInviteRequest(
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}
