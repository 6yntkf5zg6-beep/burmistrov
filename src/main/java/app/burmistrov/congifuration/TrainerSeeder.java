package app.burmistrov.congifuration;

import app.burmistrov.domain.TrainerProfile;
import app.burmistrov.domain.User;
import app.burmistrov.repository.TrainerProfileRepository;
import app.burmistrov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The service has no open sign-up: the only way in is a referral link a trainer generates.
 * That means at least one trainer account must exist before anyone can log in, so this seeds
 * exactly one on first boot if none is present yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-trainer.email:trainer@burmistrov.pro}")
    private String seedEmail;

    @Value("${app.seed-trainer.password:ChangeMe123!}")
    private String seedPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(User.Role.TRAINER)) {
            return;
        }

        User trainer = User.builder()
                .email(seedEmail)
                .passwordHash(passwordEncoder.encode(seedPassword))
                .firstName("Дмитрий")
                .lastName("Бурмистров")
                .role(User.Role.TRAINER)
                .active(true)
                .build();
        trainer = userRepository.save(trainer);
        trainerProfileRepository.save(TrainerProfile.builder().user(trainer).build());

        log.warn("Seeded the initial trainer account: {} / {} — change the password after first login.",
                seedEmail, seedPassword);
    }
}
