package app.burmistrov.dto;

import app.burmistrov.domain.ClientInvite;

import java.time.Instant;

/**
 * Ссылка-приглашение в списке у тренера.
 *
 * {@code label} — его собственная пометка «для кого»; может быть пустой.
 */
public record InviteResponse(
        Long id,
        String token,
        String registrationUrl,
        String label,
        Instant createdAt,
        Instant expiresAt
) {
    public static InviteResponse from(ClientInvite invite, String frontendBaseUrl) {
        return new InviteResponse(
                invite.getId(),
                invite.getToken(),
                frontendBaseUrl + "/register/" + invite.getToken(),
                invite.getLabel(),
                invite.getCreatedAt(),
                invite.getExpiresAt()
        );
    }
}
