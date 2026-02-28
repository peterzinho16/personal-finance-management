package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.auth.AccountCodeConfirmation;
import com.bindord.financemanagement.model.auth.User;
import com.bindord.financemanagement.repository.AccountCodeConfirmationRepository;
import com.bindord.financemanagement.repository.UserRepository;
import com.bindord.financemanagement.utils.HashCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountActivationService {

  private final AccountCodeConfirmationRepository repository;
  private final UserRepository userRepository;

  @Transactional
  public void activateAccount(String rawToken) {

    String hash = HashCodeGenerator.hashToken(rawToken);

    AccountCodeConfirmation confirmation =
        repository.findValidToken(hash, LocalDateTime.now())
            .orElseThrow(() ->
                new IllegalStateException("Invalid or expired activation token"));

    User user = userRepository.findByUsername(confirmation.getUsername())
        .orElseThrow(() ->
            new IllegalStateException("User not found"));

    if (user.isEnabled()) {
      throw new IllegalStateException("Account already activated");
    }

    user.setEnabled(true);
    confirmation.setUsed(true);

    userRepository.save(user);
    repository.save(confirmation);

    log.info("Account activated for user {}", user.getUsername());
  }

  @Transactional
  public void resetPassword(String rawToken, String newEncodedPassword) {

    String hash = HashCodeGenerator.hashToken(rawToken);

    AccountCodeConfirmation confirmation =
        repository.findValidToken(hash, LocalDateTime.now())
            .orElseThrow(() ->
                new IllegalStateException("Invalid or expired reset token"));

    User user = userRepository.findByUsername(confirmation.getUsername())
        .orElseThrow(() ->
            new IllegalStateException("User not found"));

    if (!user.isEnabled()) {
      throw new IllegalStateException("User account is not active");
    }

    if (confirmation.isUsed()) {
      throw new IllegalStateException("Reset token already used");
    }

    user.setPassword(newEncodedPassword);
    confirmation.setUsed(true);

    userRepository.save(user);
    repository.save(confirmation);

    log.info("Password successfully reset for user {}", user.getUsername());
  }
}
