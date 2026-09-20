package app.burmistrov.dto;

import app.burmistrov.domain.Publication;

public record PublicationResponse(Long id, String title, String authors, String annotation, Integer sortOrder) {

    public static PublicationResponse from(Publication publication) {
        return new PublicationResponse(
                publication.getId(),
                publication.getTitle(),
                publication.getAuthors(),
                publication.getAnnotation(),
                publication.getSortOrder()
        );
    }
}
