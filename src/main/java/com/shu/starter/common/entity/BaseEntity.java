package com.shu.starter.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public abstract class BaseEntity {
  /**
   * MP 3.5.17 中 ASSIGN_ID 走 nextId()（数值雪花），仅 ASSIGN_UUID + String 主键才路由到
   * IdentifierGenerator.nextUUID()（即 UuidIdentifierGenerator 的 UUIDv7），故此处用 ASSIGN_UUID。
   */
  @TableId(type = IdType.ASSIGN_UUID)
  private String id;

  @TableField(fill = FieldFill.INSERT)
  private String creatorId;

  @TableField(fill = FieldFill.INSERT)
  private String creatorName;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;

  @TableField(fill = FieldFill.UPDATE)
  private String updatorId;

  @TableField(fill = FieldFill.UPDATE)
  private String updatorName;
}
