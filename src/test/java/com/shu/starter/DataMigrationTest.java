package com.shu.starter;

import static org.junit.jupiter.api.Assertions.*;

import com.shu.starter.modules.user.entity.UserEntity;
import com.shu.starter.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataMigrationTest {
  @Autowired UserMapper userMapper;

  @Test
  void flyway建库并seed初始admin() {
    UserEntity admin = userMapper.selectById("00000000-0000-0000-0000-000000000001");
    assertNotNull(admin);
    assertEquals("admin", admin.getUsername());
    assertEquals("admin", admin.getUserType().name());
    assertTrue(admin.getIsActive());
  }
}
