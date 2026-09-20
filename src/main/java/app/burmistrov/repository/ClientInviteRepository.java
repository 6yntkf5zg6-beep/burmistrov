package app.burmistrov.repository;

import app.burmistrov.domain.ClientInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientInviteRepository extends JpaRepository<ClientInvite, Long> {

    Optional<ClientInvite> findByToken(String token);

    List<ClientInvite> findByTrainer_IdOrderByCreatedAtDesc(Long trainerId);
}
