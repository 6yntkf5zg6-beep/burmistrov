package app.burmistrov.service;

import app.burmistrov.domain.TrainingProgram;
import app.burmistrov.domain.User;
import app.burmistrov.dto.TrainingProgramRequest;
import app.burmistrov.dto.TrainingProgramResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.TrainingProgramRepository;
import app.burmistrov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrainingProgramResponse create(Long trainerId, TrainingProgramRequest request) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        TrainingProgram program = TrainingProgram.builder()
                .trainer(trainer)
                .name(request.name())
                .description(request.description())
                .build();
        return TrainingProgramResponse.from(trainingProgramRepository.save(program));
    }

    @Transactional(readOnly = true)
    public List<TrainingProgramResponse> listForTrainer(Long trainerId) {
        return trainingProgramRepository.findByTrainer_Id(trainerId).stream()
                .map(TrainingProgramResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainingProgramResponse get(Long programId, Long trainerId) {
        return TrainingProgramResponse.from(getOwnedByTrainer(programId, trainerId));
    }

    @Transactional
    public TrainingProgramResponse update(Long programId, Long trainerId, TrainingProgramRequest request) {
        TrainingProgram program = getOwnedByTrainer(programId, trainerId);
        program.setName(request.name());
        program.setDescription(request.description());
        return TrainingProgramResponse.from(program);
    }

    @Transactional
    public void delete(Long programId, Long trainerId) {
        TrainingProgram program = getOwnedByTrainer(programId, trainerId);
        trainingProgramRepository.delete(program);
    }

    @Transactional(readOnly = true)
    public TrainingProgram getOwnedByTrainer(Long programId, Long trainerId) {
        TrainingProgram program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Training program not found"));
        if (!program.getTrainer().getId().equals(trainerId)) {
            throw new AccessDeniedException("Not your training program");
        }
        return program;
    }
}
