package com.shu.starter.common.web;

import com.shu.starter.common.auth.UserPrincipal;
import com.shu.starter.common.config.AppProps;
import com.shu.starter.modules.operationlog.entity.OperationLogEntity;
import com.shu.starter.modules.operationlog.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {
  private final OperationLogMapper operationLogMapper;
  private final AppProps appProps;
  private final IdentifierGenerator idGenerator;
  private static final String START_TIME = "oplog.startTime";

  public OperationLogInterceptor(
      OperationLogMapper operationLogMapper,
      AppProps appProps,
      IdentifierGenerator idGenerator) {
    this.operationLogMapper = operationLogMapper;
    this.appProps = appProps;
    this.idGenerator = idGenerator;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    request.setAttribute(START_TIME, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    if (!appProps.operationLog().enabled()) return;
    OperationLogEntity log = new OperationLogEntity();
    log.setId(idGenerator.nextUUID(log));
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
      log.setUserId(p.id());
    }
    log.setOperation(request.getMethod() + " " + request.getRequestURI());
    log.setIpAddress(request.getRemoteAddr());
    log.setUserAgent(request.getHeader("User-Agent"));
    Object start = request.getAttribute(START_TIME);
    if (start instanceof Long s) {
      log.setExecutionTime((int) (System.currentTimeMillis() - s));
    }
    log.setStatus(ex == null && response.getStatus() < 400 ? "SUCCESS" : "FAILURE");
    log.setCreatedAt(LocalDateTime.now());
    operationLogMapper.insert(log);
  }
}
