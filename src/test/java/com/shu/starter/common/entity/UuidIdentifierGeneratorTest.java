package com.shu.starter.common.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UuidIdentifierGeneratorTest {
  private final UuidIdentifierGenerator generator = new UuidIdentifierGenerator();

  @Test
  void 生成的id为36字符UUID格式() {
    String id = generator.nextUUID(new Object());
    assertTrue(id.matches("^[0-9a-f-]{36}$"));
  }

  @Test
  void 生成的id版本位为7且唯一() {
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      String id = generator.nextUUID(new Object());
      assertEquals('7', id.charAt(14)); // version nibble
      assertTrue(seen.add(id));
    }
  }

  @Test
  void 趋势递增_时间有序() {
    String a = generator.nextUUID(new Object());
    String b = generator.nextUUID(new Object());
    assertTrue(a.compareTo(b) < 0);
  }
}
