package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.repository.ExpenditureOthersRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RestController
@RequestMapping("/eureka/finance-app/expenditure-others")
@AllArgsConstructor
public class ExpenditureOtherController {

  private final ExpenditureOthersRepository repository;
  private final SubCategoryRepository subCategoryRepository;

  @GetMapping
  Page<ExpenditureOthers> findAll(Pageable pageable,
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
  ExpenditureOthers findById(@PathVariable Integer id) {
    return repository.findByIdWithSubCategory(id);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Integer id) {
    log.info("Initialization... deleting expenditure with id {}", id);
    repository.deleteById(id);
    log.info("Finished deleting expenditure with id {}", id);
  }

}
