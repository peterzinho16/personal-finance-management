package com.bindord.financemanagement.config;

import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.repository.MicrosoftAccessTokenRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("application")
@Slf4j
public class AppDataConfiguration implements ApplicationListener<ApplicationReadyEvent> {

  @Getter
  private Map<String, MicrosoftAccessToken> configData;

  private final MicrosoftAccessTokenRepository microsoftAccessTokenRepository;
  public static final String APP_DATA_KEY = "financeManagementApp";

  public AppDataConfiguration(MicrosoftAccessTokenRepository microsoftAccessTokenRepository) {
    this.microsoftAccessTokenRepository = microsoftAccessTokenRepository;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    configData = new HashMap<>();
    configData.put(APP_DATA_KEY, microsoftAccessTokenRepository.findLastRecord());
  }

}
