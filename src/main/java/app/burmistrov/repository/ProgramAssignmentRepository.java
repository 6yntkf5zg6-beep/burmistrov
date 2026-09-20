package app.burmistrov.repository;

import app.burmistrov.domain.ProgramAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ProgramAssignmentRepository extends JpaRepository<ProgramAssignment, Long> {

    List<ProgramAssignment> findByTrainer_IdAndClient_IdAndAssignedAtAfterOrderByAssignedAtDesc(
            Long trainerId, Long clientId, Instant since);

    List<ProgramAssignment> findByTrainer_IdAndClient_Id(Long trainerId, Long clientId);
}
