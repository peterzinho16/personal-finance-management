package com.bindord.financemanagement.model.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenditureUpdateDto {

  private Integer id;
  private String description;
  private Integer subCategoryId;
  private Boolean lent;
  private Boolean shared;
  private String lentTo;
  private Short installments;
}
