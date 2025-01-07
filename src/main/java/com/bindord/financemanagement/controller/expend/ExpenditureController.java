package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.finance.Expenditure;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateDto;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.repository.ExpenditureRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.svc.ExpenditureService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/eureka/finance-app/expenditure")
@AllArgsConstructor
public class ExpenditureController {

  private final ExpenditureRepository repository;
  private final SubCategoryRepository subCategoryRepository;
  private final ExpenditureService expenditureService;

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
  Expenditure save(Expenditure expenditure) {
    return repository.save(expenditure);
  }

  @PutMapping("/{id}")
  Expenditure update(@RequestBody ExpenditureUpdateDto expenditure, @PathVariable Integer id) throws Exception {
    return expenditureService.updateById(expenditure, id);
  }

  @PutMapping("/{id}/{subCategoryId}")
  public Expenditure updateSubCategoryById(@PathVariable Integer id,
                                           @PathVariable Integer subCategoryId,
                                           @RequestParam(required = false) String payee) throws Exception {
    return expenditureService.updateSubCategoryById(subCategoryId, id, payee);
  }

  @PutMapping("/{id}/update/shared")
  public Expenditure updateSharedStateById(@PathVariable Integer id) throws Exception {
    return expenditureService.updateSharedById(id);
  }
}
