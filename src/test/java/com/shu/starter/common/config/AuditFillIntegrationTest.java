package com.shu.starter.common.config;

import static org.junit.jupiter.api.Assertions.*;

import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.common.entity.UserType;
import com.shu.starter.modules.user.entity.UserEntity;
import com.shu.starter.modules.user.mapper.UserMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 审计填充回归（Task 2 评审 M-2 闭环）：
 * 验证 MetaObjectHandler 的 currentOperator() 无登录态回落 SYSTEM、有登录态取 UserPrincipal。
 */
@SpringBootTest
class AuditFillIntegrationTest {
  @Autowired UserMapper userMapper;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private UserEntity newUser() {
    UserEntity e = new UserEntity();
    e.setUsername("audit-" + UUID.randomUUID());
    e.setPasswordHash("not-a-real-hash");
    e.setName("审计测试用户");
    e.setUserType(UserType.user);
    e.setIsActive(true);
    return e;
  }

  @Test
  void 无登录态insert审计字段填充SYSTEM() {
    // 前置清理：防止其他用例遗留的登录态污染"无登录态"前提（JUnit 用例顺序不保证）
    SecurityContextHolder.clearContext();
    UserEntity e = newUser();
    userMapper.insert(e);
    UserEntity reloaded = userMapper.selectById(e.getId());
    assertEquals("init", reloaded.getCreatorId());
    assertEquals("init", reloaded.getCreatorName());
  }

  @Test
  void 登录态下insert与update审计字段取principal身份() {
    UserPrincipal principal =
        new UserPrincipal("00000000-0000-0000-0000-000000000001", "系统管理员", UserType.admin);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

    UserEntity e = newUser();
    userMapper.insert(e);
    UserEntity inserted = userMapper.selectById(e.getId());
    assertEquals(principal.id(), inserted.getCreatorId());
    assertEquals(principal.name(), inserted.getCreatorName());

    inserted.setName("改名后");
    userMapper.updateById(inserted);
    UserEntity updated = userMapper.selectById(e.getId());
    assertEquals("改名后", updated.getName());
    assertEquals(principal.id(), updated.getUpdatorId());
    assertEquals(principal.name(), updated.getUpdatorName());
  }
}
