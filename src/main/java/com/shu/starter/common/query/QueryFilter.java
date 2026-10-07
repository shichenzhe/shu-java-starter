package com.shu.starter.common.query;

import lombok.Data;

@Data
public abstract class QueryFilter {
  private long pageNum = 1;
  private long pageSize = 10;
}
