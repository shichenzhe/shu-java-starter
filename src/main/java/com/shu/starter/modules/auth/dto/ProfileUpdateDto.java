package com.shu.starter.modules.auth.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/** 更新当前用户信息DTO：仅允许维护个人基础资料 */
@Data
public class ProfileUpdateDto {
  private String name;
  private String phone;
  private String email;
  private String note;

  @Email(message = "邮箱格式不正确")
  public String getEmail() {
    return email;
  }
}
