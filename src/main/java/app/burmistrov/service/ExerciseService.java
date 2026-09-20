package app.burmistrov.service;

import app.burmistrov.domain.Exercise;
import app.burmistrov.domain.User;
import app.burmistrov.dto.ExerciseRequest;
import app.burmistrov.dto.ExerciseResponse;
import app.burmistrov.exception.ConflictException;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ExerciseRepository;
import app.burmistrov.repository.UserRepository;
import app.burmistrov.repository.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;

    @Transactional(readOnly = true)
    public List<ExerciseResponse> list(Long trainerId) {
        return exerciseRepository.findByCreatedByIsNullOrCreatedBy_Id(trainerId).stream()
                .map(ExerciseResponse::from)
                .toList();
    }

    @Transactional
    public ExerciseResponse create(Long trainerId, ExerciseRequest request) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        requireNameIsFree(request.name(), trainerId, null);

        Exercise exercise = Exercise.builder()
                .name(request.name())
                .description(request.description())
                .muscleGroup(request.muscleGroup())
                .equipment(request.equipment())
                .videoUrl(request.videoUrl())
                .imageUrl(request.imageUrl())
                .createdBy(trainer)
                .build();
        return ExerciseResponse.from(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseResponse update(Long trainerId, Long exerciseId, ExerciseRequest request) {
        Exercise exercise = getOwnedByTrainer(exerciseId, trainerId);
        requireNameIsFree(request.name(), trainerId, exerciseId);
        exercise.setName(request.name());
        exercise.setDescription(request.description());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setEquipment(request.equipment());
        exercise.setVideoUrl(request.videoUrl());
        exercise.setImageUrl(request.imageUrl());
        return ExerciseResponse.from(exercise);
    }

    @Transactional
    public void delete(Long trainerId, Long exerciseId) {
        Exercise exercise = getOwnedByTrainer(exerciseId, trainerId);
        if (workoutExerciseRepository.existsByExercise_Id(exerciseId)) {
            throw new ConflictException("Exercise is used in a workout or program and cannot be deleted");
        }
        exerciseRepository.delete(exercise);
    }

    /** Тёзки в каталоге не различить на глаз, поэтому одноимённое упражнение заводить нельзя. */
    private void requireNameIsFree(String name, Long trainerId, Long excludeId) {
        if (exerciseRepository.existsVisibleWithName(name, trainerId, excludeId)) {
            throw new ConflictException("An exercise with this name already exists");
        }
    }

    private Exercise getOwnedByTrainer(Long exerciseId, Long trainerId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
        if (exercise.getCreatedBy() == null || !exercise.getCreatedBy().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your exercise");
        }
        return exercise;
    }
}
