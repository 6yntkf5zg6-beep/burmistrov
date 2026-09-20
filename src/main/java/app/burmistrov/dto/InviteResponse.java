package app.burmistrov.dto;

import app.burmistrov.domain.ClientInvite;

import java.time.Instant;

public record InviteResponse(
        Long id,
        String token,
        String registrationUrl,
        String firstName,
        String lastName,
        Instant createdAt,
        Instant expiresAt
) {
    public static InviteResponse from(ClientInvite invite, String frontendBaseUrl) {
        return new InviteResponse(
                invite.getId(),
                invite.getToken(),
                frontendBaseUrl + "/register/" + invite.getToken(),
                invite.getFirstName(),
                invite.getLastName(),
                invite.getCreatedAt(),
                invite.getExpiresAt()
        );
    }
}
