package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.auth.Authority;
import com.bindord.financemanagement.model.auth.CustomUserDetails;
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

    CustomUserDetails.builder();

    User user = userRepository.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("User not found"));
    List<Authority> authorities = authorityRepository.findByUser(user);
    return CustomUserDetails.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .password(user.getPassword())
        .enabled(user.isEnabled())
        .accountNonLocked(user.isAccountNonLocked())
        .authorities(
            authorities
                .stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .toList()
        )
        .build();
  }
}
