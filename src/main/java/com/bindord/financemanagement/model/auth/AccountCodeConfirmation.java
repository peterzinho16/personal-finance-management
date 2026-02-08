package com.bindord.financemanagement.model.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_code_confirmation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCodeConfirmation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  @Column(name = "code_hash", nullable = false, length = 64)
  private String codeHash;

  private LocalDateTime expiresAt;

  private boolean used;

  private LocalDateTime createdAt;
}
