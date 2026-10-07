package com.shu.starter.common.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 启动兜底：确保相对路径 data/ 目录在任何启动路径下存在。
 *
 * <p>SQLite URL（jdbc:sqlite:data/app.db）要求父目录存在，否则 Hikari/Flyway 首次建连即
 * SQLITE_CANTOPEN 启动失败；模板中 data/ 被 .gitignore 忽略，全新克隆后该目录并不存在。
 * 本类保证“未执行 init 脚本也能直接启动”。
 */
@Component
public class DataDirInitializer implements InitializingBean {

  @Override
  public void afterPropertiesSet() throws IOException {
    try {
      Files.createDirectories(Path.of("data"));
    } catch (FileAlreadyExistsException ignored) {
      // 目录已存在时 createDirectories 本就静默成功，此处仅为语义兜底
    }
  }
}
