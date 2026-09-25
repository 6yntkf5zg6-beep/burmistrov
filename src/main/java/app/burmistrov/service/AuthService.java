package app.burmistrov.service;

import app.burmistrov.domain.ClientInvite;
import app.burmistrov.domain.ClientProfile;
import app.burmistrov.domain.TrainerClient;
import app.burmistrov.domain.User;
import app.burmistrov.dto.AuthResponse;
import app.burmistrov.dto.LoginRequest;
import app.burmistrov.dto.RefreshRequest;
import app.burmistrov.dto.RegisterRequest;
import app.burmistrov.exception.ConflictException;
import app.burmistrov.exception.ResourceNotFoundException;
import app.burmistrov.repository.ClientInviteRepository;
import app.burmistrov.repository.ClientProfileRepository;
import app.burmistrov.repository.TrainerClientRepository;
import app.burmistrov.repository.UserRepository;
import app.burmistrov.security.JwtService;
import app.burmistrov.security.RefreshTokenService;
import app.burmistrov.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ClientInviteRepository clientInviteRepository;
    private final TrainerClientRepository trainerClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        ClientInvite invite = clientInviteRepository.findByToken(request.inviteToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invite link not found"));
        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw new ConflictException("Invite link has expired");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                // Имя приходит от самого клиента: приглашение его больше не несёт.
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .phone(request.phone())
                .role(User.Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);
        clientProfileRepository.save(ClientProfile.builder().user(user).build());

        trainerClientRepository.save(TrainerClient.builder()
                .trainer(invite.getTrainer())
                .client(user)
                .status(TrainerClient.Status.ACTIVE)
                .startedAt(Instant.now())
                .build());

        clientInviteRepository.delete(invite);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.email()));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private AuthResponse issueTokens(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(accessToken, refreshToken, jwtService.getExpirationMs() / 1000,
                user.getId(), user.getEmail(), user.getRole());
    }
}
