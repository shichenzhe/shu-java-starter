package com.shu.starter.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 启用/禁用用户DTO */
@Data
public class UserActiveDto {
  @NotBlank(message = "用户id不能为空")
  private String id;

  @NotNull(message = "用户状态不能为空")
  private Boolean isActive;
}
