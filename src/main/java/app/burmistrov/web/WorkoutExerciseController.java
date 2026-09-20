package app.burmistrov.web;

import app.burmistrov.dto.WorkoutExerciseRequest;
import app.burmistrov.dto.WorkoutExerciseResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.WorkoutExerciseService;
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

/** Exercises of a workout — one set of endpoints for catalog workouts and program days alike. */
@RestController
@RequestMapping("/api/workouts/{workoutId}/exercises")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class WorkoutExerciseController {

    private final WorkoutExerciseService workoutExerciseService;

    @PostMapping
    public ResponseEntity<WorkoutExerciseResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long workoutId,
                                                       @Valid @RequestBody WorkoutExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutExerciseService.add(workoutId, principal.getId(), request));
    }

    @GetMapping
    public List<WorkoutExerciseResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable Long workoutId) {
        return workoutExerciseService.list(workoutId, principal.getId());
    }

    @PutMapping("/{id}")
    public WorkoutExerciseResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long workoutId, @PathVariable Long id,
                                          @Valid @RequestBody WorkoutExerciseRequest request) {
        return workoutExerciseService.update(workoutId, id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long workoutId, @PathVariable Long id) {
        workoutExerciseService.delete(workoutId, id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
