package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.advice.CustomValidationException;
import com.bindord.financemanagement.model.dashboard.CategoryMonthlyTotalsProjection;
import com.bindord.financemanagement.model.finance.Category;
import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureAddDto;
import com.bindord.financemanagement.model.finance.ExpenditureDto;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateFormDto;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.repository.CategoryRepository;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.svc.ExpenditureReportService;
import com.bindord.financemanagement.svc.ExpenditureService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/expenditure")
@AllArgsConstructor
public class ExpenditureController {

  private final ExpenditureRepository repository;
  private final SubCategoryRepository subCategoryRepository;
  private final ExpenditureService expenditureService;
  private final ExpenditureReportService expenditureReportService;
  private final CategoryRepository categoryRepository;

  @GetMapping
  Page<Expenditure> findAll(Pageable pageable,
                            @RequestParam(required = false) String subCategoryName) {
    Integer subCatId;
    if (subCategoryName == null) {
      subCatId = null;
    } else {
      SubCategory subCategory = subCategoryRepository.findByName(subCategoryName);
      subCatId = subCategory.getId();
    }
    return repository.findAllWithSubCategory(subCatId, pageable);
  }

  @GetMapping("/{id}")
  Expenditure findByIdWithPageable(@PathVariable Integer id) {
    return repository.findByIdWithSubCategory(id);
  }

  @PostMapping
  ExpenditureDto save(@RequestBody @Valid ExpenditureAddDto expenditure) throws CustomValidationException, NoSuchAlgorithmException {
    var response = expenditureService.saveNewManually(expenditure);
    var responseObj = new ExpenditureDto();
    copyProperties(response, responseObj);
    return responseObj;
  }

  @PutMapping("/{id}")
  ExpenditureDto update(@RequestBody ExpenditureUpdateFormDto expenditureUpdateFormDto,
                        @PathVariable Integer id) throws Exception {
    var response = expenditureService.updateById(expenditureUpdateFormDto, id);
    var responseObj = new ExpenditureDto();
    copyProperties(response, responseObj);
    return responseObj;
  }

  @PutMapping("/{id}/{subCategoryId}")
  public Expenditure updateSubCategoryById(@PathVariable Integer id,
                                           @PathVariable Integer subCategoryId,
                                           @RequestParam(required = false) String payee) throws Exception {
    return expenditureService.updateSubCategoryById(subCategoryId, id, payee);
  }

  @PutMapping("/{id}/update/shared")
  public ExpenditureDto updateSharedStateById(@PathVariable Integer id) throws Exception {
    var response = expenditureService.updateSharedById(id);
    var responseObj = new ExpenditureDto();
    copyProperties(response, responseObj);
    return responseObj;
  }


  @DeleteMapping("/{id}")
  public void delete(@PathVariable Integer id) throws Exception {
    log.info("Initialization... deleting expenditure with id {}", id);
    expenditureService.deleteById(id);
    log.info("Finished deleting expenditure with id {}", id);
  }

  @GetMapping("/reports/get-category-monthly-totals")
  public List<CategoryMonthlyTotalsProjection> getCategoryMonthlyTotals() {
    return expenditureReportService.expenditureReport();
  }

  @GetMapping("/reports/list/by-period-and-category")
  List<Expenditure> findAllByYearMonthAndCategory(
      @RequestParam String period, @RequestParam String categoryName, @RequestParam Boolean shared) {
    String[] date = period.split("-");
    Category category = categoryRepository.findByNameIgnoreCase(categoryName);
    Integer categoryId = category.getId();
    return repository.findAllByYearMonthAndCategoryAndShared(
        LocalDateTime.of(
            Integer.parseInt(date[0]),
            Integer.parseInt(date[1]),
            1, 0, 0,0),
        LocalDateTime.of(
            Integer.parseInt(date[0]),
            Integer.parseInt(date[1]) + 1,
            1, 0, 0,0),
        categoryId,
        shared);
  }
}
