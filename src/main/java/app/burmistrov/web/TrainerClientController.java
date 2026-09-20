package app.burmistrov.web;

import app.burmistrov.domain.User;
import app.burmistrov.dto.AddClientRequest;
import app.burmistrov.dto.TrainerClientResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.TrainerClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainer-clients")
@RequiredArgsConstructor
public class TrainerClientController {

    private final TrainerClientService trainerClientService;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<TrainerClientResponse> invite(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody AddClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerClientService.invite(principal.getId(), request));
    }

    @GetMapping
    public List<TrainerClientResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return principal.getRole() == User.Role.TRAINER
                ? trainerClientService.listForTrainer(principal.getId())
                : trainerClientService.listForClient(principal.getId());
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public TrainerClientResponse accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return trainerClientService.accept(id, principal.getId());
    }

    @PatchMapping("/{id}/archive")
    public TrainerClientResponse archive(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return trainerClientService.archive(id, principal.getId());
    }
}
