package com.shu.starter.common.web;

import com.shu.starter.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
  private String code;
  private boolean success;
  private T data;
  private String message;
  private String traceId;

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(
        ErrorCode.SUCCESS.code(), true, data, "操作成功", MDC.get(TraceIdFilter.TRACE_ID));
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(code, false, null, message, MDC.get(TraceIdFilter.TRACE_ID));
  }
}
