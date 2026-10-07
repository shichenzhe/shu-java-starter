package com.shu.starter.modules.option.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("option")
public class OptionEntity {
  // ASSIGN_UUID：String 主键路由到 UuidIdentifierGenerator.nextUUID()（UUIDv7），见 BaseEntity 注释
  @TableId(value = "id", type = IdType.ASSIGN_UUID)
  private String id;
  private String type;
  private String name;
  private String value;
  private String note;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updatedAt;
}
