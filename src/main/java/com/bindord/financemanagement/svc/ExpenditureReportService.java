package com.bindord.financemanagement.svc;

import com.bindord.financemanagement.model.dashboard.CategoryMonthlyTotalsProjection;
import com.bindord.financemanagement.model.dashboard.MonthlyExpenseSummaryDTO;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ExpenditureReportService {

  private final ExpenditureRepository expenditureRepository;

  public List<CategoryMonthlyTotalsProjection> expenditureReportByUserId(UUID userId){
    return expenditureRepository.getCategoryMonthlyTotalsByUserId(userId);
  }

  public List<MonthlyExpenseSummaryDTO> getMonthlySummariesByUserId(UUID userId) {
    return expenditureRepository.getMonthlySummaryByUserId(userId);
  }
}
