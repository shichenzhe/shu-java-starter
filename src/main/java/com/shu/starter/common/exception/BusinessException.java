package com.shu.starter.common.exception;

public class BusinessException extends RuntimeException {
  private final ErrorCode errorCode;

  public BusinessException(String message) {
    this(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }

  public BusinessException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public static BusinessException of(String message) {
    return new BusinessException(message);
  }

  public static BusinessException of(String message, ErrorCode errorCode) {
    return new BusinessException(message, errorCode);
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
