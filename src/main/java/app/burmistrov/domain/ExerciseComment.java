package app.burmistrov.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Комментарий клиента к упражнению конкретного дня.
 *
 * <p>Лежит отдельно от документа тренировки сознательно: документ пишет тренер, и если бы
 * клиент писал в ту же строку, правка дня во время тренировки затирала бы комментарий.
 * Комментарии только добавляются, у каждого свой автор и время.
 *
 * <p>Упражнение опознаётся по {@code exerciseUid} внутри документа, а имя хранится снимком —
 * тренер может убрать упражнение из дня, но комментарий должен остаться читаемым.
 */
@Entity
@Table(name = "workout_exercise_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduled_workout_id", nullable = false)
    private Long scheduledWorkoutId;

    @Column(name = "exercise_uid", nullable = false, length = 36)
    private String exerciseUid;

    @Column(name = "exercise_name", nullable = false)
    private String exerciseName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
