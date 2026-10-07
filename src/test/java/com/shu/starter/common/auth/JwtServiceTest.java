package com.shu.starter.common.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.shu.starter.common.config.AppProps;
import com.shu.starter.common.entity.UserType;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
  private AppProps.Jwt jwt(String secret) {
    return new AppProps.Jwt(secret, Duration.ofMinutes(30), Duration.ofHours(12));
  }

  @Test
  void 生成并校验令牌_回传主体信息() {
    JwtService service = new JwtService(jwt("0123456789abcdef0123456789abcdef"));
    UserPrincipal principal =
        new UserPrincipal("00000000-0000-0000-0000-000000000001", "系统管理员", UserType.admin);
    JwtService.TokenPair pair = service.generateTokens(principal);
    assertEquals(principal, service.verify(pair.accessToken()));
    assertEquals(principal, service.verify(pair.refreshToken()));
  }

  @Test
  void 篡改令牌校验失败() {
    JwtService service = new JwtService(jwt("0123456789abcdef0123456789abcdef"));
    UserPrincipal principal = new UserPrincipal("id-1", "u", UserType.user);
    String token = service.generateTokens(principal).accessToken();
    assertThrows(Exception.class, () -> service.verify(token + "x"));
  }

  @Test
  void 过期令牌校验失败() {
    // 负 TTL：签发即已过期（expiration=now-1s），避免 TTL=0 同毫秒解析的理论竞态
    JwtService service =
        new JwtService(new AppProps.Jwt("s", Duration.ofMillis(-1000), Duration.ofMillis(-1000)));
    UserPrincipal principal = new UserPrincipal("id-1", "u", UserType.user);
    String token = service.generateTokens(principal).accessToken();
    assertThrows(Exception.class, () -> service.verify(token));
  }

  @Test
  void 短密钥也能派生_占位符形态可用() {
    JwtService service = new JwtService(jwt("{{JWT_SECRET}}"));
    UserPrincipal principal = new UserPrincipal("id-1", "u", UserType.user);
    assertEquals(principal, service.verify(service.generateTokens(principal).accessToken()));
  }
}
