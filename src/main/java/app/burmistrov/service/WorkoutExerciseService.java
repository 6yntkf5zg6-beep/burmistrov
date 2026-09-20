package app.burmistrov.service;

import app.burmistrov.domain.Exercise;
import app.burmistrov.domain.Workout;
import app.burmistrov.domain.WorkoutExercise;
import app.burmistrov.dto.WorkoutExerciseRequest;
import app.burmistrov.dto.WorkoutExerciseResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ExerciseRepository;
import app.burmistrov.repository.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Exercise positions of a workout — the same endpoints whether the workout is a catalog one or a
 * day of a program.
 */
@Service
@RequiredArgsConstructor
public class WorkoutExerciseService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutService workoutService;

    @Transactional
    public WorkoutExerciseResponse add(Long workoutId, Long trainerId, WorkoutExerciseRequest request) {
        Workout workout = workoutService.getOwnedByTrainer(workoutId, trainerId);
        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));

        WorkoutExercise we = WorkoutExercise.builder()
                .workout(workout)
                .exercise(exercise)
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : 0)
                .notes(request.notes())
                .build();
        return WorkoutExerciseResponse.from(workoutExerciseRepository.save(we));
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> list(Long workoutId, Long trainerId) {
        workoutService.getOwnedByTrainer(workoutId, trainerId);
        return workoutExerciseRepository.findByWorkout_IdOrderByOrderIndexAsc(workoutId).stream()
                .map(WorkoutExerciseResponse::from)
                .toList();
    }

    @Transactional
    public WorkoutExerciseResponse update(Long workoutId, Long id, Long trainerId, WorkoutExerciseRequest request) {
        workoutService.getOwnedByTrainer(workoutId, trainerId);
        WorkoutExercise we = requireInWorkout(workoutId, id);

        if (!we.getExercise().getId().equals(request.exerciseId())) {
            Exercise exercise = exerciseRepository.findById(request.exerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
            we.setExercise(exercise);
        }
        we.setOrderIndex(request.orderIndex() != null ? request.orderIndex() : we.getOrderIndex());
        we.setNotes(request.notes());
        return WorkoutExerciseResponse.from(we);
    }

    @Transactional
    public void delete(Long workoutId, Long id, Long trainerId) {
        workoutService.getOwnedByTrainer(workoutId, trainerId);
        workoutExerciseRepository.delete(requireInWorkout(workoutId, id));
    }

    private WorkoutExercise requireInWorkout(Long workoutId, Long id) {
        WorkoutExercise we = workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found"));
        if (!we.getWorkout().getId().equals(workoutId)) {
            throw new ResourceNotFoundException("Workout exercise not found in this workout");
        }
        return we;
    }
}
