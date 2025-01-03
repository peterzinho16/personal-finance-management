package com.bindord.financemanagement.model.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "parameters")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "key")
public class Parameter
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String key;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false)
  private String value;

  @NotBlank
  @Size(max = 255)
  @Column
  private String description;
}