package app.burmistrov.web;

import app.burmistrov.dto.WorkoutRequest;
import app.burmistrov.dto.WorkoutResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.WorkoutService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The days of one training program. Same workouts as {@link WorkoutController} serves — the only
 * difference is that these carry a program and a position in it.
 */
@RestController
@RequestMapping("/api/training-programs/{programId}/workouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class ProgramWorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<WorkoutResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long programId,
                                                  @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.createInProgram(programId, principal.getId(), request));
    }

    @GetMapping
    public List<WorkoutResponse> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long programId) {
        return workoutService.listForProgram(programId, principal.getId());
    }

    @PutMapping("/{workoutId}")
    public WorkoutResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long programId,
                                  @PathVariable Long workoutId, @Valid @RequestBody WorkoutRequest request) {
        return workoutService.updateInProgram(programId, workoutId, principal.getId(), request);
    }

    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long programId,
                                       @PathVariable Long workoutId) {
        workoutService.deleteFromProgram(programId, workoutId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
