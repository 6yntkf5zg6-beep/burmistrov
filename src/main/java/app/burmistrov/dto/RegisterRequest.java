package app.burmistrov.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Регистрация клиента по ссылке-приглашению.
 *
 * Имя и фамилия приходят отсюда, а не из приглашения: тренер выдаёт ссылку, не зная
 * заранее, как клиент себя запишет.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String phone,
        @NotBlank String inviteToken
) {
}
