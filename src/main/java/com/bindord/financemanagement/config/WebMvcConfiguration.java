package com.bindord.financemanagement.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer, ServletContextAware {

  private ServletContext context;

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.context = servletContext;
  }

  private static final Logger LOGGER = LogManager.getLogger(WebMvcConfiguration.class);

  public WebMvcConfiguration() {

  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(jacksonMessageConverter());
    WebMvcConfigurer.super.configureMessageConverters(converters);
  }

  @Bean
  public MappingJackson2HttpMessageConverter jacksonMessageConverter() {
    MappingJackson2HttpMessageConverter messageConverter =
        new MappingJackson2HttpMessageConverter();

    List<MediaType> supportedMediaTypes = new ArrayList<>();
    supportedMediaTypes.add(MediaType.APPLICATION_JSON);
    supportedMediaTypes.add(MediaType.TEXT_PLAIN);

    messageConverter.setSupportedMediaTypes(supportedMediaTypes);
    var objectMapper = new HibernateAwareObjectMapper();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.registerModule(new JavaTimeModule());
    messageConverter.setObjectMapper(objectMapper);
    messageConverter.setPrettyPrint(true);

    return messageConverter;
  }
}
