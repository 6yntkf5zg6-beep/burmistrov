package app.burmistrov.repository;

import app.burmistrov.domain.WorkoutAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutAttendanceRepository extends JpaRepository<WorkoutAttendance, Long> {

    Optional<WorkoutAttendance> findByScheduledWorkoutId(Long scheduledWorkoutId);

    List<WorkoutAttendance> findByScheduledWorkoutIdIn(List<Long> scheduledWorkoutIds);
}
