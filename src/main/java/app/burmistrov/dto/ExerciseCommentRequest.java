package app.burmistrov.dto;

import jakarta.validation.constraints.Size;

/** Пустой текст — это не ошибка, а просьба убрать комментарий. */
public record ExerciseCommentRequest(
        @Size(max = 2000) String text
) {
}
