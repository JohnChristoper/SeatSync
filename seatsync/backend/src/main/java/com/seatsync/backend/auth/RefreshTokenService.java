package com.seatsync.backend.auth;

import com.seatsync.backend.user.User;
import com.seatsync.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId){
        User user = userRepository.findById(userId).get();

        RefreshToken tokenEntity = refreshTokenRepository.findByUserId(userId)
                .orElseGet(RefreshToken::new);

        tokenEntity.setUser(user);
        tokenEntity.setToken(UUID.randomUUID().toString());
        tokenEntity.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(tokenEntity);
    }

    public boolean isTokenExpired(RefreshToken refreshToken){
        return refreshToken.getExpiryDate().isBefore(Instant.now());
    }

    public boolean isTokenExists(Long userID){
        return refreshTokenRepository.findByUserId(userID).isPresent();
    }
}
