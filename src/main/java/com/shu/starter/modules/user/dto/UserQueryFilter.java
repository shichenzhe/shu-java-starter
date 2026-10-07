package com.shu.starter.modules.user.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shu.starter.common.entity.UserType;
import com.shu.starter.common.query.QueryFilter;
import com.shu.starter.modules.user.entity.UserEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryFilter extends QueryFilter {
  private String keyword;
  private UserType userType;
  private Boolean isActive;
  private String username;

  /** 对应 TS 版 querydecoder：过滤条件 → MyBatis-Plus Wrapper */
  public LambdaQueryWrapper<UserEntity> toWrapper() {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isBlank()) {
      String kw = "%" + keyword + "%";
      wrapper.and(w -> w.like(UserEntity::getUsername, kw).or().like(UserEntity::getName, kw));
    }
    wrapper.eq(userType != null, UserEntity::getUserType, userType);
    wrapper.eq(isActive != null, UserEntity::getIsActive, isActive);
    wrapper.eq(username != null && !username.isBlank(), UserEntity::getUsername, username);
    wrapper.orderByDesc(UserEntity::getCreatedAt);
    return wrapper;
  }
}
