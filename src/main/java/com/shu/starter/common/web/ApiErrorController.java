package com.shu.starter.common.web;

import com.shu.starter.common.exception.ErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一错误分发：NoResourceFoundException（404）由 ResourceHttpRequestHandler 抛出、
 * 405/415 由 HandlerMapping 抛出——均无 HandlerMethod，@RestControllerAdvice 的
 * @ExceptionHandler 捕获不到，由容器 ERROR dispatch 到 /error，在此统一为 ApiResponse。
 *
 * <p>实现 ErrorController（Boot 4 位于 org.springframework.boot.webmvc.error）使
 * BasicErrorController 退位（@ConditionalOnMissingBean(ErrorController)）；Security 过滤器
 * 默认 dispatcher-types 不含 ERROR，/error 无需 permitAll。ResponseWrapperAdvice 的
 * basePackages=modules 不覆盖本类，返回的 ApiResponse 不会被二次包装。
 */
@RestController
public class ApiErrorController implements ErrorController {
  @RequestMapping("/error")
  public ResponseEntity<ApiResponse<Void>> error(HttpServletRequest request) {
    Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int status = code instanceof Integer i ? i : 500;
    ErrorCode errorCode = switch (status) {
      case 400 -> ErrorCode.BAD_REQUEST;
      case 401 -> ErrorCode.UNAUTHORIZED;
      case 403 -> ErrorCode.FORBIDDEN;
      case 404 -> ErrorCode.NOT_FOUND;
      case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
      case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
      default -> ErrorCode.INTERNAL_SERVER_ERROR;
    };
    String message = switch (status) {
      case 404 -> "资源不存在";
      case 405 -> "方法不允许";
      case 415 -> "不支持的媒体类型";
      default -> "服务器内部错误";
    };
    return ResponseEntity.status(status).body(ApiResponse.error(errorCode.code(), message));
  }
}
