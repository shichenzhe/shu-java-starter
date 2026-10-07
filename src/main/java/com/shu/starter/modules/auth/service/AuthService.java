package com.shu.starter.modules.auth.service;

import com.shu.starter.common.auth.JwtService;
import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.common.exception.BusinessException;
import com.shu.starter.common.exception.ErrorCode;
import com.shu.starter.modules.auth.dto.ChangePasswordDto;
import com.shu.starter.modules.auth.dto.LoginDto;
import com.shu.starter.modules.auth.dto.LoginResponseDto;
import com.shu.starter.modules.auth.dto.ProfileUpdateDto;
import com.shu.starter.modules.auth.dto.TokenRefreshDto;
import com.shu.starter.modules.user.dto.UserDto;
import com.shu.starter.modules.user.dto.UserUpdateDto;
import com.shu.starter.modules.user.entity.UserEntity;
import com.shu.starter.modules.user.service.UserService;
import org.springframework.stereotype.Service;

/** 认证编排：登录/刷新/改密/个人资料，用户校验与持久化复用 UserService（对齐 TS 版） */
@Service
public class AuthService {
  private final UserService userService;
  private final JwtService jwtService;

  public AuthService(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  /** 登录：校验通过 → UserPrincipal → generateTokens → LoginResponseDto */
  public LoginResponseDto login(LoginDto dto) {
    UserEntity user = userService.login(dto.getUsername(), dto.getPassword());
    UserPrincipal principal = new UserPrincipal(user.getId(), user.getName(), user.getUserType());
    JwtService.TokenPair tokens = jwtService.generateTokens(principal);
    LoginResponseDto response = new LoginResponseDto();
    response.setAccessToken(tokens.accessToken());
    response.setRefreshToken(tokens.refreshToken());
    response.setUser(UserDto.from(user));
    return response;
  }

  /** 刷新：verify(refreshToken) 通过后重新签发双令牌；无效/过期统一 401（对齐 TS 版） */
  public JwtService.TokenPair refresh(TokenRefreshDto dto) {
    try {
      UserPrincipal principal = jwtService.verify(dto.getRefreshToken());
      return jwtService.generateTokens(principal);
    } catch (Exception e) {
      throw BusinessException.of("登录会话已过期：令牌无效或已过期", ErrorCode.UNAUTHORIZED);
    }
  }

  public void changePassword(String userId, ChangePasswordDto dto) {
    userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
  }

  public UserDto getProfile(String userId) {
    return userService.getById(userId);
  }

  /** 个人资料更新：复用 UserService.update，仅放开 name/phone/email/note（对齐 TS 版固定 isActive=true） */
  public UserDto updateProfile(String userId, ProfileUpdateDto dto) {
    UserUpdateDto update = new UserUpdateDto();
    update.setId(userId);
    update.setName(dto.getName());
    update.setPhone(dto.getPhone());
    update.setEmail(dto.getEmail());
    update.setNote(dto.getNote());
    update.setIsActive(true);
    return userService.update(update);
  }
}
