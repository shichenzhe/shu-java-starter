package com.shu.starter.common.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private final OperationLogInterceptor operationLogInterceptor;

  public WebMvcConfig(OperationLogInterceptor operationLogInterceptor) {
    this.operationLogInterceptor = operationLogInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // /error 为容器错误分发（404/405/415 等），非业务操作：排除以免多记一行 operation_log
    registry
        .addInterceptor(operationLogInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/error");
  }
}
