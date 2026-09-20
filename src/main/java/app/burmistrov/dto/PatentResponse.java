package app.burmistrov.dto;

import app.burmistrov.domain.Patent;

public record PatentResponse(Long id, String number, String title, String scanUrl, Integer sortOrder) {

    public static PatentResponse from(Patent patent) {
        return new PatentResponse(
                patent.getId(),
                patent.getNumber(),
                patent.getTitle(),
                patent.getScanUrl(),
                patent.getSortOrder()
        );
    }
}
