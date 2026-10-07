package com.shu.starter.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 不加 @Component / 不注册为 Bean：由 SecurityConfig 显式构造并经 addFilterBefore 挂入
 * Security 链一次，避免 Boot 将 Filter Bean 额外自动登记进 servlet 容器链（双注册）。
 */
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  // Boot 默认把 Security 链注册进 ERROR dispatch（dispatcher-types 含 error）；
  // OncePerRequestFilter 默认跳过 error dispatch，会令鉴权后的 /error 分发
  // （404/405/415 → ApiErrorController）落入 401。此处放行：无状态 JWT 在
  // 每次分发重新校验 Authorization 头（ERROR dispatch 保留原请求头）。
  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        UserPrincipal principal = jwtService.verify(header.substring(7));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ignored) {
        // 无效令牌按未认证处理，由 Security 的 401 入口统一响应
      }
    }
    chain.doFilter(request, response);
  }
}
