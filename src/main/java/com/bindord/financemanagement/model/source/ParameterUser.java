package com.bindord.financemanagement.model.source;

import com.bindord.financemanagement.model.ParameterUserId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parameters_users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ParameterUser {

  @EmbeddedId
  private ParameterUserId id;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false)
  private String value;

  @NotBlank
  @Size(max = 255)
  @Column
  private String description;

  @Column
  private boolean enabled;
}