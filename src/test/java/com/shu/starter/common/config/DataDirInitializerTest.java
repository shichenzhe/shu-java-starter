package com.shu.starter.common.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DataDirInitializerTest {

  @Test
  void 启动时创建data目录且幂等() throws IOException {
    DataDirInitializer initializer = new DataDirInitializer();
    initializer.afterPropertiesSet();
    assertTrue(Files.isDirectory(Path.of("data")));
    // 已存在时再次执行不抛异常（幂等）
    assertDoesNotThrow(initializer::afterPropertiesSet);
  }
}
