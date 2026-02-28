package com.bindord.financemanagement.config;

import com.bindord.financemanagement.svc.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

  private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
  private final CustomUserDetailsService customUserDetailsService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/login",
//                "/eureka/**", //Temporal
                "/landing",
                "/register",
                "/error-page",
                "/forgot-password",
                "/activate",
                "/reset-password",
                "/registration-confirmation",
                "/activation-success",
                "/activation-error",
                "/expenditure/list",
                "/activate",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .userDetailsService(customUserDetailsService)
        .formLogin(form -> form
            .loginPage("/login")      // your custom login.html
            .defaultSuccessUrl("/", true)
            .failureHandler(customAuthenticationFailureHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );

    return http.build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}