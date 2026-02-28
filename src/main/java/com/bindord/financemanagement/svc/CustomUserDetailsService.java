package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.auth.Authority;
import com.bindord.financemanagement.model.auth.User;
import com.bindord.financemanagement.repository.AuthorityRepository;
import com.bindord.financemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;

  @Override
  public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException {

    User user = userRepository.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("User not found"));
    List<Authority> authorities = authorityRepository.findByUser(user);
    return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        .disabled(!user.isEnabled())
        .authorities(
            authorities
                .stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .toList()
        )
        .build();
  }
}
