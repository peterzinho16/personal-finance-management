package com.bindord.financemanagement.model.auth;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME) // Equivalent to sequential ID
  @Column(name = "user_id", updatable = false, nullable = false)
  private UUID userId;

  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(nullable = false)
  private boolean enabled;

  @JsonManagedReference
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true
  )
  private Set<Authority> authorities;
}