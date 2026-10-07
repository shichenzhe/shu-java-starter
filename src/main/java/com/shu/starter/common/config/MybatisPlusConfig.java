package com.shu.starter.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.common.entity.Operator;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class MybatisPlusConfig {

  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor(Environment environment) {
    // 分页方言随激活 profile 走：postgresql → PG，默认（sqlite）→ SQLite
    DbType dbType =
        environment.acceptsProfiles(Profiles.of("postgresql")) ? DbType.POSTGRE_SQL : DbType.SQLITE;
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(dbType);
    pagination.setOptimizeJoin(true);
    interceptor.addInnerInterceptor(pagination);
    return interceptor;
  }

  /**
   * 审计字段自动填充：登录态取 SecurityContext，否则 SYSTEM。
   *
   * <p>注：以 MetaObjectHandler Bean 暴露（而非 GlobalConfig Bean）——MyBatis-Plus starter
   * 的自动配置从 MybatisPlusProperties 派生 GlobalConfig，仅按类型拾取容器中的
   * MetaObjectHandler / IdentifierGenerator Bean，用户自定义 GlobalConfig Bean 不会被使用。
   * UuidIdentifierGenerator 为 @Component，由自动配置直接拾取注入。
   */
  @Bean
  public MetaObjectHandler auditMetaObjectHandler() {
    return new MetaObjectHandler() {
      @Override
      public void insertFill(MetaObject metaObject) {
        Operator op = currentOperator();
        strictInsertFill(metaObject, "creatorId", String.class, op.id());
        strictInsertFill(metaObject, "creatorName", String.class, op.name());
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
      }

      @Override
      public void updateFill(MetaObject metaObject) {
        Operator op = currentOperator();
        strictUpdateFill(metaObject, "updatorId", String.class, op.id());
        strictUpdateFill(metaObject, "updatorName", String.class, op.name());
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
      }

      /** 登录态取 SecurityContext 中的 UserPrincipal，否则 SYSTEM */
      private Operator currentOperator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
          return new Operator(p.id(), p.name());
        }
        return Operator.SYSTEM;
      }
    };
  }
}
