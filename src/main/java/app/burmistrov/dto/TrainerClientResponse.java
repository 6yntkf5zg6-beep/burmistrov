package app.burmistrov.dto;

import app.burmistrov.domain.TrainerClient;

import java.time.Instant;

public record TrainerClientResponse(
        Long id,
        Long trainerId,
        String trainerName,
        Long clientId,
        String clientName,
        TrainerClient.Status status,
        Instant startedAt
) {
    public static TrainerClientResponse from(TrainerClient tc) {
        return new TrainerClientResponse(
                tc.getId(),
                tc.getTrainer().getId(),
                tc.getTrainer().getFirstName() + " " + tc.getTrainer().getLastName(),
                tc.getClient().getId(),
                tc.getClient().getFirstName() + " " + tc.getClient().getLastName(),
                tc.getStatus(),
                tc.getStartedAt()
        );
    }
}
