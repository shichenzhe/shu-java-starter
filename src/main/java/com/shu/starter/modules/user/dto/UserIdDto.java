package com.shu.starter.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 仅携带 id 的请求体（getById/resetPassword/delete），对齐 TS 版 commons/entity/entity.ts */
@Data
public class UserIdDto {
  @NotBlank(message = "用户id不能为空")
  private String id;
}
