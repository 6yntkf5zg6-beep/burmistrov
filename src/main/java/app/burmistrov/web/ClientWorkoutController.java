package app.burmistrov.web;

import app.burmistrov.dto.ClientWorkoutCardResponse;
import app.burmistrov.dto.ClientWorkoutOpenRequest;
import app.burmistrov.dto.ClientWorkoutResponse;
import app.burmistrov.dto.ExerciseCommentRequest;
import app.burmistrov.dto.ExerciseCommentResponse;
import app.burmistrov.security.UserPrincipal;
import app.burmistrov.service.ClientWorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * Тренировки глазами клиента. Отдельный контроллер, а не методы в тренерском: там всё построено
 * вокруг {@code trainerId} из принципала, а здесь всё вокруг клиента и окна доступа.
 */
@RestController
@RequestMapping("/api/my/workouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientWorkoutController {

    private final ClientWorkoutService clientWorkoutService;

    @GetMapping
    public List<ClientWorkoutCardResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return clientWorkoutService.list(principal.getId());
    }

    /** Вес обязателен: без него тренировка не открывается. */
    @PostMapping("/{id}/open")
    public ClientWorkoutResponse open(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ClientWorkoutOpenRequest request) {
        return clientWorkoutService.open(principal.getId(), id, request.weightKg());
    }

    @GetMapping("/{id}")
    public ClientWorkoutResponse read(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return clientWorkoutService.read(principal.getId(), id);
    }

    /** Пустой текст удаляет комментарий — тогда ответ приходит без тела. */
    @PostMapping("/{id}/exercises/{uid}/comments")
    public ResponseEntity<ExerciseCommentResponse> comment(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable Long id,
                                                           @PathVariable String uid,
                                                           @Valid @RequestBody ExerciseCommentRequest request) {
        ExerciseCommentResponse saved = clientWorkoutService.comment(principal.getId(), id, uid, request.text());
        return saved != null ? ResponseEntity.ok(saved) : ResponseEntity.noContent().build();
    }
}
