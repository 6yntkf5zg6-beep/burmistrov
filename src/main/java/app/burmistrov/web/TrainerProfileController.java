package app.burmistrov.web;

import app.burmistrov.dto.TrainerProfileRequest;
import app.burmistrov.dto.TrainerProfileResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.TrainerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer-profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class TrainerProfileController {

    private final TrainerProfileService trainerProfileService;

    @GetMapping("/me")
    public TrainerProfileResponse getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return trainerProfileService.getMine(principal.getId());
    }

    @PutMapping("/me")
    public TrainerProfileResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody TrainerProfileRequest request) {
        return trainerProfileService.update(principal.getId(), request);
    }
}
