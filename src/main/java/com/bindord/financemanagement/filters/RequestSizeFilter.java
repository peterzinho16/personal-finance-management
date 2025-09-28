package com.bindord.financemanagement.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestSizeFilter extends OncePerRequestFilter {

  private static final long MAX_REQUEST_SIZE = 5 * 1024 * 1024; // 3MB

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    long size = request.getContentLengthLong();

    if (size > MAX_REQUEST_SIZE) {
      response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
          "Payload too large (max 5 MB)");
      return;
    }

    filterChain.doFilter(request, response);
  }
}