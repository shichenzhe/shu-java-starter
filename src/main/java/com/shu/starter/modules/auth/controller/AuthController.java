package com.shu.starter.modules.auth.controller;

import com.shu.starter.common.auth.JwtService;
import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.modules.auth.dto.ChangePasswordDto;
import com.shu.starter.modules.auth.dto.LoginDto;
import com.shu.starter.modules.auth.dto.LoginResponseDto;
import com.shu.starter.modules.auth.dto.ProfileUpdateDto;
import com.shu.starter.modules.auth.dto.TokenRefreshDto;
import com.shu.starter.modules.auth.service.AuthService;
import com.shu.starter.modules.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "用户登录")
  @PostMapping("/login")
  public LoginResponseDto login(@Valid @RequestBody LoginDto dto) {
    return authService.login(dto);
  }

  @Operation(summary = "刷新访问令牌")
  @PostMapping("/refresh")
  public JwtService.TokenPair refresh(@Valid @RequestBody TokenRefreshDto dto) {
    return authService.refresh(dto);
  }

  @Operation(summary = "修改密码")
  @PostMapping("/changePassword")
  public Map<String, String> changePassword(
      @Valid @RequestBody ChangePasswordDto dto,
      @AuthenticationPrincipal UserPrincipal principal) {
    authService.changePassword(principal.id(), dto);
    // 不返回裸 String：String 会走 StringHttpMessageConverter 绕过统一 JSON 包装，
    // 此处返回 Map 对齐 TS 版 {message} 结构
    return Map.of("message", "密码修改成功，请重新登录");
  }

  @Operation(summary = "获取当前用户信息")
  @PostMapping("/profile/get")
  public UserDto getProfile(@AuthenticationPrincipal UserPrincipal principal) {
    return authService.getProfile(principal.id());
  }

  @Operation(summary = "更新当前用户信息")
  @PostMapping("/profile/update")
  public UserDto updateProfile(
      @Valid @RequestBody ProfileUpdateDto dto,
      @AuthenticationPrincipal UserPrincipal principal) {
    return authService.updateProfile(principal.id(), dto);
  }
}
