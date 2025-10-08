package com.bindord.financemanagement.controller.expend;

// src/main/java/com/example/finance/controller/ResumeExpenditureController.java

import com.bindord.financemanagement.model.resume.ResumeSummaryProjection;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeExpenditureController {

  private final ExpenditureRepository expenditureRepository;

  public ResumeExpenditureController(ExpenditureRepository expenditureRepository) {
    this.expenditureRepository = expenditureRepository;
  }

  // 1️⃣ Summary endpoint (main resume data)
  @GetMapping("/summary")
  public List<ResumeSummaryProjection> getSummary(
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    return expenditureRepository.getMonthlySummary(startDate, endDate);
  }

  // 2️⃣ Money lent (pending)
  @GetMapping("/lent")
  public String getLentPending() {
    // placeholder for actual lent query
    return "Pending lent records here";
  }

  // 3️⃣ Money borrowed (pending)
  @GetMapping("/borrowed")
  public String getBorrowedPending() {
    // placeholder for actual borrowed query
    return "Pending borrowed records here";
  }
}