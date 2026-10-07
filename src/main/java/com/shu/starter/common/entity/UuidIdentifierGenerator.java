package com.shu.starter.common.entity;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

/** 主键生成器：UUIDv7（RFC 9562，应用层分配，趋势递增字符串） */
@Component
public class UuidIdentifierGenerator implements IdentifierGenerator {
  @Override
  public Number nextId(Object entity) {
    throw new UnsupportedOperationException("字符串主键请用 nextUUID");
  }

  @Override
  public String nextUUID(Object entity) {
    return UuidCreator.getTimeOrderedEpoch().toString();
  }
}
