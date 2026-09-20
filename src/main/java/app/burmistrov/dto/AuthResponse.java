package app.burmistrov.dto;

import app.burmistrov.domain.User;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        User.Role role
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, Long userId, String email, User.Role role) {
        this(accessToken, refreshToken, "Bearer", expiresIn, userId, email, role);
    }
}
