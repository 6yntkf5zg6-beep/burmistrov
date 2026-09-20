package app.burmistrov.web;

import app.burmistrov.dto.ProgramAssignmentResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.ScheduledWorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** История назначений программ клиенту. */
@RestController
@RequestMapping("/api/program-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class ProgramAssignmentController {

    private static final int DEFAULT_MONTHS = 3;

    private final ScheduledWorkoutService scheduledWorkoutService;

    @GetMapping
    public List<ProgramAssignmentResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam Long clientId,
                                                @RequestParam(defaultValue = "" + DEFAULT_MONTHS) int months) {
        return scheduledWorkoutService.listAssignments(principal.getId(), clientId, months);
    }
}
