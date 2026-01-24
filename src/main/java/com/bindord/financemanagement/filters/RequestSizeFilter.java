package com.bindord.financemanagement.filters;

import com.bindord.financemanagement.model.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestSizeFilter extends OncePerRequestFilter {

  @Value("${server.custom.max-content-length.in-mb}")
  private Integer maxRequestSize;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    long size = request.getContentLengthLong();

    if (size > (maxRequestSize * 1024 * 1024)) {

      ApiError apiError = new ApiError(
          "Payload too large, exceeds the maximum allowed size of " + maxRequestSize + " MB",
          "Request size exceeds the maximum allowed size of " + maxRequestSize + " MB"
      );

      response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      response.getWriter().write(
          objectMapper.writeValueAsString(apiError)
      );
      response.getWriter().flush();
      return;
    }

    filterChain.doFilter(request, response);
  }
}