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
public class ExpenditureUpdateFormDto {

  private ExpenditureUpdateDto expenditureUpdateDto;
  private FormBehaviour formBehaviour;

  @Setter
  @Getter
  public static class FormBehaviour {
    private Boolean updateWithoutPayeeCategorization;

  }
}
