package app.burmistrov.service;

import app.burmistrov.domain.ScheduledWorkout;
import app.burmistrov.domain.WorkoutAttendance;
import app.burmistrov.exception.ConflictException;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ExerciseCommentRepository;
import app.burmistrov.repository.ScheduledWorkoutRepository;
import app.burmistrov.repository.WorkoutAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Доступ клиента к назначенной тренировке.
 *
 * <p>Правила: открыть можно только в её день и только один раз, дальше окно живёт три часа.
 * Тренер может сдвинуть окно заново или отметить посещение руками, если клиент вообще не заходил.
 *
 * <p>Ни одно состояние не хранится: всё выводится при чтении из даты тренировки, {@code expiresAt}
 * и {@code attendedAt}. Поэтому нет и фоновой задачи, которая «закрывала» бы окна — время проходит
 * само.
 */
@Service
@RequiredArgsConstructor
public class WorkoutAttendanceService {

    /**
     * Где тренировка находится с точки зрения клиента. Ничего из этого не лежит в базе.
     */
    public enum AccessState {
        /** День ещё не наступил. */
        LOCKED,
        /** Сегодня её день, окна ещё не было. */
        AVAILABLE,
        /** Окно живо: содержимое можно показывать. */
        OPEN,
        /** Окно было и закончилось, либо посещение отмечено тренером. */
        CLOSED,
        /** День прошёл, клиент не заходил. */
        MISSED
    }

    private final WorkoutAttendanceRepository attendanceRepository;
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final ExerciseCommentRepository exerciseCommentRepository;
    private final TrainerClientService trainerClientService;

    @Value("${app.gym-timezone}")
    private String gymTimezone;

    @Value("${app.workout-access.duration}")
    private Duration accessDuration;

    /** «Сегодня» считается по поясу зала, а не по поясу сервера и тем более не по часам клиента. */
    public LocalDate today() {
        return LocalDate.now(ZoneId.of(gymTimezone));
    }

    public AccessState stateOf(ScheduledWorkout workout, WorkoutAttendance attendance, Instant now) {
        if (attendance != null && attendance.windowIsLive(now)) {
            return AccessState.OPEN;
        }
        if (attendance != null && (attendance.windowIsOver(now) || attendance.attended())) {
            return AccessState.CLOSED;
        }
        LocalDate today = today();
        if (workout.getScheduledDate().isAfter(today)) {
            return AccessState.LOCKED;
        }
        return workout.getScheduledDate().isEqual(today) ? AccessState.AVAILABLE : AccessState.MISSED;
    }

