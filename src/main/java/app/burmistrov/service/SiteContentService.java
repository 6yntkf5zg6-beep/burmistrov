package app.burmistrov.service;

import app.burmistrov.domain.Ordered;
import app.burmistrov.domain.Patent;
import app.burmistrov.domain.Publication;
import app.burmistrov.dto.PatentRequest;
import app.burmistrov.dto.PatentResponse;
import app.burmistrov.dto.PublicationRequest;
import app.burmistrov.dto.PublicationResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.PatentRepository;
import app.burmistrov.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержимое блоков «Патенты» и «Публикации» на главной. Правит его только тренер,
 * читают все — страница публичная.
 */
@Service
@RequiredArgsConstructor
public class SiteContentService {

    public enum Direction { UP, DOWN }

    private final PatentRepository patentRepository;
    private final PublicationRepository publicationRepository;

    // --- патенты -------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PatentResponse> listPatents() {
        return patentRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(PatentResponse::from).toList();
    }

    @Transactional
    public PatentResponse createPatent(PatentRequest request) {
        Patent patent = Patent.builder()
                .scanUrl(request.scanUrl().trim())
                .sortOrder(nextOrder(patentRepository.findFirstByOrderBySortOrderDescIdDesc()
                        .map(Patent::getSortOrder).orElse(null)))
                .build();
        return PatentResponse.from(patentRepository.save(patent));
    }

    @Transactional
    public PatentResponse updatePatent(Long id, PatentRequest request) {
        Patent patent = patentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patent not found"));
        // Номер и название не трогаем: форма их не спрашивает, а у старых записей они есть.
        patent.setScanUrl(request.scanUrl().trim());
        return PatentResponse.from(patent);
    }

    @Transactional
    public void deletePatent(Long id) {
        Patent patent = patentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patent not found"));
        patentRepository.delete(patent);
        patentRepository.flush();
        renumber(new ArrayList<>(patentRepository.findAllByOrderBySortOrderAscIdAsc()));
    }

    @Transactional
    public List<PatentResponse> movePatent(Long id, Direction direction) {
        List<Patent> ordered = new ArrayList<>(patentRepository.findAllByOrderBySortOrderAscIdAsc());
        move(ordered, id, direction, "Patent not found");
        return ordered.stream().map(PatentResponse::from).toList();
    }

    // --- публикации ----------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PublicationResponse> listPublications() {
        return publicationRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(PublicationResponse::from)
                .toList();
    }

    @Transactional
    public PublicationResponse createPublication(PublicationRequest request) {
        Publication publication = Publication.builder()
                .title(request.title().trim())
                .authors(request.authors().trim())
                .annotation(request.annotation().trim())
                .sortOrder(nextOrder(publicationRepository.findFirstByOrderBySortOrderDescIdDesc()
                        .map(Publication::getSortOrder).orElse(null)))
                .build();
        return PublicationResponse.from(publicationRepository.save(publication));
    }

    @Transactional
    public PublicationResponse updatePublication(Long id, PublicationRequest request) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found"));
        publication.setTitle(request.title().trim());
        publication.setAuthors(request.authors().trim());
        publication.setAnnotation(request.annotation().trim());
        return PublicationResponse.from(publication);
    }

    @Transactional
    public void deletePublication(Long id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found"));
        publicationRepository.delete(publication);
        publicationRepository.flush();
        renumber(new ArrayList<>(publicationRepository.findAllByOrderBySortOrderAscIdAsc()));
    }

    @Transactional
    public List<PublicationResponse> movePublication(Long id, Direction direction) {
        List<Publication> ordered = new ArrayList<>(publicationRepository.findAllByOrderBySortOrderAscIdAsc());
        move(ordered, id, direction, "Publication not found");
        return ordered.stream().map(PublicationResponse::from).toList();
    }

    // --- общее ---------------------------------------------------------------

    /**
     * Переставляет запись на одну позицию и перенумеровывает весь список.
     * Перенумерация, а не обмен двух значений: так список чинится сам, даже если
     * в базе остались дырки или дубли после ручных правок.
     */
    private static <T extends Ordered> void move(List<T> ordered, Long id, Direction direction, String notFound) {
        int index = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new ResourceNotFoundException(notFound);
        }

        int target = direction == Direction.UP ? index - 1 : index + 1;
        // Край списка — не ошибка: кнопка просто ничего не делает.
        if (target >= 0 && target < ordered.size()) {
            T moved = ordered.remove(index);
            ordered.add(target, moved);
        }
        renumber(ordered);
    }

    private static <T extends Ordered> void renumber(List<T> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSortOrder(i + 1);
        }
    }

    private static int nextOrder(Integer lastOrder) {
        return lastOrder == null ? 1 : lastOrder + 1;
    }
}
