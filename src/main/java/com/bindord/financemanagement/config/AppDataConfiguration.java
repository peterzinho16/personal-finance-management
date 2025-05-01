package com.bindord.financemanagement.config;

import com.bindord.financemanagement.model.finance.MicrosoftAccessToken;
import com.bindord.financemanagement.model.finance.UsdToPenConversion;
import com.bindord.financemanagement.model.source.ExchangeRateDto;
import com.bindord.financemanagement.repository.MicrosoftAccessTokenRepository;
import com.bindord.financemanagement.repository.UsdToPenConversionRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.bindord.financemanagement.model.source.ExchangeRateDto.ExchangeRateSource.FROM_LOCAL_DB;
import static com.bindord.financemanagement.model.source.ExchangeRateDto.ExchangeRateSource.FROM_SUNAT_SBS;

@Component
@Scope("application")
@Slf4j
public class AppDataConfiguration implements ApplicationListener<ApplicationReadyEvent> {

  @Getter
  private Map<String, MicrosoftAccessToken> configData;

  @Getter
  private Map<String, ExchangeRateDto> exchangeRateData;

  private final MicrosoftAccessTokenRepository microsoftAccessTokenRepository;
  private final UsdToPenConversionRepository usdToPenConversionRepository;
  public static final String APP_DATA_KEY = "financeManagementApp";
  public static final String CURRENT_USD_EXCHANGE_RATE = "currentUSDExchangeRate";

  public AppDataConfiguration(MicrosoftAccessTokenRepository microsoftAccessTokenRepository,
                              UsdToPenConversionRepository usdToPenConversionRepository) {
    this.microsoftAccessTokenRepository = microsoftAccessTokenRepository;
    this.usdToPenConversionRepository = usdToPenConversionRepository;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    //Init variables
    configData = new HashMap<>();
    exchangeRateData = new HashMap<>();

    configData.put(APP_DATA_KEY, microsoftAccessTokenRepository.findLastRecord());
    var usdExchangeRateDto = fetchExchangeRate();
    if (usdExchangeRateDto != null) {
      log.info("Current exchange rate from internet: {}", usdExchangeRateDto);
      exchangeRateData.put(CURRENT_USD_EXCHANGE_RATE,
          usdExchangeRateDto);
    } else {

      UsdToPenConversion usdToPenConversion =
          usdToPenConversionRepository.findLatestByEffectiveDate();
      LocalDateTime effectiveDate = usdToPenConversion.getEffectiveDate().atStartOfDay();
      LocalDateTime effectiveDateFull = effectiveDate.plusHours(8);

      usdExchangeRateDto = new ExchangeRateDto(FROM_LOCAL_DB, effectiveDateFull,
          usdToPenConversion.getExchangeRate());
      exchangeRateData.put(CURRENT_USD_EXCHANGE_RATE,
          usdExchangeRateDto);
      log.info("Current exchange rate obtained from local: {}", usdExchangeRateDto);
    }

  }

  public ExchangeRateDto fetchExchangeRate() {
    try {
      URL url = new URL("https://www.sunat.gob.pe/a/txt/tipoCambio.txt");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      int responseCode = conn.getResponseCode();
      if (responseCode == 200) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          String inputLine = in.readLine();
          if (inputLine != null) {
            String[] parts = inputLine.split("\\|");
            if (parts.length >= 3) {
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
              LocalDate date = LocalDate.parse(parts[0], formatter);
              LocalDateTime dateTime = date.atStartOfDay();
              LocalDateTime updatedDateTime = dateTime.plusHours(8);
              return new ExchangeRateDto(FROM_SUNAT_SBS, updatedDateTime,
                  new BigDecimal(parts[2]));
            } else {
              log.warn("Unexpected format from exchange rate data from internet: {}", inputLine);
            }
          } else {
            log.warn("Empty response from exchange rate service from internet");
          }
        }
      } else {
        log.error("Failed to fetch exchange rate from internet. HTTP response code: {}", responseCode);
      }
    } catch (Exception e) {
      log.error("Error fetching exchange rate data from internet", e);
    }
    return null;
  }


}
