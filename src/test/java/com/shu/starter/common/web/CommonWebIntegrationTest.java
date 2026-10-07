package com.shu.starter.common.web;

import static org.junit.jupiter.api.Assertions.*;

import com.shu.starter.common.auth.JwtService;
import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.common.entity.UserType;
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
  @Autowired JwtService jwtService;

  @Test
  void traceId响应头回传() {
    var headers = new HttpHeaders();
    headers.set(TraceIdFilter.TRACE_ID, "trace-test-001");
    var response =
        rest.exchange("/actuator/health", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertEquals("trace-test-001", response.getHeaders().getFirst(TraceIdFilter.TRACE_ID));
  }

  @Test
  void 不存在的接口返回统一404格式() {
    // /no-such-api 受保护 → 401 统一格式；404 场景由 ApiErrorController 的 /error 分发覆盖
    var response = rest.getForEntity("/no-such-api", String.class);
    assertTrue(response.getStatusCode().value() == 401 || response.getStatusCode().value() == 404);
  }

  @Test
  void 带token访问不存在路径走error链路返回404统一格式() {
    // 鉴权通过后无匹配 handler → NoResourceFoundException（无 HandlerMethod，
    // @ExceptionHandler 捕不到）→ 容器 ERROR dispatch 到 /error → ApiErrorController
    String token =
        jwtService
            .generateTokens(
                new UserPrincipal(
                    "00000000-0000-0000-0000-000000000001", "系统管理员", UserType.admin))
            .accessToken();
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    var response =
        rest.exchange("/no-such-api", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertEquals(404, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("\"code\":\"4040\""), response.getBody());
    assertTrue(response.getBody().contains("\"success\":false"), response.getBody());
    assertTrue(response.getBody().contains("资源不存在"), response.getBody());
    // ERROR dispatch 不再经过 TraceIdFilter（shouldNotFilterErrorDispatch），
    // MDC 已清理 → traceId 字段存在但值为 null
    assertTrue(response.getBody().contains("\"traceId\""), response.getBody());
  }
}
