package app.burmistrov.repository;

import app.burmistrov.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByCreatedByIsNullOrCreatedBy_Id(Long trainerId);

    /**
     * Есть ли у тренера упражнение с таким названием. Область та же, что и в списке каталога:
     * общие упражнения плюс собственные — именно среди них тезка создаёт путаницу.
     * {@code excludeId} позволяет переименовывать упражнение, не натыкаясь на самого себя.
     */
    @Query("""
            select count(e) > 0 from Exercise e
            where lower(trim(e.name)) = lower(trim(:name))
              and (e.createdBy is null or e.createdBy.id = :trainerId)
              and (:excludeId is null or e.id <> :excludeId)
            """)
    boolean existsVisibleWithName(@Param("name") String name,
                                  @Param("trainerId") Long trainerId,
                                  @Param("excludeId") Long excludeId);
}
