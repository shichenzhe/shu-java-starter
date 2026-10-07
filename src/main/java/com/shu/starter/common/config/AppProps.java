package com.shu.starter.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProps(String name, Jwt jwt, OperationLog operationLog) {
  public record Jwt(String secret, Duration expiresIn, Duration refreshExpiresIn) {}

  public record OperationLog(boolean enabled) {}
}
