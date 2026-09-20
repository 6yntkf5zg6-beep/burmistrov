package app.burmistrov.service;

import app.burmistrov.domain.ExerciseComment;
import app.burmistrov.domain.ScheduledWorkout;
import app.burmistrov.domain.WorkoutAttendance;
import app.burmistrov.domain.WorkoutDocument;
import app.burmistrov.dto.ClientWorkoutCardResponse;
import app.burmistrov.dto.ClientWorkoutResponse;
import app.burmistrov.dto.ExerciseCommentResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ClientProfileRepository;
import app.burmistrov.repository.ExerciseCommentRepository;
import app.burmistrov.repository.ScheduledWorkoutRepository;
import app.burmistrov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Кабинет клиента: что он видит в списке и что получает, открыв тренировку.
 *
 * <p>Граница проходит здесь: список отдаёт только карточки, а упражнения уходят клиенту
 * единственным способом — в ответ на открытие или чтение живого окна.
 */
@Service
@RequiredArgsConstructor
public class ClientWorkoutService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final ExerciseCommentRepository commentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final WorkoutAttendanceService attendanceService;

    @Transactional(readOnly = true)
    public List<ClientWorkoutCardResponse> list(Long clientId) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByClient_IdOrderByScheduledDateAsc(clientId);
        Map<Long, WorkoutAttendance> attendance =
                attendanceService.byWorkoutIds(workouts.stream().map(ScheduledWorkout::getId).toList());
        Instant now = Instant.now();

        return workouts.stream()
                .map(workout -> {
                    WorkoutAttendance record = attendance.get(workout.getId());
                    return new ClientWorkoutCardResponse(
                            workout.getId(),
                            workout.getScheduledDate(),
                            workout.getWorkout() != null ? workout.getWorkout().name() : null,
                            workout.getWorkout() != null ? workout.getWorkout().exercises().size() : 0,
                            attendanceService.stateOf(workout, record, now),
                            record != null ? record.getAttendedAt() : null,
                            record != null ? record.getExpiresAt() : null,
                            record != null ? record.getWeightKg() : null);
                })
                .toList();
    }

    /**
     * Открыть тренировку: проверки внутри, наружу уходит уже содержимое.
     *
     * <p>Названный вес попадает и в профиль: там он значит «сколько клиент весит сейчас», и
     * тренер смотрит именно туда. История же остаётся за заходами.
     */
    @Transactional
    public ClientWorkoutResponse open(Long clientId, Long scheduledWorkoutId, BigDecimal weightKg) {
        WorkoutAttendance attendance = attendanceService.openByClient(clientId, scheduledWorkoutId, weightKg);
        clientProfileRepository.findByUser_Id(clientId).ifPresent(profile -> profile.setWeightKg(weightKg));
        return full(requireWorkout(scheduledWorkoutId), attendance);
    }

    /** Прочитать уже открытую: содержимое живёт ровно столько, сколько живёт окно. */
    @Transactional
    public ClientWorkoutResponse read(Long clientId, Long scheduledWorkoutId) {
        ScheduledWorkout workout = attendanceService.readByClient(clientId, scheduledWorkoutId);
        return full(workout, attendanceService.find(scheduledWorkoutId));
    }

    /**
     * Комментарий к упражнению. Единственное, что клиент может записать, и только пока окно живо.
     *
     * <p>Комментарий один на упражнение: повторная отправка правит его, а не добавляет ещё один.
     * Имя упражнения переснимаем — тренер мог переименовать его между заходами.
     *
     * @return {@code null}, если текст пуст: стереть написанное — это тоже действие, и отдельной
     *         кнопки для него не нужно.
     */
    @Transactional
    public ExerciseCommentResponse comment(Long clientId, Long scheduledWorkoutId, String exerciseUid, String text) {
        ScheduledWorkout workout = attendanceService.readByClient(clientId, scheduledWorkoutId);
        WorkoutDocument.Exercise exercise = workout.getWorkout().exercises().stream()
                .filter(candidate -> exerciseUid.equals(candidate.uid()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("This workout has no such exercise"));

        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            commentRepository.findByScheduledWorkoutIdAndExerciseUid(scheduledWorkoutId, exercise.uid())
                    .ifPresent(commentRepository::delete);
            return null;
        }

        ExerciseComment comment = commentRepository
                .findByScheduledWorkoutIdAndExerciseUid(scheduledWorkoutId, exercise.uid())
                .orElseGet(() -> ExerciseComment.builder()
                        .scheduledWorkoutId(scheduledWorkoutId)
                        .exerciseUid(exercise.uid())
                        .author(userRepository.getReferenceById(clientId))
                        .build());

        comment.setExerciseName(exercise.name());
        comment.setText(trimmed);

        return ExerciseCommentResponse.from(commentRepository.save(comment));
    }

    private ClientWorkoutResponse full(ScheduledWorkout workout, WorkoutAttendance attendance) {
        return new ClientWorkoutResponse(
                workout.getId(),
                workout.getScheduledDate(),
                workout.getWorkout() != null ? workout.getWorkout().name() : null,
                workout.getWorkout(),
                attendance != null ? attendance.getExpiresAt() : null,
                commentRepository.findByScheduledWorkoutIdOrderByCreatedAtAsc(workout.getId()).stream()
                        .map(ExerciseCommentResponse::from)
                        .toList());
    }

    private ScheduledWorkout requireWorkout(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled workout not found"));
    }
}
