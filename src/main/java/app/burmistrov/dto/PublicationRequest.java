package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;

public record PublicationRequest(
        @NotBlank String title,
        @NotBlank String authors,
        @NotBlank String annotation
) {
}
