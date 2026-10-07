package com.shu.starter.common.auth;

import com.shu.starter.common.config.AppProps;
import com.shu.starter.common.entity.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  // 拼接构造，防止 init 全文替换波及本文件的比对字符串
  private static final String PLACEHOLDER_SECRET = "{{" + "JWT_SECRET" + "}}";
  private static final String LEGACY_DEFAULT_SECRET = "3C4F4BD192D0489FE0632F0C11ACBF35";

  private final AppProps.Jwt props;
  private final SecretKey key;

  public JwtService(AppProps.Jwt props) {
    this.props = props;
    // jjwt 要求 HS256 密钥 >= 32 字节：统一 SHA-256 派生，占位符短串也能工作。
    // 定案：派生放构造器，@PostConstruct 仅做告警检测，单测直接 new 无需手动 init。
    try {
      byte[] derived =
          MessageDigest.getInstance("SHA-256")
              .digest(props.secret().getBytes(StandardCharsets.UTF_8));
      this.key = Keys.hmacShaKeyFor(derived);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public record TokenPair(String accessToken, String refreshToken) {}

  @PostConstruct
  void init() {
    if (PLACEHOLDER_SECRET.equals(props.secret()) || LEGACY_DEFAULT_SECRET.equals(props.secret())) {
      org.slf4j.LoggerFactory.getLogger(JwtService.class)
          .warn("[安全告警] app.jwt.secret 仍为模板默认值，请执行 init 完成初始化后再用于生产环境！");
    }
  }

  public TokenPair generateTokens(UserPrincipal principal) {
    return new TokenPair(
        buildToken(principal, props.expiresIn()),
        buildToken(principal, props.refreshExpiresIn()));
  }

  private String buildToken(UserPrincipal principal, Duration ttl) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.id())
        .claim("name", principal.name())
        .claim("userType", principal.userType().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  public UserPrincipal verify(String token) {
    Claims claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return new UserPrincipal(
        claims.getSubject(),
        claims.get("name", String.class),
        UserType.valueOf(claims.get("userType", String.class)));
  }
}
