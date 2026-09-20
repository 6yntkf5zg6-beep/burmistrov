package app.burmistrov.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Открывая тренировку, клиент называет свой вес — без него открыть нельзя. */
public record ClientWorkoutOpenRequest(
        @NotNull
        @DecimalMin(value = "20.0", message = "вес должен быть не меньше 20 кг")
        @DecimalMax(value = "400.0", message = "вес должен быть не больше 400 кг")
        BigDecimal weightKg
) {
}