    @Transactional(readOnly = true)
    public Map<Long, WorkoutAttendance> byWorkoutIds(List<Long> scheduledWorkoutIds) {
        if (scheduledWorkoutIds.isEmpty()) {
            return Map.of();
        }
        return attendanceRepository.findByScheduledWorkoutIdIn(scheduledWorkoutIds).stream()
                .collect(Collectors.toMap(WorkoutAttendance::getScheduledWorkoutId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public WorkoutAttendance find(Long scheduledWorkoutId) {
        return attendanceRepository.findByScheduledWorkoutId(scheduledWorkoutId).orElse(null);
    }

    /** Пока окно живо, тренировку правит только клиент — комментариями. Тренеру придётся подождать. */
    @Transactional(readOnly = true)
    public boolean windowIsLive(Long scheduledWorkoutId) {
        WorkoutAttendance attendance = find(scheduledWorkoutId);
        return attendance != null && attendance.windowIsLive(Instant.now());
    }

    /**
     * Клиент открывает тренировку, назвав свой вес. Вес запоминается за этим заходом: из таких
     * точек потом собирается график «от тренировки к тренировке».
     */
    @Transactional
    public WorkoutAttendance openByClient(Long clientId, Long scheduledWorkoutId, BigDecimal weightKg) {
        ScheduledWorkout workout = requireClientWorkout(clientId, scheduledWorkoutId);
        Instant now = Instant.now();
        WorkoutAttendance attendance = find(scheduledWorkoutId);

        if (attendance != null && attendance.windowIsLive(now)) {
            attendance.setWeightKg(weightKg);
            return markPresence(attendance, now);
        }
        if (attendance != null && attendance.getOpenedAt() != null) {
            throw new ConflictException("This workout has already been opened");
        }
        if (!workout.getScheduledDate().isEqual(today())) {
            throw new ConflictException("A workout can only be opened on the day it is scheduled for");
        }

        WorkoutAttendance opened = attendance != null ? attendance : blank(workout);
        opened.setWeightKg(weightKg);
        opened.setOpenedAt(now);
        opened.setExpiresAt(now.plus(accessDuration));
        return attendanceRepository.save(markPresence(opened, now));
    }

    /** Чтение содержимого клиентом: только пока окно живо. */
    @Transactional
    public ScheduledWorkout readByClient(Long clientId, Long scheduledWorkoutId) {
        ScheduledWorkout workout = requireClientWorkout(clientId, scheduledWorkoutId);
        requireLiveWindow(scheduledWorkoutId);
        return workout;
    }

    /** Общая проверка для всего, что клиенту можно делать только внутри окна. */
    @Transactional
    public WorkoutAttendance requireLiveWindow(Long scheduledWorkoutId) {
        Instant now = Instant.now();
        WorkoutAttendance attendance = find(scheduledWorkoutId);
        if (attendance == null || !attendance.windowIsLive(now)) {
            throw new AccessDeniedException("The access window for this workout is closed");
        }
        return markPresence(attendance, now);
    }

    /** Тренер отмечает посещение руками — окно при этом не открывается. */
    @Transactional
    public WorkoutAttendance markAttended(Long trainerId, Long scheduledWorkoutId) {
        ScheduledWorkout workout = requireTrainerWorkout(trainerId, scheduledWorkoutId);
        WorkoutAttendance attendance = find(scheduledWorkoutId);
        if (attendance == null) {
            attendance = blank(workout);
        }
        if (attendance.attended()) {
            throw new ConflictException("This workout is already marked as attended");
        }
        attendance.setAttendedAt(Instant.now());
        attendance.setAttendanceSource(WorkoutAttendance.Source.TRAINER);
        return attendanceRepository.save(attendance);
    }

    /**
     * Стирает всё, что известно о посещении: и окно, и сам факт.
     *
     * <p>Одна операция закрывает два случая. Если день сегодняшний, тренировка возвращается
     * в «можно открыть» — клиент откроет её сам, и три часа пойдут с его касания, а не с
     * нажатия тренера. Если день прошёл, доступ этим не вернуть (открыть можно только в свой
     * день), но запись снимается — например, когда отметили не ту тренировку.
     *
     * <p>Вместе с записью уходят и комментарии клиента к этому дню: тренировка начинается
     * заново, и оставленное в прошлый заход к ней больше не относится.
     */
    @Transactional
    public void reset(Long trainerId, Long scheduledWorkoutId) {
        requireTrainerWorkout(trainerId, scheduledWorkoutId);
        WorkoutAttendance attendance = find(scheduledWorkoutId);
        if (attendance == null) {
            throw new ConflictException("There is nothing to reset for this workout");
        }
        attendanceRepository.delete(attendance);
        exerciseCommentRepository.deleteByScheduledWorkoutId(scheduledWorkoutId);
    }

    /** Первый заход клиента фиксируется один раз и переоткрытием не сдвигается. */
    private WorkoutAttendance markPresence(WorkoutAttendance attendance, Instant now) {
        if (!attendance.attended()) {
            attendance.setAttendedAt(now);
            attendance.setAttendanceSource(WorkoutAttendance.Source.CLIENT);
        }
        return attendance;
    }

    private WorkoutAttendance blank(ScheduledWorkout workout) {
        return WorkoutAttendance.builder()
                .scheduledWorkoutId(workout.getId())
                .trainer(workout.getTrainer())
                .client(workout.getClient())
                .workoutDate(workout.getScheduledDate())
                .workoutName(workout.getWorkout() != null ? workout.getWorkout().name() : "Тренировка")
                .build();
    }

    private ScheduledWorkout requireClientWorkout(Long clientId, Long scheduledWorkoutId) {
        ScheduledWorkout workout = scheduledWorkoutRepository.findById(scheduledWorkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled workout not found"));
        if (!workout.getClient().getId().equals(clientId)) {
            throw new AccessDeniedException("Not your workout");
        }
        return workout;
    }

    private ScheduledWorkout requireTrainerWorkout(Long trainerId, Long scheduledWorkoutId) {
        ScheduledWorkout workout = scheduledWorkoutRepository.findById(scheduledWorkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled workout not found"));
        if (!workout.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your scheduled workout");
        }
        if (!trainerClientService.isActiveRelation(trainerId, workout.getClient().getId())) {
            throw new AccessDeniedException("No active trainer-client relation with this client");
        }
        return workout;
    }
}
