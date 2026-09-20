package app.burmistrov.web;

import app.burmistrov.dto.ExerciseRequest;
import app.burmistrov.dto.ExerciseResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.ExerciseService;
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

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public List<ExerciseResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return exerciseService.list(principal.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ExerciseResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ExerciseResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                    @Valid @RequestBody ExerciseRequest request) {
        return exerciseService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        exerciseService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
