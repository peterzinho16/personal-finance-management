package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.resume.BorrowedPendingProjection;
import com.bindord.financemanagement.model.resume.LentPendingProjection;
import com.bindord.financemanagement.model.resume.ResumeSummaryProjection;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
  public List<LentPendingProjection> getLentPending(
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return expenditureRepository.getLentPending(startDate, endDate);
  }

  // 3️⃣ Money borrowed (pending)
  @GetMapping("/borrowed")
  public List<BorrowedPendingProjection> getBorrowedPending(
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    return expenditureRepository.getBorrowedPending(startDate, endDate);
  }

  @PutMapping("/lent/{lentTo}/pay")
  public ResponseEntity<Void> markLentAsPaid(
      @PathVariable String lentTo,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    expenditureRepository.markLentAsPaid(lentTo, startDate, endDate);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/borrowed/{borrowedFrom}/pay")
  public ResponseEntity<Void> markBorrowedAsPaid(
      @PathVariable String borrowedFrom,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    expenditureRepository.markBorrowedAsPaid(borrowedFrom, startDate, endDate);
    return ResponseEntity.noContent().build();
  }
}