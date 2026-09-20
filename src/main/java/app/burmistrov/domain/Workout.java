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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * A named, ordered list of exercises owned by a trainer — the single shape a workout takes in
 * this system. It lives in one of two places, told apart by {@link #trainingProgram}:
 * <ul>
 *   <li>{@code null} — a standalone workout in the trainer's catalog ("Тренировки"), which can be
 *       put on a client's calendar on its own;</li>
 *   <li>set — one day of that program, ordered by {@link #orderIndex}, placed on the calendar only
 *       when the whole program is assigned.</li>
 * </ul>
 * A workout belongs to at most one program: adding a catalog workout to a program copies it,
 * exactly as it did when the two were separate tables.
 */
@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Null for a catalog workout; set for one day of a program. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_program_id")
    private TrainingProgram trainingProgram;

    /** Position inside {@link #trainingProgram}; null for a catalog workout. */
    @Column(name = "order_index")
    private Integer orderIndex;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean belongsToProgram() {
        return trainingProgram != null;
    }
}
