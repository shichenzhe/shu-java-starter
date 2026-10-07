package com.shu.starter.modules.user.controller;

import com.shu.starter.common.query.QueryResult;
import com.shu.starter.modules.user.dto.UserActiveDto;
import com.shu.starter.modules.user.dto.UserCreateDto;
import com.shu.starter.modules.user.dto.UserDto;
import com.shu.starter.modules.user.dto.UserIdDto;
import com.shu.starter.modules.user.dto.UserQueryFilter;
import com.shu.starter.modules.user.dto.UserUpdateDto;
import com.shu.starter.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理端点（全部仅 ADMIN）。操作者身份不显式传参：审计字段（creator/updator）由
 * MetaObjectHandler 从 SecurityContext 的 UserPrincipal 自动填充，操作日志由
 * OperationLogInterceptor 落库，controller 只表达业务本身。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "创建用户")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/create")
  public UserDto create(@Valid @RequestBody UserCreateDto dto) {
    return userService.create(dto);
  }

  @Operation(summary = "查询用户列表")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/query")
  public QueryResult<UserDto> query(@RequestBody UserQueryFilter filter) {
    return userService.query(filter);
  }

  @Operation(summary = "获取用户详情")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/getById")
  public UserDto getById(@Valid @RequestBody UserIdDto dto) {
    return userService.getById(dto.getId());
  }

  @Operation(summary = "更新用户信息")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/update")
  public UserDto update(@Valid @RequestBody UserUpdateDto dto) {
    return userService.update(dto);
  }

  @Operation(summary = "启用/禁用用户")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/active")
  public void active(@Valid @RequestBody UserActiveDto dto) {
    userService.active(dto.getId(), dto.getIsActive());
  }

  @Operation(summary = "重置用户密码（重置为123456）")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/resetPassword")
  public void resetPassword(@Valid @RequestBody UserIdDto dto) {
    userService.resetPassword(dto.getId());
  }

  @Operation(summary = "删除用户")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/delete")
  public void delete(@Valid @RequestBody UserIdDto dto) {
    userService.delete(dto.getId());
  }
}
