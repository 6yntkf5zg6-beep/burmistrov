package app.burmistrov.repository;

import app.burmistrov.domain.TrainerClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerClientRepository extends JpaRepository<TrainerClient, Long> {

    Optional<TrainerClient> findByTrainer_IdAndClient_Id(Long trainerId, Long clientId);

    List<TrainerClient> findByTrainer_Id(Long trainerId);

    List<TrainerClient> findByClient_Id(Long clientId);
}
