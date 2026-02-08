package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.mail.EmailService;
import com.bindord.financemanagement.model.auth.AccountCodeConfirmation;
import com.bindord.financemanagement.model.auth.Authority;
import com.bindord.financemanagement.model.auth.User;
import com.bindord.financemanagement.repository.AccountCodeConfirmationRepository;
import com.bindord.financemanagement.repository.UserRepository;
import com.bindord.financemanagement.utils.ActivationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccountCodeConfirmationRepository accountCodeConfirmationRepository;
  private final EmailService emailService;

  @Transactional
  public void registerUser(String email, String rawPassword) {

    if (userRepository.existsByUsername(email)) {
      throw new IllegalStateException("User already exists");
    }

    User user = User.builder()
        .username(email)
        .password(passwordEncoder.encode(rawPassword))
        .enabled(false)
        .build();

    Authority authority = Authority.builder()
        .authority("ROLE_USER")
        .user(user)
        .build();

    user.setAuthorities(Set.of(authority));
    userRepository.save(user);

    String token = ActivationCodeGenerator.generateToken();
    String tokenHash = ActivationCodeGenerator.hashToken(token);

    AccountCodeConfirmation confirmation =
        AccountCodeConfirmation.builder()
            .username(email)
            .codeHash(tokenHash)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .used(false)
            .createdAt(LocalDateTime.now())
            .build();

    accountCodeConfirmationRepository.save(confirmation);

    emailService.sendActivationEmail(email, token);
  }
}
