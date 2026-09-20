package app.burmistrov.dto;

public record InviteCheckResponse(
        boolean valid,
        String trainerName,
        String firstName,
        String lastName
) {
}
