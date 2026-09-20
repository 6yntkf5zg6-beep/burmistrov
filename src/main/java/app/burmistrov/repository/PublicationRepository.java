package app.burmistrov.repository;

import app.burmistrov.domain.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findAllByOrderBySortOrderAscIdAsc();

    Optional<Publication> findFirstByOrderBySortOrderDescIdDesc();
}
