package com.shu.starter.common.auth;

import com.shu.starter.common.web.TraceIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/login",
                        // 对齐 TS 版 @Public()：refresh 凭 body 内 refreshToken 鉴权，
                        // 由 AuthService.refresh 自行校验并统一 401
                        "/auth/refresh",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                    (request, response, ex) -> {
                      response.setStatus(401);
                      // 显式 UTF-8：否则 Tomcat 默认 ISO-8859-1，中文 message 输出为 '?'
                      response.setCharacterEncoding("UTF-8");
                      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      response
                          .getWriter()
                          .write(
                              "{\"code\":\"4010\",\"success\":false,\"data\":null,"
                                  + "\"message\":\"未认证或令牌无效\",\"traceId\":"
                                  + (request.getHeader(TraceIdFilter.TRACE_ID) == null
                                      ? "null"
                                      : "\""
                                          + request.getHeader(TraceIdFilter.TRACE_ID)
                                          + "\"")
                                  + "}");
                    }))
        // 过滤器不注册为 Bean（无 @Component）：避免 Boot 把 Filter Bean 再自动登记进
        // servlet 容器链造成双注册，仅在此处经 addFilterBefore 挂入 Security 链一次
        .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
