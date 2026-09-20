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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

/**
 * One workout on one date for one client. The workout itself is a {@link WorkoutDocument}
 * snapshot rather than a link, so the client's history never changes under them when a template
 * is edited or removed.
 */
@Entity
@Table(name = "scheduled_workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /** Which workout this grew from. A soft link with no foreign key — kept for stats only. */
    @Column(name = "source_workout_id")
    private Long sourceWorkoutId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workout", nullable = false, columnDefinition = "jsonb")
    private WorkoutDocument workout;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
