package com.pharma.drugverification.service;

import com.pharma.drugverification.config.ApplicationProperties;
import com.pharma.drugverification.domain.User;
import com.pharma.drugverification.dto.AuthResponse;
import com.pharma.drugverification.dto.LoginRequest;
import com.pharma.drugverification.repository.UserRepository;
import com.pharma.drugverification.security.JwtTokenProvider;
import com.pharma.drugverification.security.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharma.drugverification.exception.BadRequestException;
import com.pharma.drugverification.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationProperties applicationProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuditService auditService;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final int MAX_FAILED_ATTEMPTS = 3;

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.getActive()) {
            throw new BadRequestException("Account is disabled");
        }

        if (isAccountLocked(user)) {
            throw new BadRequestException("Account is locked. Please try again later.");
        }

        if (!passwordHashingService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadRequestException("Invalid credentials");
        }

        resetFailedAttempts(user);

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername());

        auditService.log("USER_LOGIN", "User", user.getId(), user.getId(), null);

        return new AuthResponse(
                accessToken,
                refreshToken,
                applicationProperties.getJwt().getExpiration(),
                user.getId(),
                user.getUsername(),
                user.getRole().name());
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getActive()) {
            throw new BadRequestException("Account is disabled");
        }

        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                applicationProperties.getJwt().getExpiration(),
                user.getId(),
                user.getUsername(),
                user.getRole().name());
    }

    @Transactional
    public void logout(String token, Long userId) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        long expirationSeconds = applicationProperties.getJwt().getExpiration() / 1000;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationSeconds, TimeUnit.SECONDS);

        auditService.log("USER_LOGOUT", "User", userId, userId, null);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            int lockoutMinutes = applicationProperties.getSecurity().getLockoutDurationMinutes();
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            log.warn("Account locked for user: {}", user.getUsername());
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}
