package com.shu.starter.common.entity;

public record Operator(String id, String name) {
  public static final Operator SYSTEM = new Operator("init", "init");
}
