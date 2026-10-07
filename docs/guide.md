# 如何添加业务模块

以模板自带的 **user 模块**为参照，新增一个业务模块共七步。假设新模块叫 `note`。

## 第 1 步：建表（Flyway 迁移）

在 `src/main/resources/db/migration/sqlite/` 新增 `V3__note.sql`（版本号顺延，当前已有
V1 建表、V2 seed）：

```sql
CREATE TABLE note (
  id              VARCHAR(36)   PRIMARY KEY,
  title           VARCHAR(200)  NOT NULL,
  content         VARCHAR(2000),
  creator_id      VARCHAR(36),
  creator_name    VARCHAR(100),
  updator_id      VARCHAR(36),
  updator_name    VARCHAR(100),
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_note_creator_id ON note (creator_id);
```

同步在 `src/main/resources/db/migration/postgresql/` 新增内容一致的 `V3__note.sql`
（两方言目录必须逐版本同步；仅布尔等类型有差异，写法参考 V1 的 `is_active`：
SQLite 用 `INTEGER DEFAULT 1`，PostgreSQL 用 `BOOLEAN DEFAULT TRUE`）。

重启应用，Flyway 自动执行新版本迁移。SQLite 本地开发也可直接删 `data/app.db`
从头重建（会清空数据）。表不建外键，关联在 service 层维护（对齐 TS 版）。

## 第 2 步：entity

`src/main/java/com/shu/starter/modules/note/entity/NoteEntity.java`——继承 BaseEntity
（id 为 UUIDv7 字符串、creator/updator/时间戳审计字段自动填充，参考 UserEntity）。
查询过滤（QueryFilter 子类）放 dto 包，见第 4 步：

```java
package com.shu.starter.modules.note.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shu.starter.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("note")
public class NoteEntity extends BaseEntity {
  private String title;
  private String content;
}
```

（表名与 SQL 保留字冲突时加引号转义，如 user 表的 `@TableName("\"user\"")`。）

## 第 3 步：mapper

`src/main/java/com/shu/starter/modules/note/mapper/NoteMapper.java`——继承 BaseMapper
即获得 insert/selectById/selectPage/updateById/deleteById 等全套方法（参考 UserMapper）：

```java
package com.shu.starter.modules.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shu.starter.modules.note.entity.NoteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<NoteEntity> {}
```

## 第 4 步：dto

`src/main/java/com/shu/starter/modules/note/dto/`——入参加 Bean Validation 校验
（message 一律中文），出参做字段白名单 + `static from()` 转换（参考 user/dto）：

```java
package com.shu.starter.modules.note.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteCreateDto {
  @NotBlank(message = "标题不能为空")
  private String title;

  private String content;
}
```

```java
package com.shu.starter.modules.note.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteIdDto {
  @NotBlank(message = "id 不能为空")
  private String id;
}
```

```java
package com.shu.starter.modules.note.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shu.starter.common.query.QueryFilter;
import com.shu.starter.modules.note.entity.NoteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoteQueryFilter extends QueryFilter {
  private String keyword;

  /** 对应 TS 版 querydecoder：过滤条件 → MyBatis-Plus Wrapper */
  public LambdaQueryWrapper<NoteEntity> toWrapper() {
    LambdaQueryWrapper<NoteEntity> wrapper = new LambdaQueryWrapper<>();
    if (keyword != null && !keyword.isBlank()) {
      String kw = "%" + keyword + "%";
      wrapper.and(w -> w.like(NoteEntity::getTitle, kw).or().like(NoteEntity::getContent, kw));
    }
    wrapper.orderByDesc(NoteEntity::getCreatedAt);
    return wrapper;
  }
}
```

```java
package com.shu.starter.modules.note.dto;

import com.shu.starter.modules.note.entity.NoteEntity;
import java.time.LocalDateTime;
import lombok.Data;

/** 出参白名单：不含多余字段，对应 TS 版 UserDto 的 @Expose 白名单 */
@Data
public class NoteDto {
  private String id;
  private String title;
  private String content;
  private String creatorName;
  private LocalDateTime createdAt;

  public static NoteDto from(NoteEntity entity) {
    NoteDto dto = new NoteDto();
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setContent(entity.getContent());
    dto.setCreatorName(entity.getCreatorName());
    dto.setCreatedAt(entity.getCreatedAt());
    return dto;
  }
}
```

## 第 5 步：service

`src/main/java/com/shu/starter/modules/note/service/NoteService.java`——业务逻辑与校验；
错误一律 `throw BusinessException.of(...)`（参考 UserService）：

