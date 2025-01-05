package com.bindord.financemanagement.model.finance;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "payee_categorizations")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "payeeId")
public class PayeeCategorization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payee_id")
  private Integer payeeId;

  @Column(nullable = false, name = "payee")
  private String payee;

  @Column(nullable = false, unique = true, updatable = false)
  private String lowerPayee;

  @NotNull
  @Column(nullable = false, name = "creation_date")
  private LocalDateTime creationDate;

  @Column(name = "modified_at")
  private LocalDateTime modifiedAt;

  @Column(nullable = false)
  private Integer totalEvents;

  @JsonManagedReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sub_category_id", nullable = false)
  private SubCategory subCategory;
}
