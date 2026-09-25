package app.burmistrov.service;

import app.burmistrov.domain.ClientInvite;
import app.burmistrov.domain.User;
import app.burmistrov.dto.CreateInviteRequest;
import app.burmistrov.dto.InviteCheckResponse;
import app.burmistrov.dto.InviteResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ClientInviteRepository;
import app.burmistrov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class InviteService {

    private static final Duration VALIDITY = Duration.ofDays(7);
    private static final InviteCheckResponse INVALID = new InviteCheckResponse(false, null);

    private final ClientInviteRepository clientInviteRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public InviteResponse create(Long trainerId, CreateInviteRequest request) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        ClientInvite invite = ClientInvite.builder()
                .trainer(trainer)
                .token(UUID.randomUUID().toString().replace("-", ""))
                .label(normalizeLabel(request.label()))
                .expiresAt(Instant.now().plus(VALIDITY))
                .build();
        return InviteResponse.from(clientInviteRepository.save(invite), frontendBaseUrl);
    }

    @Transactional(readOnly = true)
    public List<InviteResponse> listForTrainer(Long trainerId) {
        return clientInviteRepository.findByTrainer_IdOrderByCreatedAtDesc(trainerId).stream()
                .map(invite -> InviteResponse.from(invite, frontendBaseUrl))
                .toList();
    }

    @Transactional(readOnly = true)
    public InviteCheckResponse check(String token) {
        return clientInviteRepository.findByToken(token)
                .filter(invite -> invite.getExpiresAt().isAfter(Instant.now()))
                .map(invite -> new InviteCheckResponse(true,
                        invite.getTrainer().getFirstName() + " " + invite.getTrainer().getLastName()))
                .orElse(INVALID);
    }

    /** Пустая пометка и пометка из одних пробелов — это отсутствие пометки, а не пустая строка. */
    private static String normalizeLabel(String label) {
        if (label == null) return null;
        String trimmed = label.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Убирает ещё не использованную ссылку.
     *
     * Проверка роли говорит лишь о том, что запрос пришёл от какого-то тренера, а id
     * в адресе подбирается перебором — поэтому сверяем владельца отдельно, иначе один
     * тренер смог бы гасить приглашения другого.
     *
     * Использованные приглашения удалять нечего: регистрация стирает их сама.
     */
    @Transactional
    public void delete(Long trainerId, Long inviteId) {
        ClientInvite invite = clientInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));
        if (!invite.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your invite");
        }
        clientInviteRepository.delete(invite);
    }
}
