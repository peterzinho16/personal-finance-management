package com.bindord.financemanagement.svc.auth;

import com.bindord.financemanagement.model.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserService {

  public UUID getCurrentUserId() {
    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

    CustomUserDetails userDetails =
        (CustomUserDetails) authentication.getPrincipal();

    return userDetails.getUserId();
  }

  public boolean isAdmin() {
    return SecurityContextHolder.getContext()
        .getAuthentication()
        .getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}