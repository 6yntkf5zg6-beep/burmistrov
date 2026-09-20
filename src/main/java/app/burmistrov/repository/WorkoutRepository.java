package app.burmistrov.repository;

import app.burmistrov.domain.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    /** The trainer's catalog — standalone workouts, i.e. those not belonging to any program. */
    List<Workout> findByTrainer_IdAndTrainingProgramIsNull(Long trainerId);

    /** The days of one program, in the order the trainer arranged them. */
    List<Workout> findByTrainingProgram_IdOrderByOrderIndexAsc(Long trainingProgramId);
}
