package com.shu.starter.common.query;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryResult<T> {
  private List<T> records;
  private long total;
  private long pageNum;
  private long pageSize;

  public static <T> QueryResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
    return new QueryResult<>(records, total, pageNum, pageSize);
  }
}
