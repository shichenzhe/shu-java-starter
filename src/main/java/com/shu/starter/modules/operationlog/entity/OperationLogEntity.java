package com.shu.starter.modules.operationlog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("operation_log")
public class OperationLogEntity {
  // ASSIGN_UUID：String 主键路由到 UuidIdentifierGenerator.nextUUID()（UUIDv7），见 BaseEntity 注释
  @TableId(value = "id", type = IdType.ASSIGN_UUID)
  private String id;
  private String userId;
  private String operation;
  private String details;
  private String ipAddress;
  private String userAgent;
  private Integer executionTime;
  private String status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;
}
