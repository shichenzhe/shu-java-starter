package com.shu.starter.modules.auth.dto;

import com.shu.starter.modules.user.dto.UserDto;
import lombok.Data;

/** 登录响应DTO：双令牌 + 用户信息 */
@Data
public class LoginResponseDto {
  private String accessToken;
  private String refreshToken;
  private UserDto user;
}
