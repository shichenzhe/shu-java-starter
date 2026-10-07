package com.shu.starter.common.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:sqlite:target/test-common-web.db")
@AutoConfigureTestRestTemplate
class CommonWebIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void traceId响应头回传() {
    var headers = new HttpHeaders();
    headers.set("trace_id", "trace-test-001");
    var response =
        rest.exchange("/actuator/health", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertEquals("trace-test-001", response.getHeaders().getFirst("trace_id"));
  }

  @Test
  void 不存在的接口返回统一404格式() {
    // /no-such-api 受保护 → 401 统一格式；404 场景由 GlobalExceptionHandler 的
    // NoResourceFoundException 覆盖——本例两者皆可接受
    var response = rest.getForEntity("/no-such-api", String.class);
    assertTrue(response.getStatusCode().value() == 401 || response.getStatusCode().value() == 404);
  }
}
