package com.shu.starter.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 占位符替换引擎（纯函数，被 Init.java 与单测共用；行为对齐 shu-nestjs-starter 的 replace.js） */
public final class Replacer {
  static final List<String> PLACEHOLDERS = List.of("APP_NAME", "JWT_SECRET", "AUTHOR", "REPO_URL");
  private static final String CONFIG_FILE = "shu-init.json";
  private static final Set<String> TARGET_EXTS = Set.of(".yml", ".yaml", ".xml", ".md", ".java", ".properties", "");
  private static final Set<String> IGNORE_DIRS =
      Set.of("target", ".git", ".mvn", ".github", ".idea", "logs", "data", "scripts", "test");

  private Replacer() {}

  public static List<Path> applyPlaceholders(Path rootDir, Map<String, String> values)
      throws IOException {
    Map<String, String> oldValues = readOldValues(rootDir.resolve(CONFIG_FILE));
    List<Path> changed = new ArrayList<>();
    try (var stream = Files.walk(rootDir)) {
      stream.filter(Files::isRegularFile).filter(file -> isTarget(rootDir, file)).forEach(file -> {
        try {
          String content = Files.readString(file);
          String original = content;
          for (String key : PLACEHOLDERS) {
            String next = values.getOrDefault(key, "");
            content = content.replace("{{" + key + "}}", next);
            String old = oldValues.get(key);
            if (old != null && !old.isEmpty() && !old.equals(next)) {
              content = content.replace(old, next);
            }
          }
          if (!content.equals(original)) {
            Files.writeString(file, content);
            changed.add(file);
          }
        } catch (IOException e) {
          throw new UncheckedIOExceptionWrapper(e);
        }
      });
    }
    Files.writeString(rootDir.resolve(CONFIG_FILE), toJson(values));
    return changed;
  }

  /** 读 shu-init.json（不存在返回空 Map）；正则解析 "KEY": "value"，含转义还原 */
  static Map<String, String> readOldValues(Path file) {
    Map<String, String> result = new LinkedHashMap<>();
    String content;
    try {
      content = Files.readString(file);
    } catch (IOException e) {
      return result; // 首次 init，无旧配置
    }
    StringBuilder keyPattern = new StringBuilder();
    for (int i = 0; i < PLACEHOLDERS.size(); i++) {
      if (i > 0) {
        keyPattern.append('|');
      }
      keyPattern.append(PLACEHOLDERS.get(i));
    }
    java.util.regex.Matcher m = java.util.regex.Pattern
        .compile("\"(" + keyPattern + ")\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        .matcher(content);
    while (m.find()) {
      result.put(m.group(1), unescape(m.group(2)));
    }
    return result;
  }

  /**
   * 扩展名在 TARGET_EXTS 且相对根目录的路径第一段不在忽略集。
   * 注意：必须按相对路径前缀判断——src/main/java 的第一段是 src 正常扫描，
   * 而 src/test 需要精确忽略（"test" 只作目录名匹配会漏掉 src/test 子树）。
   */
  private static boolean isTarget(Path rootDir, Path file) {
    String ext = extOf(file.getFileName().toString());
    if (!TARGET_EXTS.contains(ext)) {
      return false;
    }
    Path relative = rootDir.relativize(file);
    int nameCount = relative.getNameCount();
    for (int i = 0; i < nameCount; i++) {
      String segment = relative.getName(i).toString();
      if (i < nameCount - 1 && IGNORE_DIRS.contains(segment)) {
        return false; // 位于忽略目录的子树内
      }
      // 精确忽略 src/test 子树（test 源码/资源是脚手架自测试，不属于用户模板面）
      if (i == 0 && nameCount > 2
          && "src".equals(segment) && "test".equals(relative.getName(1).toString())) {
        return false;
      }
    }
    return true;
  }

  private static String extOf(String name) {
    int dot = name.lastIndexOf('.');
    return dot == -1 ? "" : name.substring(dot);
  }

  /** 手写最小 JSON 序列化（{"APP_NAME": "x",...}），不引依赖 */
  private static String toJson(Map<String, String> values) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (String key : PLACEHOLDERS) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append('"').append(key).append("\": \"").append(escape(values.getOrDefault(key, ""))).append('"');
    }
    return sb.append("}\n").toString();
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static String unescape(String s) {
    return s.replace("\\\"", "\"").replace("\\\\", "\\");
  }

  /** 内部类：把 lambda 中的受检 IOException 转为运行时异常抛出 */
  private static final class UncheckedIOExceptionWrapper extends RuntimeException {
    UncheckedIOExceptionWrapper(IOException cause) {
      super(cause);
    }
  }
}
