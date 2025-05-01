package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.UsdToPenConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsdToPenConversionRepository extends JpaRepository<UsdToPenConversion, Long> {

  @Query(value = "SELECT * FROM usd_to_pen_conversion ORDER BY effective_date DESC LIMIT 1", nativeQuery = true)
  UsdToPenConversion findLatestByEffectiveDate();
}