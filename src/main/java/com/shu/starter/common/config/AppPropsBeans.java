package com.shu.starter.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌套配置记录提升为独立 Bean。
 *
 * <p>@ConfigurationProperties 仅把外层 AppProps 注册为 Bean，嵌套 record（如 AppProps.Jwt）
 * 不会单独成 Bean；JwtService 以 AppProps.Jwt 为构造参数注入，此处统一桥接，
 * 后续其他嵌套配置（OperationLog 等）按需追加。
 */
@Configuration
public class AppPropsBeans {

  @Bean
  AppProps.Jwt appJwtProps(AppProps appProps) {
    return appProps.jwt();
  }
}
