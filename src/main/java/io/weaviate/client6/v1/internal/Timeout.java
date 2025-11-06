package io.weaviate.client6.v1.internal;

public record Timeout(int initSeconds, int querySeconds, int insertSeconds) {
  public Timeout() {
    this(30, 60, 120);
  }

  public Timeout(int timeout) {
    this(timeout, timeout, timeout);
  }
}
