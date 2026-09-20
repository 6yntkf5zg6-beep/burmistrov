package app.burmistrov.service;

import app.burmistrov.domain.TrainerClient;
import app.burmistrov.domain.User;
import app.burmistrov.dto.AddClientRequest;
import app.burmistrov.dto.TrainerClientResponse;
import app.burmistrov.exception.ConflictException;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.TrainerClientRepository;
import app.burmistrov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerClientService {

    private final TrainerClientRepository trainerClientRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrainerClientResponse invite(Long trainerId, AddClientRequest request) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        User client = userRepository.findByEmail(request.email())
                .filter(u -> u.getRole() == User.Role.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.email()));

        trainerClientRepository.findByTrainer_IdAndClient_Id(trainerId, client.getId())
                .ifPresent(tc -> {
                    throw new ConflictException("Relation with this client already exists");
                });

        TrainerClient relation = TrainerClient.builder()
                .trainer(trainer)
                .client(client)
                .status(TrainerClient.Status.PENDING)
                .startedAt(Instant.now())
                .build();
        return TrainerClientResponse.from(trainerClientRepository.save(relation));
    }

    @Transactional
    public TrainerClientResponse accept(Long relationId, Long clientId) {
        TrainerClient relation = trainerClientRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        if (!relation.getClient().getId().equals(clientId)) {
            throw new AccessDeniedException("Not your invitation");
        }
        if (relation.getStatus() != TrainerClient.Status.PENDING) {
            throw new ConflictException("Invitation is not pending");
        }
        relation.setStatus(TrainerClient.Status.ACTIVE);
        relation.setStartedAt(Instant.now());
        return TrainerClientResponse.from(relation);
    }

    @Transactional
    public TrainerClientResponse archive(Long relationId, Long userId) {
        TrainerClient relation = trainerClientRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("Relation not found"));
        boolean party = relation.getTrainer().getId().equals(userId) || relation.getClient().getId().equals(userId);
        if (!party) {
            throw new AccessDeniedException("Not part of this relation");
        }
        if (relation.getStatus() == TrainerClient.Status.ARCHIVED) {
            throw new ConflictException("Relation is already archived");
        }
        relation.setStatus(TrainerClient.Status.ARCHIVED);
        relation.setEndedAt(Instant.now());
        return TrainerClientResponse.from(relation);
    }

    @Transactional(readOnly = true)
    public List<TrainerClientResponse> listForTrainer(Long trainerId) {
        return trainerClientRepository.findByTrainer_Id(trainerId).stream()
                .map(TrainerClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerClientResponse> listForClient(Long clientId) {
        return trainerClientRepository.findByClient_Id(clientId).stream()
                .map(TrainerClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isActiveRelation(Long trainerId, Long clientId) {
        return trainerClientRepository.findByTrainer_IdAndClient_Id(trainerId, clientId)
                .map(tc -> tc.getStatus() == TrainerClient.Status.ACTIVE)
                .orElse(false);
    }
}
