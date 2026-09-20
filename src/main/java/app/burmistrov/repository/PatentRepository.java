package app.burmistrov.repository;

import app.burmistrov.domain.Patent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatentRepository extends JpaRepository<Patent, Long> {

    List<Patent> findAllByOrderBySortOrderAscIdAsc();

    Optional<Patent> findFirstByOrderBySortOrderDescIdDesc();
}
