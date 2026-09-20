package app.burmistrov.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddClientRequest(
        @NotBlank @Email String email
) {
}
