package com.bindord.financemanagement.repository;

import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MicrosoftAccessTokenRepository extends CrudRepository<MicrosoftAccessToken, Integer> {

  @Query(nativeQuery = true, value = "SELECT * FROM MICROSOFT_ACCESS_TOKENS ORDER BY TOKEN_ID DESC LIMIT 1")
  MicrosoftAccessToken findLastRecord();
}
