package app.burmistrov.web;

import app.burmistrov.dto.UserSummary;
import app.burmistrov.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public UserSummary me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserSummary(principal.getId(), principal.getUsername(), principal.getFirstName(),
                principal.getLastName(), principal.getRole());
    }
}
