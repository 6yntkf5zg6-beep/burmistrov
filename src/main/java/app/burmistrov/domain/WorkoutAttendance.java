package app.burmistrov.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Доступ клиента к назначенной тренировке и факт её посещения — одна строка на тренировку.
 *
 * <p>Две вещи здесь намеренно разведены. {@code attendedAt} — факт: клиент зашёл, и это уже
 * не меняется. {@code openedAt} и {@code expiresAt} — текущее окно доступа, тренер сдвигает
 * его переоткрытием. Если бы переоткрытие двигало и факт, тренер, открывший окно вечером,
 * затирал бы время, когда клиент реально был.
 *
 * <p>Ссылка на тренировку мягкая, а имя и дата продублированы снимком: тренер может удалить
 * тренировку, но посещение должно остаться в истории.
 */
@Entity
@Table(name = "workout_attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutAttendance {

    /** Кто отметил посещение: сам клиент, открыв тренировку, или тренер вручную. */
    public enum Source {
        CLIENT, TRAINER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduled_workout_id", nullable = false, unique = true)
    private Long scheduledWorkoutId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "workout_name", nullable = false)
    private String workoutName;

    /** Момент первого захода. Переоткрытием не сдвигается. */
    @Column(name = "attended_at")
    private Instant attendedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_source", length = 16)
    private Source attendanceSource;

    /** Вес клиента на момент открытия тренировки — точка графика в статистике. */
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /** Посещение есть тогда и только тогда, когда клиент зашёл или тренер это отметил. */
    public boolean attended() {
        return attendedAt != null;
    }

    public boolean windowIsLive(Instant now) {
        return expiresAt != null && now.isBefore(expiresAt);
    }

    /** Окно было и закончилось — в отличие от «окна не было вовсе». */
    public boolean windowIsOver(Instant now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }
}
