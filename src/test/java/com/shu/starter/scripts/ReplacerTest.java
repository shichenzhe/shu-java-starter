package com.shu.starter.scripts;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReplacerTest {
  private static final List<String> KEYS = List.of("APP_NAME", "JWT_SECRET", "AUTHOR", "REPO_URL");

  private Map<String, String> values(String appName) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("APP_NAME", appName);
    map.put("JWT_SECRET", "abc");
    map.put("AUTHOR", "someone");
    map.put("REPO_URL", "");
    return map;
  }

  @Test
  void 替换占位符并返回被修改文件(@TempDir Path dir) throws IOException {
    Path yml = dir.resolve("application.yml");
    Files.writeString(yml, "name: '{{APP_NAME}}'");
    Path readme = dir.resolve("README.md");
    Files.writeString(readme, "author: {{AUTHOR}}");

    List<Path> changed = Replacer.applyPlaceholders(dir, values("my-service"));

    assertEquals(2, changed.size());
    assertEquals("name: 'my-service'", Files.readString(yml));
  }

  @Test
  void 幂等_旧值也被替换(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("app.md");
    Files.writeString(file, "title: {{APP_NAME}}");

    Replacer.applyPlaceholders(dir, values("first"));
    List<Path> changed = Replacer.applyPlaceholders(dir, values("second"));

    assertEquals("title: second", Files.readString(file));
    assertTrue(changed.contains(file));
  }

  @Test
  void 忽略目标目录_不扫描scripts与tests(@TempDir Path dir) throws IOException {
    Files.createDirectories(dir.resolve("target"));
    Files.writeString(dir.resolve("target/app.md"), "{{APP_NAME}}");
    Path root = dir.resolve("app.md");
    Files.writeString(root, "{{APP_NAME}}");

    List<Path> changed = Replacer.applyPlaceholders(dir, values("x"));

    assertEquals(List.of(root), changed);
  }

  @Test
  void 写入shuInitJson(@TempDir Path dir) throws IOException {
    Replacer.applyPlaceholders(dir, values("x"));
    String json = Files.readString(dir.resolve("shu-init.json"));
    assertTrue(json.contains("\"APP_NAME\": \"x\""));
  }
}
