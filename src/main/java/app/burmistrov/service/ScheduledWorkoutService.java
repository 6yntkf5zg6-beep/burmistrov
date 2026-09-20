package app.burmistrov.service;

import app.burmistrov.domain.ProgramAssignment;
import app.burmistrov.domain.ExerciseComment;
import app.burmistrov.domain.ScheduledWorkout;
import app.burmistrov.domain.WorkoutAttendance;
import app.burmistrov.domain.TrainingProgram;
import app.burmistrov.domain.User;
import app.burmistrov.domain.Workout;
import app.burmistrov.domain.WorkoutDocument;
import app.burmistrov.dto.AssignProgramRequest;
import app.burmistrov.dto.ProgramAssignmentResponse;
import app.burmistrov.dto.ScheduledWorkoutRequest;
import app.burmistrov.dto.ExerciseCommentResponse;
import app.burmistrov.dto.ScheduledWorkoutResponse;
import app.burmistrov.dto.WorkoutAttendanceResponse;
import app.burmistrov.exception.ConflictException;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ExerciseCommentRepository;
import app.burmistrov.repository.ProgramAssignmentRepository;
import app.burmistrov.repository.ScheduledWorkoutRepository;
import app.burmistrov.repository.UserRepository;
import app.burmistrov.repository.WorkoutExerciseRepository;
import app.burmistrov.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledWorkoutService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final ProgramAssignmentRepository programAssignmentRepository;
    private final ExerciseCommentRepository exerciseCommentRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final TrainerClientService trainerClientService;
    private final WorkoutService workoutService;
    private final TrainingProgramService trainingProgramService;
    private final WorkoutAttendanceService workoutAttendanceService;

    @Transactional
    public ScheduledWorkoutResponse create(Long trainerId, ScheduledWorkoutRequest request) {
        requireActiveClient(trainerId, request.clientId());

        WorkoutDocument document = request.workout();
        if (document == null) {
            if (request.sourceWorkoutId() == null) {
                throw new ConflictException("Either a workout to copy or a workout document is required");
            }
            document = snapshotOf(workoutService.getOwnedByTrainer(request.sourceWorkoutId(), trainerId));
        } else if (request.sourceWorkoutId() != null) {
            // only to fail early if the id is not the trainer's own
            workoutService.getOwnedByTrainer(request.sourceWorkoutId(), trainerId);
        }

        ScheduledWorkout scheduledWorkout = scheduledWorkoutRepository.save(ScheduledWorkout.builder()
                .trainer(userRepository.getReferenceById(trainerId))
                .client(userRepository.getReferenceById(request.clientId()))
                .sourceWorkoutId(request.sourceWorkoutId())
                .workout(document)
                .scheduledDate(request.scheduledDate())
                .build());

        return ScheduledWorkoutResponse.from(scheduledWorkout);
    }

    /**
     * Replaces the workout of one day. This is what makes a scheduled workout editable on its own:
     * exercises can be added, dropped or reordered without the template noticing.
     */
    @Transactional
    public ScheduledWorkoutResponse updateDocument(Long trainerId, Long id, WorkoutDocument document) {
        ScheduledWorkout scheduledWorkout = getOwnedByTrainer(trainerId, id);
        // Пока окно живо, клиент смотрит именно этот документ. Менять упражнения у него под
        // руками нельзя — тренер подождёт три часа или закроет окно сам.
        if (workoutAttendanceService.windowIsLive(id)) {
            throw new ConflictException("The client is training right now — the workout cannot be edited");
        }
        scheduledWorkout.setWorkout(document);
        return ScheduledWorkoutResponse.from(scheduledWorkout);
    }

    @Transactional
    public List<ScheduledWorkoutResponse> assignProgram(Long trainerId, AssignProgramRequest request) {
        requireActiveClient(trainerId, request.clientId());
        TrainingProgram program = trainingProgramService.getOwnedByTrainer(request.programId(), trainerId);
        List<Workout> workouts = workoutRepository.findByTrainingProgram_IdOrderByOrderIndexAsc(program.getId());

        User trainer = userRepository.getReferenceById(trainerId);
        User client = userRepository.getReferenceById(request.clientId());

        if (workouts.isEmpty()) {
            throw new ConflictException("The program has no workouts to assign");
        }
        Map<Long, Workout> byId = workouts.stream().collect(Collectors.toMap(Workout::getId, w -> w));

        List<ScheduledWorkout> created = request.placements().stream()
                .map(placement -> {
                    Workout workout = byId.get(placement.workoutId());
                    if (workout == null) {
                        throw new ResourceNotFoundException(
                                "Workout " + placement.workoutId() + " is not part of this program");
                    }
                    return ScheduledWorkout.builder()
                            .trainer(trainer)
                            .client(client)
                            .sourceWorkoutId(workout.getId())
                            .workout(snapshotOf(workout))
                            .scheduledDate(placement.date())
                            .build();
                })
                .toList();

        List<ScheduledWorkoutResponse> scheduled = scheduledWorkoutRepository.saveAll(created).stream()
                .map(ScheduledWorkoutResponse::from)
                .toList();

        recordAssignment(trainer, client, program, workouts, request);
        return scheduled;
    }

    /**
     * Сохраняет само назначение как событие. Из строк расписания его не восстановить: они не
     * помнят ни того, что назначались одной программой, ни того, какие тренировки тренер убрал.
     */
    private void recordAssignment(User trainer, User client, TrainingProgram program,
                                  List<Workout> workouts, AssignProgramRequest request) {
        Map<Long, LocalDate> datesByWorkout = request.placements().stream()
                .collect(Collectors.toMap(AssignProgramRequest.Placement::workoutId,
                        AssignProgramRequest.Placement::date, (first, second) -> first));

        List<ProgramAssignment.AssignedWorkout> snapshot = workouts.stream()
                .map(workout -> {
                    LocalDate date = datesByWorkout.get(workout.getId());
                    return new ProgramAssignment.AssignedWorkout(
                            workout.getId(), workout.getName(), workout.getOrderIndex(), date, date != null);
                })
                .toList();

        programAssignmentRepository.save(ProgramAssignment.builder()
                .trainer(trainer)
                .client(client)
                .sourceProgramId(program.getId())
                .programName(program.getName())
                .workouts(ProgramAssignment.AssignedWorkouts.of(snapshot))
                .build());
    }

    /** История назначений за последние {@code months} месяцев, свежие сверху. */
    @Transactional(readOnly = true)
    public List<ProgramAssignmentResponse> listAssignments(Long trainerId, Long clientId, int months) {
        requireActiveClient(trainerId, clientId);
        Instant since = Instant.now().minus(Duration.ofDays(30L * months));
        return programAssignmentRepository
                .findByTrainer_IdAndClient_IdAndAssignedAtAfterOrderByAssignedAtDesc(trainerId, clientId, since)
                .stream()
                .map(ProgramAssignmentResponse::from)
                .toList();
    }

    /**
     * Дни клиента вместе с посещениями и комментариями: календарю и истории иначе пришлось бы
     * ходить на сервер трижды.
     */
    @Transactional(readOnly = true)
    public List<ScheduledWorkoutResponse> listForClient(Long trainerId, Long clientId) {
        requireActiveClient(trainerId, clientId);
        return withAttendanceAndComments(scheduledWorkoutRepository
                .findByTrainer_IdAndClient_IdOrderByScheduledDateAsc(trainerId, clientId));
    }

    /** Все дни тренера — общий календарь кабинета, где клиенты идут вперемешку. */
    @Transactional(readOnly = true)
    public List<ScheduledWorkoutResponse> listForTrainer(Long trainerId) {
        return withAttendanceAndComments(
                scheduledWorkoutRepository.findByTrainer_IdOrderByScheduledDateAsc(trainerId));
    }

    private List<ScheduledWorkoutResponse> withAttendanceAndComments(List<ScheduledWorkout> workouts) {
        List<Long> ids = workouts.stream().map(ScheduledWorkout::getId).toList();

        Map<Long, WorkoutAttendance> attendance = workoutAttendanceService.byWorkoutIds(ids);
        Map<Long, List<ExerciseCommentResponse>> comments = ids.isEmpty() ? Map.of()
                : exerciseCommentRepository.findByScheduledWorkoutIdInOrderByCreatedAtAsc(ids).stream()
                        .collect(Collectors.groupingBy(ExerciseComment::getScheduledWorkoutId,
                                Collectors.mapping(ExerciseCommentResponse::from, Collectors.toList())));

        return workouts.stream()
                .map(workout -> ScheduledWorkoutResponse.from(
                        workout,
                        WorkoutAttendanceResponse.from(attendance.get(workout.getId())),
                        comments.getOrDefault(workout.getId(), List.of())))
                .toList();
    }

    /**
     * Переносит день на другую дату.
     *
     * <p>Только пока тренировку не открывали: посещение всегда относится к дате, в которую оно
     * случилось, и таскать его следом было бы враньём. Если запись о посещении мешает, тренер
     * сначала сбрасывает её.
     */
    @Transactional
    public ScheduledWorkoutResponse moveToDate(Long trainerId, Long id, LocalDate date) {
        ScheduledWorkout scheduledWorkout = getOwnedByTrainer(trainerId, id);
        if (workoutAttendanceService.find(id) != null) {
            throw new ConflictException("This workout has already been opened — reset it before moving");
        }
        if (date.isBefore(workoutAttendanceService.today())) {
            throw new ConflictException("A workout cannot be moved into the past");
        }
        scheduledWorkout.setScheduledDate(date);
        return ScheduledWorkoutResponse.from(scheduledWorkout);
    }

    @Transactional
    public void delete(Long trainerId, Long id) {
        ScheduledWorkout scheduledWorkout = getOwnedByTrainer(trainerId, id);
        Long clientId = scheduledWorkout.getClient().getId();
        scheduledWorkoutRepository.delete(scheduledWorkout);
        scheduledWorkoutRepository.flush();
        pruneEmptyAssignments(trainerId, clientId);
    }

    /**
     * Назначение живёт, пока в расписании осталась хоть одна его тренировка. Когда тренер удалил
     * последнюю, показывать в истории нечего — и хранить незачем. Отмену никто не записывает
     * отдельным событием, поэтому состав назначения сверяем с тем, что реально есть в расписании:
     * ключ — исходная тренировка плюс дата.
     */
    private void pruneEmptyAssignments(Long trainerId, Long clientId) {
        List<ProgramAssignment> assignments =
                programAssignmentRepository.findByTrainer_IdAndClient_Id(trainerId, clientId);
        if (assignments.isEmpty()) {
            return;
        }

        Set<String> remaining = scheduledWorkoutRepository
                .findByTrainer_IdAndClient_IdOrderByScheduledDateAsc(trainerId, clientId).stream()
                .filter(sw -> sw.getSourceWorkoutId() != null)
                .map(sw -> placementKey(sw.getSourceWorkoutId(), sw.getScheduledDate()))
                .collect(Collectors.toSet());

        List<ProgramAssignment> empty = assignments.stream()
                .filter(assignment -> !hasLiveWorkout(assignment, remaining))
                .toList();

        if (!empty.isEmpty()) {
            programAssignmentRepository.deleteAll(empty);
        }
    }

    private boolean hasLiveWorkout(ProgramAssignment assignment, Set<String> remaining) {
        return assignment.getWorkouts().items().stream()
                .filter(ProgramAssignment.AssignedWorkout::included)
                // Без источника или даты сверять не с чем — такое назначение не трогаем.
                .anyMatch(workout -> workout.workoutId() == null || workout.date() == null
                        || remaining.contains(placementKey(workout.workoutId(), workout.date())));
    }

    private static String placementKey(Long workoutId, LocalDate date) {
        return workoutId + "|" + date;
    }

    /** Copies a workout as it is right now — exercises in order, no sets filled in yet. */
    @Transactional(readOnly = true)
    public WorkoutDocument snapshotOf(Workout workout) {
        List<WorkoutDocument.Exercise> exercises = workoutExerciseRepository
                .findByWorkout_IdOrderByOrderIndexAsc(workout.getId()).stream()
                .map(we -> new WorkoutDocument.Exercise(
                        null,
                        we.getExercise().getId(),
                        we.getExercise().getName(),
                        we.getExercise().getImageUrl(),
                        we.getExercise().getVideoUrl(),
                        we.getNotes(),
                        List.of()))
                .toList();

        return new WorkoutDocument(
                WorkoutDocument.CURRENT_VERSION, workout.getName(), workout.getDescription(), exercises);
    }

    private ScheduledWorkout getOwnedByTrainer(Long trainerId, Long id) {
        ScheduledWorkout scheduledWorkout = scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled workout not found"));
        if (!scheduledWorkout.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your scheduled workout");
        }
        return scheduledWorkout;
    }

    private void requireActiveClient(Long trainerId, Long clientId) {
        if (!trainerClientService.isActiveRelation(trainerId, clientId)) {
            throw new AccessDeniedException("No active trainer-client relation with this client");
        }
    }
}
