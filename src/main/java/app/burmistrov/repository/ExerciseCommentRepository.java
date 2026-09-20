package app.burmistrov.repository;

import app.burmistrov.domain.ExerciseComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseCommentRepository extends JpaRepository<ExerciseComment, Long> {

    List<ExerciseComment> findByScheduledWorkoutIdOrderByCreatedAtAsc(Long scheduledWorkoutId);

    void deleteByScheduledWorkoutId(Long scheduledWorkoutId);

    /** Комментарий к упражнению один: клиент его правит, а не пишет новый. */
    Optional<ExerciseComment> findByScheduledWorkoutIdAndExerciseUid(Long scheduledWorkoutId, String exerciseUid);

    List<ExerciseComment> findByScheduledWorkoutIdInOrderByCreatedAtAsc(List<Long> scheduledWorkoutIds);
}
