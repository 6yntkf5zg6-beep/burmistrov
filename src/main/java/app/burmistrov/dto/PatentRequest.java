package app.burmistrov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Патент задаётся сканом: номер и название на карточке не показываются. */
public record PatentRequest(@NotBlank @Size(max = 500) String scanUrl) {
}
