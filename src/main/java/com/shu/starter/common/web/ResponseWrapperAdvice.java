package com.shu.starter.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.shu.starter.modules")
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {
  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {
    // 文件下载（byte[]/Resource）与已是 ApiResponse 的返回不包装
    return !ApiResponse.class.isAssignableFrom(returnType.getParameterType())
        && !byte[].class.isAssignableFrom(returnType.getParameterType())
        && !Resource.class.isAssignableFrom(returnType.getParameterType());
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body instanceof ApiResponse<?>) return body;
    return ApiResponse.ok(body);
  }
}
