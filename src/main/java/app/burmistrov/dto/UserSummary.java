package app.burmistrov.dto;

import app.burmistrov.domain.User;

public record UserSummary(
        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role
) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }
}
