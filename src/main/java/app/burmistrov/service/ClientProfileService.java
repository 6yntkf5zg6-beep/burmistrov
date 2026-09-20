package app.burmistrov.service;

import app.burmistrov.domain.ClientProfile;
import app.burmistrov.dto.ClientProfileRequest;
import app.burmistrov.dto.ClientProfileResponse;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final ClientProfileRepository clientProfileRepository;
    private final TrainerClientService trainerClientService;

    @Transactional(readOnly = true)
    public ClientProfileResponse getMine(Long clientId) {
        return ClientProfileResponse.from(findByUserId(clientId));
    }

    @Transactional
    public ClientProfileResponse update(Long clientId, ClientProfileRequest request) {
        ClientProfile profile = findByUserId(clientId);
        profile.setBirthDate(request.birthDate());
        profile.setGender(request.gender());
        profile.setHeightCm(request.heightCm());
        profile.setWeightKg(request.weightKg());
        profile.setGoal(request.goal());
        profile.setHealthNotes(request.healthNotes());
        return ClientProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public ClientProfileResponse getForTrainer(Long clientId, Long trainerId) {
        if (!trainerClientService.isActiveRelation(trainerId, clientId)) {
            throw new AccessDeniedException("No active relation with this client");
        }
        return ClientProfileResponse.from(findByUserId(clientId));
    }

    private ClientProfile findByUserId(Long clientId) {
        return clientProfileRepository.findByUser_Id(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));
    }
}
