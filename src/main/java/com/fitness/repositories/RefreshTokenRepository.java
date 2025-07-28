package com.fitness.repositories;

import com.fitness.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    //void deleteByUsername(String username);

    @Modifying
    @Query("update RefreshToken r " +
            "set r.revoked = true " +
            "where r.username = :u and r.revoked = false")
    void revokeAllActive(@Param("u") String username);
}
