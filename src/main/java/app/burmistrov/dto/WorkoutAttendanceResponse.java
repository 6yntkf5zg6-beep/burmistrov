package app.burmistrov.dto;

import app.burmistrov.domain.WorkoutAttendance;

import java.time.Instant;

/**
 * Посещение и окно доступа глазами тренера. {@code attendedAt} — факт визита, он не двигается;
 * {@code expiresAt} — текущее окно, которое тренер может сдвинуть переоткрытием.
 */
public record WorkoutAttendanceResponse(
        Instant attendedAt,
        WorkoutAttendance.Source attendanceSource,
        Instant openedAt,
        Instant expiresAt
) {
    public static WorkoutAttendanceResponse from(WorkoutAttendance attendance) {
        return attendance == null ? null : new WorkoutAttendanceResponse(
                attendance.getAttendedAt(),
                attendance.getAttendanceSource(),
                attendance.getOpenedAt(),
                attendance.getExpiresAt());
    }
}
