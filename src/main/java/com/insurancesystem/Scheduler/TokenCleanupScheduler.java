package com.insurancesystem.Scheduler;

import com.insurancesystem.Repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Clean up expired revoked tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            revokedTokenRepository.deleteExpiredTokens(Instant.now());
            log.debug("Cleaned up expired revoked tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }
}
