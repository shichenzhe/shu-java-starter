package com.shu.starter.common.exception;

/** 错误码（对齐 shu-nestjs-starter 的 ErrorCodeEnum，Java 版补充 4000） */
public enum ErrorCode {
  SUCCESS("2000"),
  BAD_REQUEST("4000"),
  UNAUTHORIZED("4010"),
  FORBIDDEN("4030"),
  NOT_FOUND("4040"),
  METHOD_NOT_ALLOWED("4050"),
  UNSUPPORTED_MEDIA_TYPE("4150"),
  INTERNAL_SERVER_ERROR("5000");

  private final String code;

  ErrorCode(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public int httpStatus() {
    return Integer.parseInt(code.substring(0, 3));
  }
}
