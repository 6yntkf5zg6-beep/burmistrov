package app.burmistrov.web;

import app.burmistrov.dto.AssignProgramRequest;
import app.burmistrov.dto.ScheduledWorkoutDateRequest;
import app.burmistrov.dto.ScheduledWorkoutDocumentRequest;
import app.burmistrov.dto.ScheduledWorkoutRequest;
import app.burmistrov.dto.ScheduledWorkoutResponse;
import app.burmistrov.dto.WorkoutAttendanceResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.ScheduledWorkoutService;
import app.burmistrov.service.WorkoutAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-workouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class ScheduledWorkoutController {

    private final ScheduledWorkoutService scheduledWorkoutService;
    private final WorkoutAttendanceService workoutAttendanceService;

    @PostMapping
    public ResponseEntity<ScheduledWorkoutResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody ScheduledWorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduledWorkoutService.create(principal.getId(), request));
    }

    /** Без {@code clientId} отдаёт дни всех клиентов — общий календарь кабинета. */
    @GetMapping
    public List<ScheduledWorkoutResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam(required = false) Long clientId) {
        return clientId != null
                ? scheduledWorkoutService.listForClient(principal.getId(), clientId)
                : scheduledWorkoutService.listForTrainer(principal.getId());
    }

    @PutMapping("/{id}")
    public ScheduledWorkoutResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                           @Valid @RequestBody ScheduledWorkoutDocumentRequest request) {
        return scheduledWorkoutService.updateDocument(principal.getId(), id, request.workout());
    }

    @PostMapping("/assign-program")
    public List<ScheduledWorkoutResponse> assignProgram(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody AssignProgramRequest request) {
        return scheduledWorkoutService.assignProgram(principal.getId(), request);
    }

    /** Переносит день на другую дату — пока тренировку не открывали. */
    @PutMapping("/{id}/date")
    public ScheduledWorkoutResponse move(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ScheduledWorkoutDateRequest request) {
        return scheduledWorkoutService.moveToDate(principal.getId(), id, request.date());
    }

    /** Отмечает посещение вручную: клиент был, но тренировку не открывал. Окно не появляется. */
    @PostMapping("/{id}/attendance")
    public WorkoutAttendanceResponse markAttended(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long id) {
        return WorkoutAttendanceResponse.from(workoutAttendanceService.markAttended(principal.getId(), id));
    }

    /**
     * Стирает запись о посещении целиком. Для сегодняшнего дня это «переоткрыть»: клиент
     * откроет тренировку заново, и три часа пойдут с его касания.
     */
    @DeleteMapping("/{id}/attendance")
    public ResponseEntity<Void> resetAttendance(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id) {
        workoutAttendanceService.reset(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        scheduledWorkoutService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
