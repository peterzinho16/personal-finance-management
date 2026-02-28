package com.bindord.financemanagement.svc;


import com.bindord.financemanagement.mail.EmailService;
import com.bindord.financemanagement.model.auth.AccountCodeConfirmation;
import com.bindord.financemanagement.repository.AccountCodeConfirmationRepository;
import com.bindord.financemanagement.utils.HashCodeGenerator;
import com.bindord.financemanagement.utils.enums.MailNotificationType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AccountCodeConfirmationService {

  private final AccountCodeConfirmationRepository repository;
  private final EmailService emailService;

  @Transactional
  public AccountCodeConfirmation generateConfirmationCode(String username, MailNotificationType type) {
    String token = HashCodeGenerator.generateToken();
    String tokenHash = HashCodeGenerator.hashToken(token);

    AccountCodeConfirmation confirmation =
        AccountCodeConfirmation.builder()
            .username(username)
            .codeHash(tokenHash)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .used(false)
            .createdAt(LocalDateTime.now())
            .build();

    var code = repository.save(confirmation);
    emailService.sendNotificationEmail(username, token, type);
    return code;
  }

  @Transactional(readOnly = true)
  public boolean validateIfTokenWasNotUsed(String rawToken) {
    String hash = HashCodeGenerator.hashToken(rawToken);
    return repository.existsByCodeHashAndUsedFalseAndExpiresAtAfter(hash, LocalDateTime.now());
  }
}
