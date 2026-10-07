package com.shu.starter.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 更新用户DTO：id 必填，其余字段可选（null 表示不修改） */
@Data
public class UserUpdateDto {
  @NotBlank(message = "用户id不能为空")
  private String id;

  private String name;
  private String phone;
  private String email;
  private String note;
  private Boolean isActive;

  @Email(message = "邮箱格式不正确")
  public String getEmail() {
    return email;
  }
}
