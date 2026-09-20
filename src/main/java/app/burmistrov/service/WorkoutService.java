package app.burmistrov.service;

import app.burmistrov.domain.TrainingProgram;
import app.burmistrov.domain.User;
import app.burmistrov.domain.Workout;
import app.burmistrov.domain.WorkoutExercise;
import app.burmistrov.dto.WorkoutRequest;
import app.burmistrov.dto.WorkoutResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.UserRepository;
import app.burmistrov.repository.WorkoutExerciseRepository;
import app.burmistrov.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Workouts in both of the places they live: the trainer's catalog (no program) and the days of a
 * training program. They are the same rows in the same table, so ownership, exercises and
 * scheduling all work identically — only the listing differs.
 */
@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final UserRepository userRepository;
    private final TrainingProgramService trainingProgramService;

    // ----- catalog -----

    @Transactional
    public WorkoutResponse create(Long trainerId, WorkoutRequest request) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        Workout workout = workoutRepository.save(Workout.builder()
                .trainer(trainer)
                .name(request.name())
                .description(request.description())
                .build());

        copyExercisesFromSource(request.sourceWorkoutId(), trainerId, workout);
        return WorkoutResponse.from(workout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> listForTrainer(Long trainerId) {
        return workoutRepository.findByTrainer_IdAndTrainingProgramIsNull(trainerId).stream()
                .map(WorkoutResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponse get(Long workoutId, Long trainerId) {
        return WorkoutResponse.from(getOwnedByTrainer(workoutId, trainerId));
    }

    @Transactional
    public WorkoutResponse update(Long workoutId, Long trainerId, WorkoutRequest request) {
        Workout workout = getOwnedByTrainer(workoutId, trainerId);
        applyEdits(workout, request);
        return WorkoutResponse.from(workout);
    }

    @Transactional
    public void delete(Long workoutId, Long trainerId) {
        workoutRepository.delete(getOwnedByTrainer(workoutId, trainerId));
    }

    // ----- days of a program -----

    @Transactional
    public WorkoutResponse createInProgram(Long programId, Long trainerId, WorkoutRequest request) {
        TrainingProgram program = trainingProgramService.getOwnedByTrainer(programId, trainerId);
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        Workout workout = workoutRepository.save(Workout.builder()
                .trainer(trainer)
                .trainingProgram(program)
                .name(request.name())
                .description(request.description())
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : 0)
                .build());

        copyExercisesFromSource(request.sourceWorkoutId(), trainerId, workout);
        return WorkoutResponse.from(workout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> listForProgram(Long programId, Long trainerId) {
        trainingProgramService.getOwnedByTrainer(programId, trainerId);
        return workoutRepository.findByTrainingProgram_IdOrderByOrderIndexAsc(programId).stream()
                .map(WorkoutResponse::from)
                .toList();
    }

    @Transactional
    public WorkoutResponse updateInProgram(Long programId, Long workoutId, Long trainerId, WorkoutRequest request) {
        trainingProgramService.getOwnedByTrainer(programId, trainerId);
        Workout workout = requireInProgram(programId, workoutId);
        applyEdits(workout, request);
        return WorkoutResponse.from(workout);
    }

    @Transactional
    public void deleteFromProgram(Long programId, Long workoutId, Long trainerId) {
        trainingProgramService.getOwnedByTrainer(programId, trainerId);
        workoutRepository.delete(requireInProgram(programId, workoutId));
    }

    // ----- shared -----

    @Transactional(readOnly = true)
    public Workout getOwnedByTrainer(Long workoutId, Long trainerId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));
        if (!workout.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your workout");
        }
        return workout;
    }

    private void applyEdits(Workout workout, WorkoutRequest request) {
        workout.setName(request.name());
        workout.setDescription(request.description());
        if (request.orderIndex() != null && workout.belongsToProgram()) {
            workout.setOrderIndex(request.orderIndex());
        }
    }

    /**
     * Seeds a new workout from an existing one by copying its exercise positions. A copy, not a
     * reference: editing either workout afterwards leaves the other untouched.
     */
    private void copyExercisesFromSource(Long sourceWorkoutId, Long trainerId, Workout target) {
        if (sourceWorkoutId == null) {
            return;
        }
        Workout source = getOwnedByTrainer(sourceWorkoutId, trainerId);
        for (WorkoutExercise we : workoutExerciseRepository.findByWorkout_IdOrderByOrderIndexAsc(source.getId())) {
            workoutExerciseRepository.save(WorkoutExercise.builder()
                    .workout(target)
                    .exercise(we.getExercise())
                    .orderIndex(we.getOrderIndex())
                    .notes(we.getNotes())
                    .build());
        }
    }

    private Workout requireInProgram(Long programId, Long workoutId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));
        if (!workout.belongsToProgram() || !workout.getTrainingProgram().getId().equals(programId)) {
            throw new ResourceNotFoundException("Workout not found in this program");
        }
        return workout;
    }
}
