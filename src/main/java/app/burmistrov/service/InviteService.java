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

@Service
@RequiredArgsConstructor
public class InviteService {

    private static final Duration VALIDITY = Duration.ofDays(7);
    private static final InviteCheckResponse INVALID = new InviteCheckResponse(false, null, null, null);

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
                .firstName(request.firstName())
                .lastName(request.lastName())
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
                        invite.getTrainer().getFirstName() + " " + invite.getTrainer().getLastName(),
                        invite.getFirstName(), invite.getLastName()))
                .orElse(INVALID);
    }
}
