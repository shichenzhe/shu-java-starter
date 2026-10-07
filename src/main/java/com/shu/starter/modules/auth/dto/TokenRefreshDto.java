package com.shu.starter.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshDto {
  @NotBlank(message = "刷新令牌不能为空")
  private String refreshToken;
}
