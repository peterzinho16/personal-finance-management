package com.bindord.financemanagement.controller.expend;

import com.bindord.financemanagement.model.finance.ExpenditureDto;
import com.bindord.financemanagement.model.finance.ExpenditureOthers;
import com.bindord.financemanagement.model.finance.ExpenditureUpdateFormDto;
import com.bindord.financemanagement.model.finance.SubCategory;
import com.bindord.financemanagement.repository.ExpenditureOthersRepository;
import com.bindord.financemanagement.repository.SubCategoryRepository;
import com.bindord.financemanagement.svc.ExpenditureOthersService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_EXP_IMPORTED_ALREADY;
import static com.bindord.financemanagement.utils.Constants.MSG_ERROR_EXP_NOT_SHARED_CANT_BE_IMPORTED;
import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@RestController
@RequestMapping("/eureka/finance-app/expenditure-others")
@AllArgsConstructor
public class ExpenditureOtherController {

  private final ExpenditureOthersRepository repository;
  private final SubCategoryRepository subCategoryRepository;
  private final ExpenditureOthersService expenditureOthersService;

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

  @PutMapping("/{id}/update/shared")
  public ExpenditureDto updateSharedStateById(@PathVariable Integer id) throws Exception {
    var response = expenditureOthersService.updateSharedById(id);
    var responseObj = new ExpenditureDto();
    copyProperties(response, responseObj);
    return responseObj;
  }

  @PutMapping("/{id}")
  ExpenditureDto update(@RequestBody ExpenditureUpdateFormDto expenditureUpdateFormDto,
                        @PathVariable Integer id) throws Exception {
    var response = expenditureOthersService.updateById(expenditureUpdateFormDto, id);
    var responseObj = new ExpenditureDto();
    copyProperties(response, responseObj);
    return responseObj;
  }

  @PutMapping("/{id}/update/import-state")
  void updateImportState(@PathVariable Integer id) throws Exception {
    ExpenditureOthers expenditureOthers = expenditureOthersService.findById(id);
    if (!expenditureOthers.getShared()) {
      throw new Exception(MSG_ERROR_EXP_NOT_SHARED_CANT_BE_IMPORTED);
    }
    if (expenditureOthers.getWasImported() == null || !expenditureOthers.getWasImported()) {
      repository.updateImportStateById(id);
      return;
    }
    throw new Exception(MSG_ERROR_EXP_IMPORTED_ALREADY);
  }

}
