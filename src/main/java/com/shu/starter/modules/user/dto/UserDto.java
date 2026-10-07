package com.shu.starter.modules.user.dto;

import com.shu.starter.common.entity.UserType;
import com.shu.starter.modules.user.entity.UserEntity;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户出参白名单：不含 passwordHash 与审计字段，对应 TS 版 UserDto 的 @Expose 白名单 */
@Data
public class UserDto {
  private String id;
  private String username;
  private String name;
  private String phone;
  private String email;
  private String note;
  private UserType userType;
  private Boolean isActive;
  private LocalDateTime lastLoginTime;

  public static UserDto from(UserEntity entity) {
    UserDto dto = new UserDto();
    dto.setId(entity.getId());
    dto.setUsername(entity.getUsername());
    dto.setName(entity.getName());
    dto.setPhone(entity.getPhone());
    dto.setEmail(entity.getEmail());
    dto.setNote(entity.getNote());
    dto.setUserType(entity.getUserType());
    dto.setIsActive(entity.getIsActive());
    dto.setLastLoginTime(entity.getLastLoginTime());
    return dto;
  }
}
