package com.shu.starter.common.exception;

import com.shu.starter.common.web.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 404/405/415 无 HandlerMethod，@ExceptionHandler 捕不到：由 ApiErrorController 的 /error 统一分发

@RestControllerAdvice(basePackages = "com.shu.starter.modules")
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> business(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.httpStatus())
        .body(ApiResponse.error(code.code(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> invalid(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getDefaultMessage())
            .findFirst()
            .orElse("请求参数不合法");
    return ResponseEntity.badRequest()
        .body(ApiResponse.error(ErrorCode.BAD_REQUEST.code(), message));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> forbidden(AccessDeniedException e) {
    return ResponseEntity.status(403)
        .body(ApiResponse.error(ErrorCode.FORBIDDEN.code(), "无权访问该资源"));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> unauthorized(AuthenticationException e) {
    return ResponseEntity.status(401)
        .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.code(), "未认证或令牌无效"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> fallback(Exception e) {
    log.error("未处理异常", e);
    return ResponseEntity.status(500)
        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.code(), "服务器内部错误"));
  }
}
