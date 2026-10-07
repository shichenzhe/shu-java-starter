package com.shu.starter.modules.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 认证与用户管理端到端冒烟（对齐 TS 版行为 oracle）：统一 ApiResponse 包装、双令牌、
 * RBAC 403、错误码格式；并覆盖 Task 4 deferred 的 OperationLogInterceptor 端到端验证。
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:sqlite:target/test-auth-flow.db")
@AutoConfigureTestRestTemplate
class AuthFlowIntegrationTest {
  private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

  @Autowired TestRestTemplate rest;
  @Autowired JdbcTemplate jdbc;

  @SuppressWarnings("unchecked")
  private Map<String, Object> login() {
    var response =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", "admin", "password", "123456"), jsonHeaders()),
            Map.class);
    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> body = response.getBody();
    assertEquals("2000", body.get("code"));
    assertEquals(true, body.get("success"));
    return (Map<String, Object>) body.get("data");
  }

  @Test
  void 登录返回双令牌与用户信息() {
    Map<String, Object> data = login();
    assertTrue(((String) data.get("accessToken")).length() > 20);
    assertNotNull(data.get("refreshToken"));
    Map<String, Object> user = (Map<String, Object>) data.get("user");
    assertEquals("admin", user.get("username"));
    assertEquals(ADMIN_ID, user.get("id"));
  }

  @Test
  void 刷新令牌签发新双令牌() {
    Map<String, Object> data = login();
    var response =
        rest.postForEntity(
            "/auth/refresh",
            new HttpEntity<>(Map.of("refreshToken", data.get("refreshToken")), jsonHeaders()),
            Map.class);
    assertEquals(200, response.getStatusCode().value());
    assertEquals("2000", response.getBody().get("code"));
    Map<String, Object> tokens = (Map<String, Object>) response.getBody().get("data");
    assertTrue(((String) tokens.get("accessToken")).length() > 20);
    assertNotNull(tokens.get("refreshToken"));
  }

  @Test
  void 带token查询用户列表() {
    String token = (String) login().get("accessToken");
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    var response = rest.postForEntity("/user/query", new HttpEntity<>(Map.of(), headers), Map.class);
    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
    assertTrue(((Number) data.get("total")).longValue() >= 1);
  }

  @Test
  void user角色访问管理接口返回403() {
    // 先用 admin 创建一个 user 角色账号，登录后访问 /user/query → 403
    // username 追加时间戳：sqlite 库文件在多次 ./mvnw test 之间复用，固定名会撞唯一约束
    String username = "u01-" + System.currentTimeMillis();
    String token = (String) login().get("accessToken");
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    var create =
        rest.postForEntity(
            "/user/create",
            new HttpEntity<>(
                Map.of(
                    "username", username,
                    "password", "123456",
                    "name", "普通用户一",
                    "userType", "user"),
                headers),
            Map.class);
    assertEquals(200, create.getStatusCode().value());
    var login2 =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", username, "password", "123456"), jsonHeaders()),
            Map.class);
    String userToken =
        (String) ((Map<String, Object>) login2.getBody().get("data")).get("accessToken");
    var userHeaders = jsonHeaders();
    userHeaders.setBearerAuth(userToken);
    var forbidden =
        rest.postForEntity("/user/query", new HttpEntity<>(Map.of(), userHeaders), Map.class);
    assertEquals(403, forbidden.getStatusCode().value());
    assertEquals("4030", forbidden.getBody().get("code"));
  }

  @Test
  void 错误密码返回统一错误格式() {
    var response =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", "admin", "password", "wrong1"), jsonHeaders()),
            Map.class);
    assertEquals(401, response.getStatusCode().value());
    assertEquals("4010", response.getBody().get("code"));
  }

  @Test
  void 普通用户自助更新资料与修改密码() {
    // admin 建普通账号 → 以该账号登录走 profile/update、profile/get、changePassword 全链路
    String username = "prof-" + System.currentTimeMillis();
    String id = createUser(username, "123456");
    String userToken = loginAs(username, "123456");
    var headers = jsonHeaders();
    headers.setBearerAuth(userToken);

    var update =
        rest.postForEntity(
            "/auth/profile/update",
            new HttpEntity<>(Map.of("name", "资料改后", "phone", "13800000000"), headers),
            Map.class);
    assertEquals(200, update.getStatusCode().value());
    assertEquals("资料改后", ((Map<String, Object>) update.getBody().get("data")).get("name"));

    var get =
        rest.postForEntity("/auth/profile/get", new HttpEntity<>(Map.of(), headers), Map.class);
    assertEquals(200, get.getStatusCode().value());
    Map<String, Object> profile = (Map<String, Object>) get.getBody().get("data");
    assertEquals(id, profile.get("id"));
    assertEquals(username, profile.get("username"));
    assertEquals("资料改后", profile.get("name"));

    var change =
        rest.postForEntity(
            "/auth/changePassword",
            new HttpEntity<>(Map.of("oldPassword", "123456", "newPassword", "abc12345"), headers),
            Map.class);
    assertEquals(200, change.getStatusCode().value());
    assertEquals(
        "密码修改成功，请重新登录",
        ((Map<String, Object>) change.getBody().get("data")).get("message"));

    var oldLogin =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", username, "password", "123456"), jsonHeaders()),
            Map.class);
    assertEquals(401, oldLogin.getStatusCode().value());
    assertNotNull(loginAs(username, "abc12345"));
  }

  @Test
  void 管理员用户管理全套冒烟() {
    String adminToken = (String) login().get("accessToken");
    var headers = jsonHeaders();
    headers.setBearerAuth(adminToken);
    // 初始密码非 123456，便于验证 resetPassword 重置回默认密码
    String username = "mg-" + System.currentTimeMillis();
    String id = createUser(username, "pass1234");

    var got =
        rest.postForEntity(
            "/user/getById", new HttpEntity<>(Map.of("id", id), headers), Map.class);
    assertEquals(200, got.getStatusCode().value());
    assertEquals(username, ((Map<String, Object>) got.getBody().get("data")).get("username"));

    var upd =
        rest.postForEntity(
            "/user/update", new HttpEntity<>(Map.of("id", id, "name", "管理全套改"), headers), Map.class);
    assertEquals(200, upd.getStatusCode().value());
    assertEquals("管理全套改", ((Map<String, Object>) upd.getBody().get("data")).get("name"));

    var disable =
        rest.postForEntity(
            "/user/active", new HttpEntity<>(Map.of("id", id, "isActive", false), headers), Map.class);
    assertEquals(200, disable.getStatusCode().value());
    assertEquals("2000", disable.getBody().get("code"));
    var disabledLogin =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", username, "password", "pass1234"), jsonHeaders()),
            Map.class);
    assertEquals(403, disabledLogin.getStatusCode().value());
    assertEquals("4030", disabledLogin.getBody().get("code"));

    rest.postForEntity(
        "/user/active", new HttpEntity<>(Map.of("id", id, "isActive", true), headers), Map.class);
    assertNotNull(loginAs(username, "pass1234"));

    var reset =
        rest.postForEntity(
            "/user/resetPassword", new HttpEntity<>(Map.of("id", id), headers), Map.class);
    assertEquals(200, reset.getStatusCode().value());
    assertEquals("2000", reset.getBody().get("code"));
    assertNotNull(loginAs(username, "123456"));

    var del =
        rest.postForEntity("/user/delete", new HttpEntity<>(Map.of("id", id), headers), Map.class);
    assertEquals(200, del.getStatusCode().value());
    var gone =
        rest.postForEntity(
            "/user/getById", new HttpEntity<>(Map.of("id", id), headers), Map.class);
    assertEquals(404, gone.getStatusCode().value());
    assertEquals("4040", gone.getBody().get("code"));
  }

  /** 以 admin 身份创建用户，返回新用户 id */
  @SuppressWarnings("unchecked")
  private String createUser(String username, String password) {
    String adminToken = (String) login().get("accessToken");
    var headers = jsonHeaders();
    headers.setBearerAuth(adminToken);
    var create =
        rest.postForEntity(
            "/user/create",
            new HttpEntity<>(
                Map.of(
                    "username", username,
                    "password", password,
                    "name", "业务测试用户",
                    "userType", "user"),
                headers),
            Map.class);
    assertEquals(200, create.getStatusCode().value());
    return (String) ((Map<String, Object>) create.getBody().get("data")).get("id");
  }

  /** 指定账号密码登录，返回 accessToken（断言 200/2000） */
  @SuppressWarnings("unchecked")
  private String loginAs(String username, String password) {
    var response =
        rest.postForEntity(
            "/auth/login",
            new HttpEntity<>(Map.of("username", username, "password", password), jsonHeaders()),
            Map.class);
    assertEquals(200, response.getStatusCode().value());
    assertEquals("2000", response.getBody().get("code"));
    return (String) ((Map<String, Object>) response.getBody().get("data")).get("accessToken");
  }

  @Test
  void 操作日志拦截器端到端落库并记录操作者() throws InterruptedException {
    // Task 4 deferred 义务：登录后调用业务接口，operation_log 新增一行且 userId 为 admin 的 id
    long before = countOperationLog(ADMIN_ID);
    String token = (String) login().get("accessToken");
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    var response = rest.postForEntity("/user/query", new HttpEntity<>(Map.of(), headers), Map.class);
    assertEquals(200, response.getStatusCode().value());
    // afterCompletion 落库与响应回传存在毫秒级竞态，短暂轮询等待
    long after = before;
    for (int i = 0; i < 50 && after <= before; i++) {
      Thread.sleep(100);
      after = countOperationLog(ADMIN_ID);
    }
    assertEquals(before + 1, after);
  }

  private long countOperationLog(String userId) {
    Long count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM operation_log WHERE user_id = ?", Long.class, userId);
    return count == null ? 0 : count;
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
