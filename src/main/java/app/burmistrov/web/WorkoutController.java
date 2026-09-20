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

/** The trainer's workout catalog. Days of a program are managed under their program instead. */
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<WorkoutResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.create(principal.getId(), request));
    }

    @GetMapping
    public List<WorkoutResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return workoutService.listForTrainer(principal.getId());
    }

    @GetMapping("/{id}")
    public WorkoutResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return workoutService.get(id, principal.getId());
    }

    @PutMapping("/{id}")
    public WorkoutResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                  @Valid @RequestBody WorkoutRequest request) {
        return workoutService.update(id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        workoutService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
