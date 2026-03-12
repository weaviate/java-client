package io.weaviate.client6.v1.internal;

import javax.annotation.Nullable;

public record Proxy(
    String scheme,
    String host,
    int port,
    @Nullable String username,
    @Nullable String password
) {
  public Proxy(String host, int port) {
    this("http", host, port, null, null);
  }
}
