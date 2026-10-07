package com.shu.starter.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shu.starter.common.entity.BaseEntity;
import com.shu.starter.common.entity.UserType;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("\"user\"")
public class UserEntity extends BaseEntity {
  private String username;
  private String passwordHash;
  private String name;
  private String phone;
  private String email;
  private String note;
  private UserType userType;
  private Boolean isActive;
  private LocalDateTime lastLoginTime;
}
