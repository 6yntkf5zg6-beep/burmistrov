package app.burmistrov.web;

import app.burmistrov.dto.CreateInviteRequest;
import app.burmistrov.dto.InviteCheckResponse;
import app.burmistrov.dto.InviteResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<InviteResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @Valid @RequestBody CreateInviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteService.create(principal.getId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('TRAINER')")
    public List<InviteResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return inviteService.listForTrainer(principal.getId());
    }

    @GetMapping("/check/{token}")
    public InviteCheckResponse check(@PathVariable String token) {
        return inviteService.check(token);
    }
}
