package com.bindord.financemanagement.aop;

import com.bindord.financemanagement.config.JacksonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(value = -2)
public class AspectController {

  private static final Logger LOGGER = LogManager.getLogger(AspectController.class);
  private static final ObjectMapper mapper = JacksonFactory.getObjectMapper();

  @Before(value = "within(com.bindord.financemanagement.controller..*) && !@annotation(com.bindord.financemanagement.annotation.NoLogging)",
      argNames = "joinPoint")
  private void before(JoinPoint joinPoint) {
    String caller = joinPoint.getSignature().toShortString();
    LOGGER.info("{} method called.", caller);

    if (LOGGER.isInfoEnabled()) {
      Object[] args = joinPoint.getArgs();

      for (Object arg : args) {
        if (arg == null) continue;

        // 🧩 Skip types that are not meant to be serialized (like HttpServletRequest, Response, Session, etc.)
        if (arg instanceof org.springframework.web.context.request.NativeWebRequest ||
            arg instanceof jakarta.servlet.http.HttpServletRequest ||
            arg instanceof jakarta.servlet.http.HttpServletResponse ||
            arg instanceof jakarta.servlet.http.HttpSession) {
          continue;
        }

        try {
          String json = mapper.writeValueAsString(arg);
          LOGGER.debug(">> Input: {}", json);
        } catch (JsonProcessingException e) {
          LOGGER.warn("Could not serialize argument of type {}: {}", arg.getClass().getName(), e.getMessage());
        }
      }
    }
  }

  @AfterReturning(value = "within(com.bindord.financemanagement.controller..*) && !@annotation(com.bindord.financemanagement.annotation.NoLogging)",
      returning = "returnValue")
  private void after(JoinPoint joinPoint, Object returnValue) {
    // You can add response logging here if desired
    if (returnValue != null) {
      try {
        String json = mapper.writeValueAsString(returnValue);
        LOGGER.debug("<< Output: {}", json);
      } catch (JsonProcessingException e) {
        LOGGER.warn("Could not serialize return value of type {}: {}", returnValue.getClass().getName(), e.getMessage());
      }
    }
  }
}
