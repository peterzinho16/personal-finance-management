package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.auth.Authority;
import com.bindord.financemanagement.model.auth.User;
import com.bindord.financemanagement.repository.UserRepository;
import com.bindord.financemanagement.utils.enums.MailNotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccountCodeConfirmationService accountCodeConfirmationService;

  @Transactional
  public void registerUser(String email, String rawPassword) {

    if (userRepository.existsByUsername(email)) {
      throw new IllegalStateException("User already exists");
    }

    User user = User.builder()
        .username(email)
        .password(passwordEncoder.encode(rawPassword))
        .accountNonLocked(true)
        .enabled(false)
        .build();

    Authority authority = Authority.builder()
        .authority("ROLE_USER")
        .user(user)
        .build();

    user.setAuthorities(Set.of(authority));
    userRepository.save(user);
    accountCodeConfirmationService.generateConfirmationCode(email, MailNotificationType.SIGNUP_USER_CONFIRMATION);

  }

  public boolean validateIfUserExistsAndAccountIsActive(String email) {
    return userRepository.existsByUsernameAndEnabledTrue(email);
  }
}
