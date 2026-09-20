package app.burmistrov.web;

import app.burmistrov.dto.ClientProfileRequest;
import app.burmistrov.dto.ClientProfileResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.ClientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client-profile")
@RequiredArgsConstructor
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ClientProfileResponse getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return clientProfileService.getMine(principal.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ClientProfileResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ClientProfileRequest request) {
        return clientProfileService.update(principal.getId(), request);
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('TRAINER')")
    public ClientProfileResponse getForTrainer(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long clientId) {
        return clientProfileService.getForTrainer(clientId, principal.getId());
    }
}
