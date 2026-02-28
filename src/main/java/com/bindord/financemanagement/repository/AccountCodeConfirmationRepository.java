package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.auth.AccountCodeConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountCodeConfirmationRepository extends JpaRepository<AccountCodeConfirmation,
    Long> {

  @Query("""
        SELECT c
        FROM AccountCodeConfirmation c
        WHERE c.codeHash = :hash
          AND c.used = false
          AND c.expiresAt > :now
    """)
  Optional<AccountCodeConfirmation> findValidToken(
      String hash,
      LocalDateTime now
  );

  boolean existsByCodeHashAndUsedFalseAndExpiresAtAfter(
      String codeHash,
      LocalDateTime now
  );
}
