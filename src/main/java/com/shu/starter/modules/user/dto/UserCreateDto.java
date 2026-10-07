package com.shu.starter.modules.user.dto;

import com.shu.starter.common.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateDto {
  @NotBlank(message = "用户名不能为空")
  private String username;

  @NotBlank(message = "密码不能为空")
  private String password;

  @NotBlank(message = "姓名不能为空")
  private String name;

  @NotNull(message = "用户类型不能为空")
  private UserType userType;

  private String phone;
  private String email;
  private String note;

  @Email(message = "邮箱格式不正确")
  public String getEmail() {
    return email;
  }
}
