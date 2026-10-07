package com.shu.starter.modules.option;

import static org.junit.jupiter.api.Assertions.*;

import com.shu.starter.modules.option.service.OptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 系统选项读写：覆盖 setOption 的 insert 与 update 两个分支（(type,name) 唯一索引） */
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/test-option.db")
class OptionServiceIntegrationTest {
  @Autowired OptionService optionService;

  @Test
  void setOption首次插入再次更新可读回() {
    String type = "site-" + System.currentTimeMillis();
    assertNull(optionService.getOption(type, "title"));
    optionService.setOption(type, "title", "v1");
    assertEquals("v1", optionService.getOption(type, "title"));
    optionService.setOption(type, "title", "v2");
    assertEquals("v2", optionService.getOption(type, "title"));
  }
}
