package app.burmistrov.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
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
import java.util.List;

/**
 * Факт назначения программы клиенту. Хранится снимком — как и сама запланированная тренировка:
 * имя программы и её состав на момент назначения, включая тренировки, которые тренер из этого
 * назначения исключил. Правка или удаление программы историю не меняет, поэтому ссылка на неё
 * мягкая, без внешнего ключа.
 */
@Entity
@Table(name = "program_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "source_program_id")
    private Long sourceProgramId;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workouts", nullable = false, columnDefinition = "jsonb")
    private AssignedWorkouts workouts;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    /**
     * Обёртка над списком: Hibernate не умеет отображать в JSON «голый» {@code List<record>} —
     * ему нужен конкретный тип. {@code @JsonValue} и {@code @JsonCreator} оставляют в базе
     * обычный массив, без лишнего объекта-контейнера.
     */
    public record AssignedWorkouts(List<AssignedWorkout> items) {

        @JsonCreator
        public static AssignedWorkouts of(List<AssignedWorkout> items) {
            return new AssignedWorkouts(items != null ? List.copyOf(items) : List.of());
        }

        @JsonValue
        @Override
        public List<AssignedWorkout> items() {
            return items;
        }
    }

    /**
     * Одна тренировка программы в этом назначении. {@code included = false} означает, что тренер
     * убрал её при назначении — тогда {@code date} пуста.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssignedWorkout(
            Long workoutId,
            String name,
            Integer orderIndex,
            LocalDate date,
            boolean included
    ) {
    }
}
