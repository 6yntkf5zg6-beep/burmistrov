package app.burmistrov.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The whole content of one scheduled workout, stored as a JSONB snapshot on
 * {@link ScheduledWorkout}. It is deliberately a copy rather than a reference: what was assigned
 * on a date must stay exactly that, even after the template it came from is edited or deleted.
 * That also lets the trainer add, drop or reorder exercises for a single day.
 *
 * <p>{@code version} is what makes the shape changeable later — a future migration can tell old
 * documents from new ones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkoutDocument(
        Integer version,
        String name,
        String description,
        List<Exercise> exercises
) {
    public static final int CURRENT_VERSION = 2;

    public WorkoutDocument {
        version = version != null ? version : CURRENT_VERSION;
        exercises = exercises != null ? List.copyOf(exercises) : List.of();
    }

    /**
     * @param uid        устойчивый идентификатор упражнения внутри дня. К нему цепляются
     *                   комментарии клиента: {@code exerciseId} бывает пустым у разового
     *                   упражнения, а порядок тренер меняет перетаскиванием. Если клиент
     *                   прислал документ без uid, он проставляется здесь — так документ
     *                   в базе всегда с идентификаторами.
     * @param exerciseId soft link to the catalog — no foreign key, so the exercise stays free to
     *                   change or disappear; {@code name} is the snapshot that survives it.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Exercise(
            String uid,
            Long exerciseId,
            String name,
            String imageUrl,
            String videoUrl,
            String notes,
            List<SetEntry> sets
    ) {
        public Exercise {
            uid = uid != null && !uid.isBlank() ? uid : UUID.randomUUID().toString();
            sets = sets != null ? List.copyOf(sets) : List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SetEntry(Integer reps, BigDecimal weightKg) {
    }
}
