package app.burmistrov.repository;

import app.burmistrov.domain.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

    List<TrainingProgram> findByTrainer_Id(Long trainerId);
}
