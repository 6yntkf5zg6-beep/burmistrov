package app.burmistrov.service;

import app.burmistrov.domain.TrainerProfile;
import app.burmistrov.dto.TrainerProfileRequest;
import app.burmistrov.dto.TrainerProfileResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.TrainerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerProfileService {

    private final TrainerProfileRepository trainerProfileRepository;

    @Transactional(readOnly = true)
    public TrainerProfileResponse getMine(Long trainerId) {
        return TrainerProfileResponse.from(findByUserId(trainerId));
    }

    @Transactional
    public TrainerProfileResponse update(Long trainerId, TrainerProfileRequest request) {
        TrainerProfile profile = findByUserId(trainerId);
        profile.setBio(request.bio());
        profile.setSpecialization(request.specialization());
        profile.setExperienceYears(request.experienceYears());
        return TrainerProfileResponse.from(profile);
    }

    private TrainerProfile findByUserId(Long trainerId) {
        return trainerProfileRepository.findByUser_Id(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
    }
}
