package com.shu.starter.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** 交互式初始化：java src/main/java/com/shu/starter/scripts/Init.java（JDK 25 单文件直跑） */
public class Init {
  public static void main(String[] args) throws Exception {
    Path rootDir = Path.of("").toAbsolutePath();
    // 从运行目录向上定位仓库根（含 pom.xml）；单文件运行时 cwd 即仓库根
    while (!Files.exists(rootDir.resolve("pom.xml")) && rootDir.getParent() != null) {
      rootDir = rootDir.getParent();
    }
    System.out.printf("%n=== shu-java-starter 初始化 ===%n%n");
    // System.console() 在管道/重定向输入下为 null，统一用 BufferedReader(System.in)：
    // 交互式与管道两种场景行为一致；readLine 返回 null（EOF）按回车空串处理走默认值
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    Map<String, String> old = readOld(rootDir.resolve("shu-init.json"));

    String appName = ask(reader, "应用名称（kebab-case，如 my-service）", "APP_NAME", old, "my-service")
        .replaceAll("[^a-zA-Z0-9-]", "-").toLowerCase();
    String jwtSecret =
        ask(reader, "JWT 签名密钥（留空自动生成随机密钥，生产环境必须替换）", "JWT_SECRET", old, "");
    if (jwtSecret.isBlank()) {
      byte[] bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
      jwtSecret = Base64.getEncoder().encodeToString(bytes);
      System.out.println("  已生成随机密钥：" + jwtSecret);
    }
    String author = ask(reader, "作者", "AUTHOR", old, "");
    String repoUrl = ask(reader, "仓库地址（可留空）", "REPO_URL", old, "");

    Map<String, String> values = new LinkedHashMap<>();
    values.put("APP_NAME", appName);
    values.put("JWT_SECRET", jwtSecret);
    values.put("AUTHOR", author);
    values.put("REPO_URL", repoUrl);

    System.out.printf("%n正在替换占位符…%n");
    var changed = Replacer.applyPlaceholders(rootDir, values);
    if (repoUrl.isBlank()) {
      removeScmBlock(rootDir.resolve("pom.xml"));
      System.out.println("  未填仓库地址：已移除 pom.xml 的 <scm> 块");
    }
    System.out.printf("已更新 %d 个文件（本次输入保存在 shu-init.json，重复运行可覆盖更新）%n", changed.size());
    System.out.printf("%n✓ 初始化完成！下一步：%n");
    System.out.println("  ./mvnw spring-boot:run");
  }

  private static String ask(
      BufferedReader reader, String question, String key, Map<String, String> old, String fallback)
      throws IOException {
    String def = old.getOrDefault(key, fallback);
    String suffix = def == null || def.isBlank() ? "" : " (" + def + ")";
    System.out.print(question + suffix + ": ");
    // print 不带换行，管道/行缓冲终端下显式 flush 保证提示语先于等待输入可见
    System.out.flush();
    String answer = reader.readLine();
    if (answer == null) return def == null ? "" : def;
    answer = answer.trim();
    return answer.isBlank() ? (def == null ? "" : def) : answer;
  }

  private static Map<String, String> readOld(Path file) {
    return Replacer.readOldValues(file);
  }

  private static void removeScmBlock(Path pom) {
    try {
      String content = Files.readString(pom);
      content = content.replaceAll("(?s)\\s*<scm>.*?</scm>", "");
      Files.writeString(pom, content);
    } catch (Exception e) {
      System.err.println("移除 scm 块失败（可手动处理）: " + e.getMessage());
    }
  }
}