```java
package com.shu.starter.modules.note.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shu.starter.common.exception.BusinessException;
import com.shu.starter.common.exception.ErrorCode;
import com.shu.starter.common.query.QueryResult;
import com.shu.starter.modules.note.dto.NoteCreateDto;
import com.shu.starter.modules.note.dto.NoteDto;
import com.shu.starter.modules.note.dto.NoteQueryFilter;
import com.shu.starter.modules.note.entity.NoteEntity;
import com.shu.starter.modules.note.mapper.NoteMapper;
import org.springframework.stereotype.Service;

@Service
public class NoteService {
  private final NoteMapper noteMapper;

  public NoteService(NoteMapper noteMapper) {
    this.noteMapper = noteMapper;
  }

  public QueryResult<NoteDto> query(NoteQueryFilter filter) {
    Page<NoteEntity> page =
        noteMapper.selectPage(
            new Page<>(filter.getPageNum(), filter.getPageSize()), filter.toWrapper());
    return QueryResult.of(
        page.getRecords().stream().map(NoteDto::from).toList(),
        page.getTotal(),
        filter.getPageNum(),
        filter.getPageSize());
  }

  public NoteDto create(NoteCreateDto dto) {
    NoteEntity entity = new NoteEntity();
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    noteMapper.insert(entity); // id 与审计字段自动填充
    return NoteDto.from(noteMapper.selectById(entity.getId()));
  }

  public NoteDto getById(String id) {
    return NoteDto.from(requireNote(id));
  }

  public void delete(String id) {
    requireNote(id);
    noteMapper.deleteById(id);
  }

  private NoteEntity requireNote(String id) {
    NoteEntity entity = noteMapper.selectById(id);
    if (entity == null) {
      throw BusinessException.of("笔记不存在：" + id, ErrorCode.NOT_FOUND);
    }
    return entity;
  }
}
```

## 第 6 步：controller

`src/main/java/com/shu/starter/modules/note/controller/NoteController.java`——POST 风格路由 +
权限注解 + springdoc 注解（参考 UserController）：

```java
package com.shu.starter.modules.note.controller;

import com.shu.starter.common.query.QueryResult;
import com.shu.starter.modules.note.dto.NoteCreateDto;
import com.shu.starter.modules.note.dto.NoteDto;
import com.shu.starter.modules.note.dto.NoteIdDto;
import com.shu.starter.modules.note.dto.NoteQueryFilter;
import com.shu.starter.modules.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "笔记管理")
@RestController
@RequestMapping("/note")
public class NoteController {
  private final NoteService noteService;

  public NoteController(NoteService noteService) {
    this.noteService = noteService;
  }

  @Operation(summary = "创建笔记")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/create")
  public NoteDto create(@Valid @RequestBody NoteCreateDto dto) {
    return noteService.create(dto);
  }

  @Operation(summary = "笔记列表")
  @PostMapping("/query")
  public QueryResult<NoteDto> query(@RequestBody NoteQueryFilter filter) {
    return noteService.query(filter);
  }

  @Operation(summary = "获取笔记详情")
  @PostMapping("/getById")
  public NoteDto getById(@Valid @RequestBody NoteIdDto dto) {
    return noteService.getById(dto.getId());
  }

  @Operation(summary = "删除笔记")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/delete")
  public void delete(@Valid @RequestBody NoteIdDto dto) {
    noteService.delete(dto.getId());
  }
}
```

要点：

- 默认所有接口需登录；限角色加 `@PreAuthorize("hasRole('ADMIN')")`；需要当前用户信息时
  加参数 `@AuthenticationPrincipal UserPrincipal principal`（参考 AuthController）
- controller 不返回裸 String（会绕过统一 JSON 包装）；返回 `void` 或对象均可，
  `ResponseWrapperAdvice` 自动包成 `{code, success, data, message, traceId}`
- 单 id 入参用独立 DTO（`NoteIdDto`，参考 UserIdDto），不用 `@PathVariable`

## 第 7 步：无需注册（自动扫描）

Spring 自动发现一切：`@SpringBootApplication` 在包根 `com.shu.starter`，其下任意子包的
`@RestController`/`@Service` 自动装配，mapper 靠 `@Mapper` 注解被 MyBatis-Plus 拾取。
对比 TS 版要新建 `note.module.ts` 并在 `app.module.ts` 注册——Java 版没有这一步，
建好文件启动即可用。

操作日志同样无需接线：`OperationLogInterceptor` 已全局注册（默认启用，
`app.operation-log.enabled: false` 可关），新接口自动记录（用户/IP/耗时/状态）。

完成。删模块 = 反向删除上述产物（注意：已执行过的 Flyway 迁移文件不要直接删除，
Flyway 会校验历史；开发库可删 `data/app.db` 重建，生产库用新迁移做 drop）。
