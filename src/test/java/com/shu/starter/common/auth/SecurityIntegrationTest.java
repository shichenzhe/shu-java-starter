package com.shu.starter.common.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:sqlite:target/test-security.db")
@AutoConfigureTestRestTemplate
class SecurityIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void 无token访问受保护接口返回401统一格式() {
    var response = rest.getForEntity("/user/query", String.class);
    assertEquals(401, response.getStatusCode().value());
    assertTrue(response.getBody().contains("\"code\":\"4010\""));
  }
}
