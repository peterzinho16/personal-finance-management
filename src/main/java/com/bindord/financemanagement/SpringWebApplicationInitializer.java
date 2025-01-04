package com.bindord.financemanagement;

import com.bindord.financemanagement.config.WebMvcConfiguration;
import jakarta.servlet.Filter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class SpringWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  public static final String CHARACTER_ENCODING = "UTF-8";


  public SpringWebApplicationInitializer() {
    super();
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class<?>[] { WebMvcConfiguration.class };
  }

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[0];
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

  @Override
  protected Filter[] getServletFilters() {
    final CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
    encodingFilter.setEncoding(CHARACTER_ENCODING);
    encodingFilter.setForceEncoding(true);
    return new Filter[] { encodingFilter };
  }

}