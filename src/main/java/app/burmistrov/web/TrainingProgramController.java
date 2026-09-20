package app.burmistrov.web;

import app.burmistrov.dto.TrainingProgramRequest;
import app.burmistrov.dto.TrainingProgramResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.TrainingProgramService;
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
@RequestMapping("/api/training-programs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    @PostMapping
    public ResponseEntity<TrainingProgramResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody TrainingProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingProgramService.create(principal.getId(), request));
    }

    @GetMapping
    public List<TrainingProgramResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return trainingProgramService.listForTrainer(principal.getId());
    }

    @GetMapping("/{id}")
    public TrainingProgramResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return trainingProgramService.get(id, principal.getId());
    }

    @PutMapping("/{id}")
    public TrainingProgramResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                           @Valid @RequestBody TrainingProgramRequest request) {
        return trainingProgramService.update(id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        trainingProgramService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
