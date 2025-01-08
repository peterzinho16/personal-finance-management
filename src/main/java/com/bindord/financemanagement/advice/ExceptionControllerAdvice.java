package com.bindord.financemanagement.advice;

import com.bindord.financemanagement.model.exception.ApiError;
import com.bindord.financemanagement.model.exception.ApiSubError;
import com.bindord.financemanagement.model.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.ArrayList;
import java.util.List;

import static com.bindord.financemanagement.config.JacksonFactory.getObjectMapper;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

  private static final ObjectMapper mapper = getObjectMapper();

  private static final String SQL_UNIQUE_VIOLATION_CODE = "23505";
  private static final String SQL_DUP_EXCEP_PREFIX = "duplicate key value violates unique " +
      "constraint";
  private static final String SQL_DUP_EXCEP_PREFIX_ES = "llave duplicada viola restricción de " +
      "unicidad";
  private static final String TEMP_UNIQUE_CONS_ONE = "uk8jmdau039u32ktqkckcdgnvkt";
  private static final String TEMP_UNIQUE_CONS_TWO = "uk_7rr530m3pxabetp6s9r0fjp37";
  private static final String TEMP_UC_ONE_MSG = "¡El código del producto ya ha sido escaneado con" +
      " anterioridad, no puede haber códigos duplicados!";
  private static final String TEMP_UC_TWO_MSG = "El usuario que intenta registrar, ya ha sido " +
      "registrado con anterioridad. Si no lo encuentra en su lista de contactos, es porque este " +
      "ha sido registrado por otro vendedor.";

  public static final String BINDING_ERROR = "Validation has failed";

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(WebExchangeBindException.class)
  public ApiError handleBindException(WebExchangeBindException ex) {
    log.warn("method {}", "handleBindException");
    ex.getModel().entrySet().forEach(e -> {
      log.warn(e.getKey() + ": " + e.getValue());
    });
    List<ApiSubError> errors = new ArrayList<>();

    for (FieldError x : ex.getBindingResult().getFieldErrors()) {
      errors.add(new ApiSubError(x.getObjectName(), x.getField(), x.getRejectedValue(),
          x.getDefaultMessage()));
    }
    return new ApiError(BINDING_ERROR, errors);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ApiError handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("method {}", "handleIllegalArgumentException");
    ex.printStackTrace();
    return new ApiError(ex.getMessage(), ex);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundValidationException.class)
  public @ResponseBody
  ApiError handlerNotFoundValidationException(NotFoundValidationException ex) {
    log.warn("method {}", "handlerNotFoundValidationException");
    return new ApiError(ex.getMessage(), ex);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public @ResponseBody
  ErrorResponse handlerDataIntegrityViolationException(DataIntegrityViolationException ex) {
    log.warn("method {}", "handlerDataIntegrityViolationException");

    DataIntegrityViolationException cve = null;
    boolean isCVE = ex.getCause() instanceof DataIntegrityViolationException;

    if (ex.getCause() instanceof DataIntegrityViolationException r2dbcExcep) {
      cve = r2dbcExcep;
    }

    String mostSpecCause = ex.getMostSpecificCause().toString();
    log.warn(mostSpecCause);
    for (int i = 0; i < ex.getStackTrace().length; i++) {
      log.warn(ex.getStackTrace()[i].toString());
    }

    if (mostSpecCause.contains(SQL_DUP_EXCEP_PREFIX) ||
        mostSpecCause.contains(SQL_DUP_EXCEP_PREFIX_ES)) {
      if (mostSpecCause.contains(TEMP_UNIQUE_CONS_ONE)) {
        return new ErrorResponse(TEMP_UC_ONE_MSG, SQL_UNIQUE_VIOLATION_CODE);
      }

      if (mostSpecCause.contains(TEMP_UNIQUE_CONS_TWO)) {
        return new ErrorResponse(TEMP_UC_TWO_MSG, SQL_UNIQUE_VIOLATION_CODE);
      }
    }
    return new ErrorResponse(mostSpecCause, SQL_UNIQUE_VIOLATION_CODE);

  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadSqlGrammarException.class)
  public @ResponseBody
  ApiError handlerBadSqlGrammarException(BadSqlGrammarException ex) {
    log.warn("method {}", "handlerBadSqlGrammarException");
    return new ApiError(ex.getMessage(), ex.getSQLException().getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ApiError handleConstraintViolationException(ConstraintViolationException ex) {
    log.warn("method {}", "handleConstraintViolationException");
    log.warn(ex.getMessage());
    return new ApiError(ex.getMessage(), ex);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ServerWebInputException.class)
  public @ResponseBody
  ApiError handleServerWebInputException(ServerWebInputException ex) {
    log.warn("method {}", "handleServerWebInputException");
    return new ApiError(ex.getMessage(), ex);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(CustomValidationException.class)
  public @ResponseBody
  ErrorResponse handlerCustomValidationException(CustomValidationException ex) {
    log.warn("method {}", "handlerCustomValidationException");
    log.warn(ex.getMessage());
    for (int i = 0; i < ex.getStackTrace().length; i++) {
      log.warn(ex.getStackTrace()[i].toString());
    }
    return new ErrorResponse(ex.getMessage(), ex.getInternalCode());
  }
}


