package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.dashboard.CategoryMonthlyTotalsProjection;
import com.bindord.financemanagement.model.dashboard.MonthlyExpenseSummaryDTO;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureReportService {

  private final ExpenditureRepository expenditureRepository;

  public List<CategoryMonthlyTotalsProjection> expenditureReport(){
    return expenditureRepository.getCategoryMonthlyTotals();
  }

  public List<MonthlyExpenseSummaryDTO> getMonthlySummaries() {
    return expenditureRepository.getMonthlySummary();
  }
}
