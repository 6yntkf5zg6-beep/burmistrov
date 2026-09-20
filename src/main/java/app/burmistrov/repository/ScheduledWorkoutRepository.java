package app.burmistrov.repository;

import app.burmistrov.domain.ScheduledWorkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledWorkoutRepository extends JpaRepository<ScheduledWorkout, Long> {

    List<ScheduledWorkout> findByTrainer_IdAndClient_IdOrderByScheduledDateAsc(Long trainerId, Long clientId);

    List<ScheduledWorkout> findByClient_IdOrderByScheduledDateAsc(Long clientId);

    List<ScheduledWorkout> findByTrainer_IdOrderByScheduledDateAsc(Long trainerId);
}
