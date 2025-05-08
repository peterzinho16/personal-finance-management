package com.bindord.financemanagement.model.finance;

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
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "microsoft_access_tokens")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class MicrosoftAccessToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer tokenId;

  @Column(nullable = false)
  private String tokenType;

  @Column(name = "scopes", length = 1000)
  private String scope;

  @Column
  private Integer expiresIn;

  @Column
  private Integer extExpiresIn;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String accessToken;

  @Column(nullable = true, columnDefinition = "TEXT")
  private String idToken;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;
}