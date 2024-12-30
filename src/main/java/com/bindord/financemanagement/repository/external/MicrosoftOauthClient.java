package com.bindord.financemanagement.repository.external;


import com.bindord.financemanagement.model.oauth.MicrosoftAccessTokenRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "microsoftOauthClient", url = "https://login.microsoftonline.com/")
public interface MicrosoftOauthClient {

  @PostMapping(value = "common/oauth2/v2.0/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  MicrosoftAccessTokenRecord getAccessToken(
      @RequestBody MultiValueMap<String, String> formParams);
}
