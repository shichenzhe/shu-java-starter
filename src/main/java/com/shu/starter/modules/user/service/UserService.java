package com.shu.starter.modules.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shu.starter.common.exception.BusinessException;
import com.shu.starter.common.exception.ErrorCode;
import com.shu.starter.common.query.QueryResult;
import com.shu.starter.modules.user.dto.*;
import com.shu.starter.modules.user.entity.UserEntity;
import com.shu.starter.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  public QueryResult<UserDto> query(UserQueryFilter filter) {
    Page<UserEntity> page =
        userMapper.selectPage(
            new Page<>(filter.getPageNum(), filter.getPageSize()), filter.toWrapper());
    return QueryResult.of(
        page.getRecords().stream().map(UserDto::from).toList(),
        page.getTotal(),
        filter.getPageNum(),
        filter.getPageSize());
  }

  public UserDto create(UserCreateDto dto) {
    UserEntity existing =
        userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, dto.getUsername()));
    if (existing != null) {
      throw BusinessException.of("用户名已存在：" + dto.getUsername(), ErrorCode.BAD_REQUEST);
    }
    UserEntity entity = new UserEntity();
    entity.setUsername(dto.getUsername());
    entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    entity.setName(dto.getName());
    entity.setUserType(dto.getUserType());
    entity.setPhone(dto.getPhone());
    entity.setEmail(dto.getEmail());
    entity.setNote(dto.getNote());
    entity.setIsActive(true);
    userMapper.insert(entity);
    return UserDto.from(userMapper.selectById(entity.getId()));
  }

  public UserDto getById(String id) {
    UserEntity entity = requireUser(id);
    return UserDto.from(entity);
  }

  public UserDto update(UserUpdateDto dto) {
    UserEntity entity = requireUser(dto.getId());
    if (dto.getName() != null) entity.setName(dto.getName());
    if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
    if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
    if (dto.getNote() != null) entity.setNote(dto.getNote());
    if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    userMapper.updateById(entity);
    return UserDto.from(userMapper.selectById(entity.getId()));
  }

  public void active(String id, boolean isActive) {
    UserEntity entity = requireUser(id);
    entity.setIsActive(isActive);
    userMapper.updateById(entity);
  }

  public void resetPassword(String id) {
    UserEntity entity = requireUser(id);
    entity.setPasswordHash(passwordEncoder.encode("123456"));
    userMapper.updateById(entity);
  }

  public void delete(String id) {
    requireUser(id);
    userMapper.deleteById(id);
  }

  public UserEntity requireUser(String id) {
    UserEntity entity = userMapper.selectById(id);
    if (entity == null) {
      throw BusinessException.of("用户不存在：" + id, ErrorCode.NOT_FOUND);
    }
    return entity;
  }

  public UserEntity login(String username, String password) {
    UserEntity entity =
        userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
    if (entity == null || !passwordEncoder.matches(password, entity.getPasswordHash())) {
      throw BusinessException.of("用户名或密码错误", ErrorCode.UNAUTHORIZED);
    }
    if (!Boolean.TRUE.equals(entity.getIsActive())) {
      throw BusinessException.of("账号已被禁用", ErrorCode.FORBIDDEN);
    }
    entity.setLastLoginTime(LocalDateTime.now());
    userMapper.updateById(entity);
    return entity;
  }

  public void changePassword(String id, String oldPassword, String newPassword) {
    UserEntity entity = requireUser(id);
    if (!passwordEncoder.matches(oldPassword, entity.getPasswordHash())) {
      throw BusinessException.of("原密码不正确", ErrorCode.BAD_REQUEST);
    }
    entity.setPasswordHash(passwordEncoder.encode(newPassword));
    userMapper.updateById(entity);
  }
}
