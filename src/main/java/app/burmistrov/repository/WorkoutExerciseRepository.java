package app.burmistrov.repository;

import app.burmistrov.domain.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    List<WorkoutExercise> findByWorkout_IdOrderByOrderIndexAsc(Long workoutId);

    boolean existsByExercise_Id(Long exerciseId);
}
